package com.ucop.desktop.service;

import com.opencsv.CSVReader;
import com.ucop.desktop.domain.Category;
import com.ucop.desktop.domain.Item;
import com.ucop.desktop.domain.StockItem;
import com.ucop.desktop.repo.CategoryRepository;
import com.ucop.desktop.repo.ItemRepository;
import com.ucop.desktop.repo.StockItemRepository;
import com.ucop.desktop.repo.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class CatalogService {
    private final CategoryRepository categories;
    private final ItemRepository items;
    private final WarehouseRepository warehouses;
    private final StockItemRepository stocks;

    public CatalogService(CategoryRepository categories, ItemRepository items,
                          WarehouseRepository warehouses, StockItemRepository stocks) {
        this.categories = categories; this.items = items;
        this.warehouses = warehouses; this.stocks = stocks;
    }

    @Transactional(readOnly = true)
    public List<Item> activeItems() {
        return items.findByStatusOrderByNameAsc("ACTIVE");
    }

    @Transactional
    public Item saveItem(Item item) {
        if (item.getCategory() == null) throw new IllegalArgumentException("Hãy chọn danh mục sản phẩm.");
        if (item.getSku() == null || item.getSku().isBlank()
                || item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("SKU và tên sản phẩm không được để trống.");
        }
        if (item.getBasePrice() == null || item.getBasePrice().signum() <= 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0.");
        }
        Item existing = items.findBySkuIgnoreCase(item.getSku().trim()).orElse(null);
        if (existing != null && !existing.getItemId().equals(item.getItemId())) {
            throw new IllegalArgumentException("SKU đã tồn tại: " + item.getSku());
        }
        item.setSku(item.getSku().trim());
        item.setName(item.getName().trim());
        Item saved = items.save(item);
        warehouses.findAll().stream().findFirst().ifPresent(warehouse -> {
            if (stocks.findByItemAndWarehouse(saved.getItemId(), warehouse.getWarehouseId()).isEmpty()) {
                StockItem stock = new StockItem();
                stock.setItem(saved); stock.setWarehouse(warehouse);
                stock.setQuantityOnHand(0); stock.setQuantityReserved(0);
                stocks.save(stock);
            }
        });
        return saved;
    }

    @Transactional
    public void deleteItem(int itemId) {
        Item item = items.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại."));
        item.setStatus("INACTIVE");
        items.save(item);
    }

    @Transactional
    public Category saveCategory(Category category) {
        if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống.");
        }
        return categories.save(category);
    }

    @Transactional
    public int importItems(Path csvFile, Category category) throws Exception {
        if (category == null) throw new IllegalArgumentException("Hãy chọn danh mục trước khi nhập CSV.");
        int imported = 0;
        try (Reader reader = Files.newBufferedReader(csvFile);
             CSVReader csv = new CSVReader(reader)) {
            String[] row;
            boolean header = true;
            while ((row = csv.readNext()) != null) {
                if (header) { header = false; continue; }
                if (row.length < 3 || row[0].isBlank() || row[1].isBlank()) continue;
                Item item = new Item();
                item.setCategory(category);
                item.setSku(row[0].trim());
                item.setName(row[1].trim());
                item.setBasePrice(new BigDecimal(row[2].trim()));
                item.setStatus(row.length > 3 && !row[3].isBlank() ? row[3].trim() : "ACTIVE");
                saveItem(item);
                imported++;
            }
        }
        return imported;
    }
}
