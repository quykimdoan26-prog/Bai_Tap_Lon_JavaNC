package com.ucop.desktop.service;

import com.ucop.desktop.domain.Account;
import com.ucop.desktop.domain.StockItem;
import com.ucop.desktop.domain.Warehouse;
import com.ucop.desktop.repo.StockItemRepository;
import com.ucop.desktop.repo.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    private final WarehouseRepository warehouses;
    private final StockItemRepository stocks;

    public InventoryService(WarehouseRepository warehouses, StockItemRepository stocks) {
        this.warehouses = warehouses;
        this.stocks = stocks;
    }

    @Transactional
    public Warehouse saveWarehouse(Account actor, Warehouse warehouse) {
        requireStaff(actor);
        if (warehouse.getCode() == null || warehouse.getCode().isBlank()
                || warehouse.getName() == null || warehouse.getName().isBlank()) {
            throw new IllegalArgumentException("Mã và tên kho không được trống.");
        }
        Warehouse existing = warehouses.findByCodeIgnoreCase(warehouse.getCode().trim()).orElse(null);
        if (existing != null && !existing.getWarehouseId().equals(warehouse.getWarehouseId())) {
            existing.setName(warehouse.getName().trim());
            return warehouses.save(existing);
        }
        warehouse.setCode(warehouse.getCode().trim());
        warehouse.setName(warehouse.getName().trim());
        return warehouses.save(warehouse);
    }

    @Transactional
    public StockItem saveStock(Account actor, StockItem stock) {
        requireStaff(actor);
        if (stock.getItem() == null || stock.getWarehouse() == null) {
            throw new IllegalArgumentException("Hãy chọn sản phẩm và kho.");
        }
        if (stock.getQuantityOnHand() == null || stock.getQuantityOnHand() < 0
                || stock.getQuantityReserved() == null || stock.getQuantityReserved() < 0) {
            throw new IllegalArgumentException("Số lượng tồn không hợp lệ.");
        }
        StockItem existing = stocks.findByItemAndWarehouse(
                stock.getItem().getItemId(), stock.getWarehouse().getWarehouseId()).orElse(null);
        if (existing != null && !existing.getStockItemId().equals(stock.getStockItemId())) {
            existing.setQuantityOnHand(stock.getQuantityOnHand());
            existing.setQuantityReserved(stock.getQuantityReserved());
            return stocks.save(existing);
        }
        return stocks.save(stock);
    }

    private void requireStaff(Account actor) {
        String role = actor == null || actor.getRole() == null ? "" : actor.getRole().getRoleName();
        if (!"ADMIN".equalsIgnoreCase(role) && !"STAFF".equalsIgnoreCase(role)) {
            throw new SecurityException("Chỉ Admin hoặc Staff được quản lý kho.");
        }
    }
}
