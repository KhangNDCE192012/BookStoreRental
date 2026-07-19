# Book Store & Rental Management System

Dự án Spring Boot mới được thiết kế từ đầu cho nghiệp vụ **bán sách vật lý và cho thuê sách vật lý**. Project Game Account Store chỉ được dùng để tham khảo cách tổ chức MVC, phân lớp Controller - Service - Repository - Entity, phân quyền, Thymeleaf, ví, voucher, đơn mua, đơn thuê và thống kê.

## 1. Công nghệ

- Java 17
- Spring Boot 3.5.6
- Maven
- Spring MVC
- Spring Data JPA / Hibernate
- Spring Security 6
- Thymeleaf
- Jakarta Validation
- BCrypt strength 12
- SQL Server
- HTML, CSS, JavaScript, Bootstrap 5
- Apache POI dependency đã khai báo để có thể mở rộng xuất Excel; bản hiện tại xuất CSV trực tiếp.

## 2. Nguyên tắc thiết kế

Dự án không sửa source cũ và không đổi tên máy móc từ Game sang Book. Thiết kế mới phân biệt rõ:

- `Book`: thông tin chung của đầu sách.
- `BookCopy`: từng cuốn vật lý có mã, tình trạng, trạng thái và vị trí kệ riêng.
- Một `BookCopy` đang `RENTED` hoặc `RESERVED` không thể được chọn cho giao dịch khác.
- `RentalOrderDetail.bookCopy` là quan hệ nhiều-một để một cuốn có thể được thuê nhiều lần ở các thời điểm khác nhau, nhưng không đồng thời.
- `PurchaseOrderDetail.bookCopy` là duy nhất vì cuốn đã bán không quay lại kho.
- Thanh toán ví, giữ cuốn và tạo đơn nằm trong cùng transaction.
- Không xóa cứng dữ liệu nghiệp vụ; dùng trạng thái `INACTIVE`, `LOCKED`, `DAMAGED`, `LOST`.
- Mật khẩu chỉ lưu BCrypt; không dùng MD5.

## 3. Mapping từ project mẫu

| Game Account Store | Book Store & Rental | Xử lý |
|---|---|---|
| Game | Book | Thiết kế mới với ISBN, tác giả, NXB, giá mua/thuê/cọc |
| Type | Category | Giữ vai trò phân loại |
| GameAccount | BookCopy | Thay credential/rank/skin bằng copyCode/condition/status/shelf |
| Orders | PurchaseOrder | Bổ sung giao nhận, phí ship và trạng thái vật lý |
| OrderDetail | PurchaseOrderDetail | Mỗi dòng gắn một BookCopy bán ra |
| RentAccountGame | RentalOrder + RentalOrderDetail | Tách header/detail, hạn trả và phí theo từng cuốn |
| Cart | Cart + CartItem | Hỗ trợ loại PURCHASE và RENTAL |
| VoucherCustomer | UserVoucher | Theo dõi số lần sử dụng |
| Transaction/balance | Wallet + WalletTransaction | Tách ví và sổ giao dịch |
| GameOwner/GameOwned | Không dùng | Loại bỏ hoàn toàn |
| username/password game, rank, level, skin, VIP | Không dùng | Loại bỏ hoàn toàn |

## 4. Cấu trúc package

```text
vn.edu.fpt.bookstore
├── config
├── controller
│   ├── admin
│   ├── customer
│   └── staff
├── dto
├── entity
│   └── enums
├── repository
├── service
└── successfullyDat
```

Luồng xử lý chính:

```text
Browser -> Controller -> Service -> Repository -> Entity -> SQL Server
                       -> Thymeleaf Model -> HTML View
```

Danh sách đầy đủ nằm trong `docs/PROJECT_TREE.txt`.

## 5. Khởi tạo database bằng SSMS 20

1. Mở SQL Server Management Studio 20.
2. Kết nối SQL Server bằng tài khoản có quyền tạo database.
3. Chọn **New Query**.
4. Mở file `src/main/resources/db/BookStoreRentalDB.sql`.
5. Nhấn **Execute**.
6. Script tự xóa database cũ, tạo database mới, tạo bảng, constraint, index và dữ liệu mẫu.
7. Kiểm tra ba câu `SELECT` ở cuối script.

> Script xóa toàn bộ database `BookStoreRentalDB` nếu database này đã tồn tại. Không chạy lên database đang có dữ liệu cần giữ.

## 6. Cấu hình SQL Server

Mở `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=BookStoreRentalDB;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourStrongPassword123!
```

Thay `username` và `password` bằng tài khoản SQL Server của máy.

### Trường hợp dùng Windows Authentication

Cách đơn giản cho bài học là tạo SQL Login riêng:

```sql
USE master;
GO
CREATE LOGIN bookstore_app WITH PASSWORD = 'BookStore@123456';
GO
USE BookStoreRentalDB;
GO
CREATE USER bookstore_app FOR LOGIN bookstore_app;
ALTER ROLE db_datareader ADD MEMBER bookstore_app;
ALTER ROLE db_datawriter ADD MEMBER bookstore_app;
GRANT EXECUTE TO bookstore_app;
GO
```

Sau đó cấu hình:

```properties
spring.datasource.username=bookstore_app
spring.datasource.password=BookStore@123456
```

## 7. Chạy trên IntelliJ IDEA

1. Chọn **File -> Open** và mở thư mục `BookStoreRental`.
2. Chờ IntelliJ tải Maven dependencies.
3. Chọn JDK 17 hoặc JDK 21 cho Project SDK; project compile target là Java 17.
4. Tạo database bằng script SQL trước.
5. Sửa thông tin SQL Server trong `application.properties`.
6. Chạy class `vn.edu.fpt.bookstore.BookStoreRentalApplication`.
7. Truy cập `http://localhost:8080`.

Chạy bằng terminal:

```bash
mvn clean test
mvn spring-boot:run
```

## 8. Tài khoản mẫu

| Vai trò | Username | Password |
|---|---|---|
| Admin | `admin` | `Admin@123` |
| Staff | `staff` | `Staff@123` |
| Customer | `customer` | `Customer@123` |

Customer mẫu có sẵn 5.000.000 đồng trong ví và một địa chỉ giao nhận mặc định.

## 9. URL chính

### Public / Guest

| URL | Chức năng |
|---|---|
| `/` | Trang chủ |
| `/books` | Danh sách, tìm kiếm, lọc sách |
| `/books/{id}` | Chi tiết đầu sách và tồn kho |
| `/auth/register` | Đăng ký |
| `/auth/login` | Đăng nhập |
| `/auth/forgot-password` | Quên mật khẩu |

### Customer

| URL | Chức năng |
|---|---|
| `/customer/profile` | Hồ sơ, địa chỉ, đổi mật khẩu |
| `/customer/wallet` | Ví và giao dịch |
| `/customer/cart` | Giỏ mua và giỏ thuê |
| `/customer/purchases` | Lịch sử mua |
| `/customer/rentals` | Lịch sử thuê và gia hạn |

### Staff

| URL | Chức năng |
|---|---|
| `/staff` | Danh sách đơn cần xử lý |
| `/staff/purchases/{id}` | Xử lý đơn mua |
| `/staff/rentals/{id}` | Giao/nhận lại sách |
| `/staff/revenue` | Thống kê doanh thu |
| `/staff/revenue/export.csv` | Xuất CSV |

### Admin

| URL | Chức năng |
|---|---|
| `/admin` | Dashboard |
| `/admin/books` | Quản lý đầu sách |
| `/admin/books/{id}/copies` | Quản lý từng BookCopy |
| `/admin/categories` | Quản lý thể loại |
| `/admin/users` | Khách hàng, nhân viên, khóa/mở khóa |
| `/admin/vouchers` | Quản lý voucher |
| `/admin/revenue` | Doanh thu |
| `/admin/activity-logs` | Nhật ký chỉnh sửa |

Danh sách chi tiết method và URL nằm trong `docs/CONTROLLER_URLS.md`.

## 10. Luồng mua sách

1. Customer thêm đầu sách vào giỏ với loại `PURCHASE`.
2. Khi checkout, service khóa các BookCopy `AVAILABLE` bằng `PESSIMISTIC_WRITE`.
3. Voucher được khóa và kiểm tra ngày, số lượng, mức tối thiểu, giới hạn mỗi user.
4. BookCopy chuyển sang `RESERVED`.
5. Hệ thống tạo `PurchaseOrder` và từng `PurchaseOrderDetail`.
6. Ví được khóa và trừ tiền.
7. Payment chuyển `SUCCESS`; transaction commit.
8. Staff xác nhận: BookCopy chuyển `SOLD`.
9. Nếu hủy/từ chối hợp lệ: BookCopy trở lại `AVAILABLE`, tiền và lượt voucher được hoàn.

## 11. Luồng thuê, gia hạn và trả

### Checkout thuê

- Phí thuê = giá thuê/ngày x số ngày x số lượng.
- Cọc = tiền cọc từng đầu sách x số lượng.
- Voucher chỉ giảm trên phí thuê, không giảm tiền cọc.
- Tổng trừ ví = phí thuê - giảm giá + tiền cọc.
- Copy được giữ `RESERVED`; Staff xác nhận thì chuyển `RENTED` và ghi hạn trả.

### Gia hạn

- Chỉ cho phép khi đơn `RENTING`, chưa quá hạn và chưa có yêu cầu chờ xử lý cho cùng detail.
- Staff duyệt mới trừ phí gia hạn và cập nhật `dueDate`.
- Nếu ví không đủ, toàn bộ thao tác duyệt bị rollback.

### Trả sách

- Phí trễ = số ngày trễ x `app.rental.late-fee-per-day`.
- Staff chọn `GOOD`, `FAIR`, `DAMAGED` hoặc `LOST`.
- Tiền phạt được khấu trừ từ cọc.
- Nếu phạt lớn hơn cọc, hệ thống trừ thêm ví.
- Phần cọc còn lại được hoàn về ví.
- Copy trở về `AVAILABLE`, hoặc chuyển `DAMAGED`/`LOST`.

## 12. Kiểm thử theo giai đoạn

### Giai đoạn 1 - Khởi tạo và database

- Chạy script SQL không lỗi.
- Kiểm tra đủ roles, users, books và copies.
- Chạy app không có lỗi schema validation.

### Giai đoạn 2 - Authentication

- Đăng ký user mới.
- Kiểm tra password trong DB bắt đầu bằng `$2...`, không phải text thường hoặc MD5.
- Login đúng/sai.
- Khóa Customer từ Admin rồi thử login.
- Đổi mật khẩu.
- Quên mật khẩu: khi `app.mail.enabled=false`, link reset hiển thị trên màn hình để test local.

### Giai đoạn 3 - Catalog và kho

- Tìm theo tên, tác giả, ISBN.
- Lọc category và khoảng giá.
- Admin thêm Book và upload ảnh.
- Tạo nhiều BookCopy cho cùng Book.
- Không cho trùng ISBN hoặc copyCode.

### Giai đoạn 4 - Purchase

- Nạp ví.
- Thêm sách mua, cập nhật số lượng, xóa dòng.
- Checkout với voucher hợp lệ và không hợp lệ.
- Kiểm tra BookCopy `RESERVED` sau checkout.
- Staff approve -> `SOLD`.
- Customer cancel khi `PENDING` -> hoàn ví và `AVAILABLE`.
- Không cho cancel sau khi Staff xử lý.

### Giai đoạn 5 - Rental

- Thuê với số ngày 1-30.
- Kiểm tra ví phải đủ phí thuê + cọc.
- Staff approve -> `RENTED`, có dueDate.
- Gửi gia hạn, Staff duyệt/từ chối.
- Yêu cầu trả, Staff ghi tình trạng.
- Kiểm tra phí trễ, hư hỏng, mất và hoàn cọc.

### Giai đoạn 6 - Security

- Guest truy cập `/customer/cart` phải chuyển login.
- Customer truy cập `/staff` hoặc `/admin` phải bị từ chối.
- Staff truy cập `/admin/users` phải bị từ chối.
- Customer chỉ mở được đơn có `user_id` của mình.

## 13. Lỗi thường gặp

### `Login failed for user 'sa'`

- Sai password hoặc SQL Authentication chưa bật.
- Kiểm tra SQL Server Configuration Manager và restart dịch vụ.
- Có thể tạo login `bookstore_app` như hướng dẫn ở trên.

### `The TCP/IP connection to host localhost, port 1433 has failed`

- TCP/IP chưa bật.
- SQL Server không chạy port 1433.
- Instance tên riêng có thể cần cấu hình port cố định hoặc sửa JDBC URL.

### `Schema-validation: missing table ...`

- Chưa chạy script SQL hoặc app đang kết nối nhầm database.
- Kiểm tra `databaseName=BookStoreRentalDB`.

### `Port 8080 was already in use`

Đổi trong properties:

```properties
server.port=8081
```

### Ảnh upload không hiển thị

- Thư mục `uploads/books` được tạo theo working directory khi chạy app.
- Không chạy app từ thư mục chỉ đọc.
- Kiểm tra đường dẫn lưu trong `books.cover_image` bắt đầu bằng `/uploads/`.

### Voucher bị giảm số lượng nhưng checkout lỗi

Thiết kế service đặt voucher, giữ copy, trừ ví và lưu đơn trong cùng transaction. Khi exception xảy ra, transaction rollback. Không tự bắt exception bên trong service rồi bỏ qua, vì làm vậy có thể phá rollback.

## 14. Tài liệu và sơ đồ

- `docs/BookStoreRental_RDS.docx`
- `docs/BookStoreRental_SDS.docx`
- `docs/diagrams/erd.png`
- `docs/diagrams/use-case.png`
- `docs/diagrams/class-diagram.png`
- `docs/diagrams/sequence-purchase.png`
- `docs/diagrams/sequence-rental.png`
- `docs/diagrams/sequence-return.png`
- File nguồn Graphviz `.dot` đi kèm để chỉnh sửa.

## 15. Trạng thái kiểm tra kỹ thuật

- Toàn bộ Java source đã được kiểm tra cú pháp và liên kết method nội bộ bằng `javac --release 17` với bộ stub framework cục bộ.
- Toàn bộ template HTML đã parse thành công bằng HTML parser.
- SQL script được đối chiếu với tên bảng/cột và enum trong entity.
- Trong môi trường tạo artifact không có Maven runtime và SQL Server, vì vậy cần chạy `mvn clean test` và kiểm thử tích hợp SQL Server trên máy của người dùng sau khi tải project.
