package com.ucop.desktop.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROMOTION")
public class Promotion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer promotionId;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(name = "DiscountType", nullable = false, length = 20)
    private String discountType;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal value;
    private LocalDateTime expiryDate;
    public Integer getPromotionId() { return promotionId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
}
