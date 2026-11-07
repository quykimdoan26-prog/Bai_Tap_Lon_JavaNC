package com.ucop.desktop.domain;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ITEM")
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "CategoryID")
    private Category category;
    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String sku;
    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;
    @NotNull @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal basePrice;
    @Column(precision = 10, scale = 2)
    private BigDecimal weight = BigDecimal.ZERO;
    @Column(length = 20)
    private String status = "ACTIVE";
    public Integer getItemId() { return itemId; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @Override public String toString() { return sku + " - " + name; }
}
