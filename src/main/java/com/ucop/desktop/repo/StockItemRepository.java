package com.ucop.desktop.repo;
import com.ucop.desktop.domain.StockItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface StockItemRepository extends JpaRepository<StockItem, Integer> {
    @Query("select s from StockItem s where s.item.itemId = :itemId and s.warehouse.warehouseId = :warehouseId")
    Optional<StockItem> findByItemAndWarehouse(
            @Param("itemId") Integer itemId,
            @Param("warehouseId") Integer warehouseId);
}
