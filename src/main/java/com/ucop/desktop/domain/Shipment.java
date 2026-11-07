package com.ucop.desktop.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SHIPMENT")
public class Shipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer shipmentId;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "OrderID")
    private CustomerOrder order;
    private String trackingCode;
    @Column(nullable = false, length = 50)
    private String status = "PENDING";
    private LocalDateTime deliveryDate;
    public void setOrder(CustomerOrder order) { this.order = order; }
    public void setTrackingCode(String trackingCode) { this.trackingCode = trackingCode; }
    public void setStatus(String status) { this.status = status; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }
}
