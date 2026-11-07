package com.ucop.desktop.service;

import com.ucop.desktop.repo.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final OrderRepository orders;

    public ReportService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public Map<Integer, BigDecimal> revenueByMonth(int year) {
        return orders.findByCreatedAtBetweenAndStatusNot(
                        LocalDateTime.of(year, 1, 1, 0, 0),
                        LocalDateTime.of(year + 1, 1, 1, 0, 0),
                        "CART")
                .stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().getMonthValue(),
                        Collectors.mapping(order -> order.getGrandTotal(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }

    @Transactional(readOnly = true)
    public BigDecimal revenueToday() {
        LocalDate today = LocalDate.now();
        return orders.findByCreatedAtBetweenAndStatusNot(
                        today.atStartOfDay(), today.plusDays(1).atStartOfDay(), "CART")
                .stream()
                .map(order -> order.getGrandTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
