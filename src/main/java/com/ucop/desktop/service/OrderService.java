package com.ucop.desktop.service;

import com.ucop.desktop.domain.*;
import com.ucop.desktop.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final ItemRepository items;
    private final StockItemRepository stocks;
    private final PromotionRepository promotions;
    private final PaymentRepository payments;
    private final WarehouseRepository warehouses;
    private final PromotionUsageRepository promotionUsages;

    public OrderService(OrderRepository orders, ItemRepository items, StockItemRepository stocks,
                        PromotionRepository promotions, PaymentRepository payments, WarehouseRepository warehouses,
                        PromotionUsageRepository promotionUsages) {
        this.orders = orders; this.items = items; this.stocks = stocks;
        this.promotions = promotions; this.payments = payments; this.warehouses = warehouses;
        this.promotionUsages = promotionUsages;
    }

    @Transactional
    public CustomerOrder placeOrder(Account account, int itemId, int warehouseId, int quantity,
                                    String promotionCode, String paymentMethod) {
        return placeOrder(account, List.of(new CartLine(itemId, quantity)), warehouseId, promotionCode, paymentMethod);
    }

    @Transactional
    public CustomerOrder placeOrder(Account account, List<CartLine> lines, int warehouseId,
                                    String promotionCode, String paymentMethod) {
        requireRole(account, "ADMIN", "STAFF", "CUSTOMER");
        if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("Giỏ hàng đang trống.");
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Hãy chọn phương thức thanh toán.");
        }
        CustomerOrder order = new CustomerOrder();
        order.setAccount(account);
        order.setCreatedAt(LocalDateTime.now());
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartLine cartLine : lines) {
            if (cartLine.quantity() <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
            Item item = items.findById(cartLine.itemId())
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại."));
            StockItem stock = stocks.findByItemAndWarehouse(cartLine.itemId(), warehouseId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Sản phẩm chưa được cấu hình tồn kho tại kho mặc định: " + item.getName()));
            int available = stock.getQuantityOnHand() - stock.getQuantityReserved();
            if (available < cartLine.quantity()) throw new IllegalArgumentException("Tồn kho không đủ cho " + item.getName());
            OrderItem line = new OrderItem();
            line.setOrder(order); line.setItem(item); line.setQuantity(cartLine.quantity()); line.setUnitPrice(item.getBasePrice());
            order.getItems().add(line);
            subtotal = subtotal.add(item.getBasePrice().multiply(BigDecimal.valueOf(cartLine.quantity())));
            stock.setQuantityOnHand(stock.getQuantityOnHand() - cartLine.quantity());
        }
        if (order.getItems().isEmpty()) throw new IllegalArgumentException("Giỏ hàng đang trống.");
        BigDecimal discount = BigDecimal.ZERO;
        if (promotionCode != null && !promotionCode.isBlank()) {
            Promotion promotion = promotions.findByCodeIgnoreCase(promotionCode)
                    .orElseThrow(() -> new IllegalArgumentException("Mã khuyến mãi không hợp lệ."));
            if (promotion.getExpiryDate() != null && promotion.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Mã khuyến mãi đã hết hạn.");
            }
            discount = "PERCENTAGE".equalsIgnoreCase(promotion.getDiscountType())
                    ? subtotal.multiply(promotion.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : promotion.getValue().min(subtotal);
            order.setPromotion(promotion);
        }
        BigDecimal taxable = subtotal.subtract(discount);
        BigDecimal tax = taxable.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shipping = new BigDecimal("30000");
        order.setSubTotal(subtotal); order.setDiscountAmount(discount); order.setTaxAmount(tax);
        order.setShippingFee(shipping); order.setGrandTotal(taxable.add(tax).add(shipping));
        order.setStatus("PENDING_PAYMENT");
        CustomerOrder saved = orders.save(order);

        Payment payment = new Payment();
        payment.setOrder(saved); payment.setMethod(paymentMethod); payment.setAmount(saved.getGrandTotal());
        payment.setStatus("PENDING"); payments.save(payment);
        if (saved.getPromotion() != null) {
            PromotionUsage usage = new PromotionUsage();
            usage.setPromotion(saved.getPromotion());
            usage.setAccount(account);
            usage.setOrder(saved);
            promotionUsages.save(usage);
        }
        return saved;
    }

    public record CartLine(int itemId, int quantity) {}

    @Transactional(readOnly = true)
    public int defaultWarehouseId() {
        return stocks.findAll().stream().findFirst()
                .map(stock -> stock.getWarehouse().getWarehouseId())
                .orElseGet(() -> warehouses.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chưa cấu hình kho hàng."))
                .getWarehouseId());
    }

    @Transactional(readOnly = true)
    public int warehouseFor(List<CartLine> lines) {
        if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("Giỏ hàng đang trống.");
        return warehouses.findAll().stream()
                .filter(warehouse -> lines.stream().allMatch(line ->
                        stocks.findByItemAndWarehouse(line.itemId(), warehouse.getWarehouseId())
                                .map(stock -> stock.getQuantityOnHand() - stock.getQuantityReserved() >= line.quantity())
                                .orElse(false)))
                .findFirst()
                .map(warehouse -> warehouse.getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Không có kho nào đủ tồn cho toàn bộ giỏ hàng."));
    }

    @Transactional(readOnly = true)
    public java.util.List<CustomerOrder> ordersFor(Account account) {
        if (isRole(account, "CUSTOMER")) return orders.findByAccountOrderByCreatedAtDesc(account);
        return orders.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void transition(Account actor, int orderId, String nextStatus) {
        requireRole(actor, "ADMIN", "STAFF");
        CustomerOrder order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại."));
        java.util.Set<String> allowed = switch (order.getStatus()) {
            case "PENDING_PAYMENT" -> java.util.Set.of("PAID", "CANCELED");
            case "PAID" -> java.util.Set.of("PACKED", "CANCELED", "RMA_REQUESTED");
            case "PACKED" -> java.util.Set.of("SHIPPED", "CANCELED");
            case "SHIPPED" -> java.util.Set.of("DELIVERED");
            case "DELIVERED" -> java.util.Set.of("CLOSED", "RMA_REQUESTED");
            case "RMA_REQUESTED" -> java.util.Set.of("REFUNDED");
            default -> java.util.Set.of();
        };
        if (!allowed.contains(nextStatus)) {
            throw new IllegalArgumentException("Không thể chuyển " + order.getStatus() + " sang " + nextStatus);
        }
        order.setStatus(nextStatus);
    }

    private boolean isRole(Account account, String role) {
        return account != null && account.getRole() != null
                && role.equalsIgnoreCase(account.getRole().getRoleName());
    }

    private void requireRole(Account account, String... roles) {
        for (String role : roles) if (isRole(account, role)) return;
        throw new SecurityException("Bạn không có quyền thực hiện chức năng này.");
    }
}
