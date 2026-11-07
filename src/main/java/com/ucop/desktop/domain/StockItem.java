package com.ucop.desktop.domain;

import javax.persistence.*;

@Entity
@Table(name = "STOCK_ITEM", uniqueConstraints = @UniqueConstraint(columnNames = {"WarehouseID", "ItemID"}))
public class StockItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stockItemId;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "WarehouseID")
    private Warehouse warehouse;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "ItemID")
    private Item item;
    @Column(name = "Quantity_OnHand")
    private Integer quantityOnHand = 0;
    @Column(name = "Quantity_Reserved")
    private Integer quantityReserved = 0;
    public Integer getStockItemId() { return stockItemId; }
    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    public Integer getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(Integer quantityOnHand) { this.quantityOnHand = quantityOnHand; }
    public Integer getQuantityReserved() { return quantityReserved; }
    public void setQuantityReserved(Integer quantityReserved) { this.quantityReserved = quantityReserved; }
    @Override public String toString() {
        return item.getName() + " | On hand: " + quantityOnHand + " | Reserved: " + quantityReserved;
    }
}
