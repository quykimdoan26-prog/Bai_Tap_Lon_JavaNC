# UCOP Retail - JavaFX / Spring / Hibernate

Ứng dụng desktop JavaFX cho đề UCOP, triển khai theo 3 lớp:

- `ui`: FXML và JavaFX Controller.
- `service`: nghiệp vụ đăng nhập, RBAC nền tảng, tính tiền, khuyến mãi, tồn kho và transaction đặt hàng.
- `repo`: Spring Data JPA repository.
- `domain`: entity JPA bám theo các bảng trong database.

## Chạy dự án

1. Cài JDK 17+ và Maven 3.9+.
2. Mở SQL Server/SSMS, đăng nhập bằng tài khoản có quyền tạo database.
3. Mở `database/sqlserver_schema.sql`, chạy toàn bộ script một lần. Script tạo `ucop_db`, bảng, seed dữ liệu và tài khoản mẫu.
   Nếu database đã tạo từ trước, chạy thêm `database/seed_rbac_accounts.sql` để bổ sung/reset ba tài khoản mẫu.
4. SQL Server của máy này đang lắng nghe TCP/IP trên cổng `1434`.
5. Nếu đăng nhập bằng tài khoản `sa`, dùng cấu hình:

   ```properties
   db.auth=sql
   db.url=jdbc:sqlserver://LAPTOP-K4CK2QRN:1434;databaseName=ucop_db;encrypt=true;trustServerCertificate=true
   db.username=sa
   db.password=mat_khau_sa
   ```

   Nếu muốn dùng Windows Authentication, cần thêm file native
   `mssql-jdbc_auth-12.6.1.x64.dll` trong `PATH` hoặc chỉ định bằng
   `-Djava.library.path`.

   Cấu hình Windows Authentication tương ứng:

   ```properties
   db.auth=windows
   db.url=jdbc:sqlserver://LAPTOP-K4CK2QRN:1434;databaseName=ucop_db;encrypt=true;trustServerCertificate=true;integratedSecurity=true
   db.username=
   db.password=
   ```

6. Mở thư mục project bằng IntelliJ IDEA, chọn Maven trong cửa sổ Maven rồi chạy:

   ```text
   Plugins > javafx > javafx:run
   ```

   hoặc chạy tại terminal:

   ```powershell
   mvn clean javafx:run
   ```

Tài khoản mẫu (đều dùng mật khẩu `password`):

| Tài khoản | Vai trò | Quyền chính |
|---|---|---|
| `admin` | ADMIN | Users/RBAC, catalog, kho, đơn hàng, payment/refund, promotion, dashboard |
| `staff` | STAFF | Catalog, kho/tồn, xử lý trạng thái đơn, xác nhận thanh toán, hoàn tiền |
| `customer` | CUSTOMER | Xem sản phẩm, đặt hàng, khuyến mãi, thanh toán lựa chọn, xem đơn của mình, đổi mật khẩu |

## Đối chiếu yêu cầu đề

- RBAC: Role, Account, AccountProfile 1-1 và chặn tài khoản không ACTIVE.
- Catalog: Category đa cấp và Item thuộc Category; bảng hiển thị và export CSV.
- Kho: Warehouse, StockItem, kiểm tra tồn trước khi đặt hàng.
- Order: trạng thái `CART`, `PENDING_PAYMENT` và các trạng thái mở rộng trong schema; OrderItem lưu giá tại thời điểm mua.
- Payment: COD/TRANSFER/WALLET qua Payment, tính subtotal, discount, VAT 10%, shipping và grand total.
- Transaction: `OrderService.placeOrder` cập nhật tồn kho, tạo order/order item/payment trong một transaction.
- Dashboard: JavaFX BarChart, sẵn điểm tích hợp truy vấn doanh thu theo ngày/tháng/phương thức.
- Giao diện: tab Tổng quan, Sản phẩm & CSV và Bán hàng; Admin/Staff quản lý catalog, Customer chỉ dùng nghiệp vụ bán hàng.
- RBAC service-side: các service kiểm tra vai trò trước khi sửa account, kho, trạng thái đơn, thanh toán, hoàn tiền và promotion; UI chỉ là lớp hiển thị quyền.
- Luồng giao diện: mọi role dùng chung màn đăng nhập; `CUSTOMER` được chuyển vào trang shop riêng, còn `ADMIN/STAFF` được chuyển vào giao diện quản trị vận hành.
- Customer: đăng ký tài khoản, xem sản phẩm, giỏ hàng nhiều sản phẩm, checkout, mã giảm giá, lịch sử/trạng thái đơn và cập nhật AccountProfile.
- Khi thêm sản phẩm từ giao diện, hệ thống tự tạo dòng tồn kho ban đầu bằng 0 trong kho đầu tiên; cập nhật số lượng tại tab Kho & tồn kho trước khi khách đặt hàng.
- Xóa sản phẩm là chuyển sang trạng thái INACTIVE để không phá vỡ lịch sử đơn hàng và khóa ngoại.
- ORM: Hibernate 5.6/JPA annotation; validation nghiệp vụ và ràng buộc SQL.

Đề gốc ghi SQL Server, vì vậy project dùng dialect SQL Server và script `sqlserver_schema.sql`; file SQL đính kèm ban đầu có cú pháp MySQL nên không chạy trực tiếp trên SQL Server.
