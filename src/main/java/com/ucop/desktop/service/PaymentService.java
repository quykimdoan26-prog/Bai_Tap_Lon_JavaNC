package com.ucop.desktop.service;

import com.ucop.desktop.domain.Account;
import com.ucop.desktop.domain.CustomerOrder;
import com.ucop.desktop.domain.Payment;
import com.ucop.desktop.repo.OrderRepository;
import com.ucop.desktop.repo.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentService {
    private final OrderRepository orders;
    private final PaymentRepository payments;

    public PaymentService(OrderRepository orders, PaymentRepository payments) {
        this.orders = orders;
        this.payments = payments;
    }

    @Transactional
    public void confirm(Account actor, int orderId) {
        requireStaff(actor);
        CustomerOrder order = order(orderId);
        Payment payment = payments.findByOrder(order)
                .orElseThrow(() -> new IllegalArgumentException("Đơn chưa có bản ghi thanh toán."));
        payment.setStatus("PAID");
        order.setStatus("PAID");
    }

    @Transactional
    public void refund(Account actor, int orderId, BigDecimal amount) {
        requireStaff(actor);
        CustomerOrder order = order(orderId);
        if (!"DELIVERED".equals(order.getStatus()) && !"CLOSED".equals(order.getStatus())
                && !"RMA_REQUESTED".equals(order.getStatus())) {
            throw new IllegalArgumentException("Chỉ hoàn tiền đơn đã giao hoặc yêu cầu hoàn.");
        }
        if (amount == null || amount.signum() <= 0 || amount.compareTo(order.getGrandTotal()) > 0) {
            throw new IllegalArgumentException("Số tiền hoàn không hợp lệ.");
        }
        Payment payment = payments.findByOrder(order)
                .orElseThrow(() -> new IllegalArgumentException("Đơn chưa có thanh toán."));
        payment.setStatus(amount.compareTo(order.getGrandTotal()) == 0 ? "REFUNDED" : "PARTIALLY_REFUNDED");
        order.setStatus("REFUNDED");
    }

    private CustomerOrder order(int id) {
        return orders.findById(id).orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại."));
    }

    private void requireStaff(Account actor) {
        String role = actor == null || actor.getRole() == null ? "" : actor.getRole().getRoleName();
        if (!"ADMIN".equalsIgnoreCase(role) && !"STAFF".equalsIgnoreCase(role)) {
            throw new SecurityException("Chỉ Admin hoặc Staff được xử lý thanh toán.");
        }
    }
}
