package com.ucop.desktop.domain;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "PAYMENT")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentId;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "OrderID")
    private CustomerOrder order;
    @Column(nullable = false, length = 50)
    private String method;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 50)
    private String status = "PENDING";
    public void setOrder(CustomerOrder order) { this.order = order; }
    public void setMethod(String method) { this.method = method; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPaymentId() { return paymentId; }
    public CustomerOrder getOrder() { return order; }
    public String getMethod() { return method; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
}
