package com.ucop.desktop.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "[ORDER]")
public class CustomerOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "AccountID")
    private Account account;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "PromotionID")
    private Promotion promotion;
    @Column(nullable = false, length = 50)
    private String status = "CART";
    private BigDecimal subTotal = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal shippingFee = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    public Integer getOrderId() { return orderId; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public Promotion getPromotion() { return promotion; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getSubTotal() { return subTotal; }
    public void setSubTotal(BigDecimal value) { subTotal = value; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal value) { discountAmount = value; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal value) { taxAmount = value; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal value) { shippingFee = value; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal value) { grandTotal = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public List<OrderItem> getItems() { return items; }
    @PrePersist
    void beforeInsert() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
