package com.ucop.desktop.domain;

import javax.persistence.*;

@Entity
@Table(name = "WAREHOUSE")
public class Warehouse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer warehouseId;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(nullable = false, length = 100)
    private String name;
    public Integer getWarehouseId() { return warehouseId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    @Override public String toString() { return code + " - " + name; }
}
