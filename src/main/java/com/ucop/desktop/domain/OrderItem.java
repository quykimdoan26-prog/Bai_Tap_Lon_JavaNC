package com.ucop.desktop.domain;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ORDER_ITEM")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderItemId;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "OrderID")
    private CustomerOrder order;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "ItemID")
    private Item item;
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;
    public Integer getOrderItemId() { return orderItemId; }
    public CustomerOrder getOrder() { return order; }
    public void setOrder(CustomerOrder order) { this.order = order; }
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
