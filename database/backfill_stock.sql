USE ucop_db;
GO

-- Tạo bản ghi tồn kho bằng 0 cho các sản phẩm chưa có tồn kho.
-- Sau đó Admin/Staff cập nhật số lượng thực tế trong màn hình Kho & tồn kho.
INSERT INTO STOCK_ITEM (WarehouseID, ItemID, Quantity_OnHand, Quantity_Reserved)
SELECT
    (SELECT TOP 1 WarehouseID FROM WAREHOUSE ORDER BY WarehouseID),
    i.ItemID,
    0,
    0
FROM ITEM i
WHERE NOT EXISTS (
    SELECT 1
    FROM STOCK_ITEM s
    WHERE s.ItemID = i.ItemID
)
AND EXISTS (SELECT 1 FROM WAREHOUSE);
GO
