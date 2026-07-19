# Hướng dẫn triển khai theo 10 giai đoạn

Tài liệu này giúp đối chiếu toàn bộ project đã bàn giao với thứ tự xây dựng. Mỗi file được cung cấp đầy đủ trong source; không có placeholder kiểu “phần còn lại tự làm”.

## Giai đoạn 1 - Phân tích project mẫu

**Mục tiêu:** chỉ tái sử dụng tư duy tổ chức MVC, role, cart/order/rental/voucher/wallet/revenue; loại bỏ toàn bộ credential/rank/skin/level/VIP và GameOwned.

**Kết quả:** bảng mapping trong `README.md`, RDS và SDS.

## Giai đoạn 2 - Khởi tạo project mới

**File chính:**

- `pom.xml`
- `src/main/java/vn/edu/fpt/bookstore/BookStoreRentalApplication.java`
- `src/main/resources/application.properties`
- `src/test/resources/application-test.properties`
- `config/SecurityConfig.java`, `config/WebConfig.java`

**Kiểm tra:** import Maven bằng JDK 17, sửa datasource, chạy class main; URL `/` phải trả trang chủ.

## Giai đoạn 3 - Database SQL Server

**File:** `src/main/resources/db/BookStoreRentalDB.sql`.

**Kiểm tra:** chạy toàn bộ script trong SSMS 20; ba SELECT cuối script phải trả roles/users/books/copies.

## Giai đoạn 4 - Entity, enum và repository

**Folder:** `entity`, `entity/enums`, `repository`.

**Kiểm tra:** `mvn test`; Hibernate `ddl-auto=validate` không báo thiếu bảng/cột.

## Giai đoạn 5 - Đăng ký, đăng nhập và phân quyền

**File chính:** `AuthController`, `AuthService`, `CustomUserDetailsService`, `CurrentUserService`, `SecurityConfig`, các DTO auth và templates `auth/*`.

**URL:** `/auth/register`, `/auth/login`, `/auth/forgot-password`, `/auth/reset-password`.

**Kiểm tra:** đăng ký, login ba role, đổi/quên mật khẩu, khóa user; DB chỉ chứa BCrypt.

## Giai đoạn 6 - Quản lý sách và kho

**File chính:** `PublicController`, `AdminBookController`, `BookService`, `BookRepository`, `BookCopyRepository`, `FileStorageService`, templates `public/*`, `admin/books*`, `admin/book-copies*`.

**URL:** `/books`, `/books/{id}`, `/admin/books`, `/admin/books/{id}/copies`, `/admin/categories`.

**Kiểm tra:** ISBN/copyCode không trùng; upload bìa; Book có nhiều BookCopy độc lập.

## Giai đoạn 7 - Giỏ hàng và mua sách

**File chính:** `CartController`, `PurchaseOrderController`, `CartService`, `PurchaseOrderService`, `VoucherService`, `WalletService` và các entity/repository Cart, PurchaseOrder, Payment, Wallet, Voucher.

**URL:** `/customer/cart`, `/customer/purchases`, `/staff/purchases/{id}`.

**Kiểm tra:** checkout, RESERVED -> SOLD, cancel/reject hoàn ví và copy; lỗi phải rollback.

## Giai đoạn 8 - Thuê, gia hạn và trả sách

**File chính:** `RentalOrderController`, `StaffDashboardController`, `RentalOrderService`, RentalOrder/Detail, ExtensionRequest, ReturnRecord và repository tương ứng.

**URL:** `/customer/rentals`, `/staff/rentals/{id}`, `/staff/extensions/{id}/process`.

**Kiểm tra:** phí thuê+cọc, RENTED, overdue, extension, return, phí trễ/hư/mất và hoàn cọc.

## Giai đoạn 9 - Staff và Admin

**File chính:** toàn bộ `controller/staff`, `controller/admin`, `UserService`, `RevenueService`, `ActivityLogService`.

**URL:** `/staff`, `/admin`, `/admin/users`, `/admin/vouchers`, `/admin/revenue`, `/admin/activity-logs`.

**Kiểm tra:** Staff không truy cập `/admin/**`; Admin tạo Staff và khóa non-Admin.

## Giai đoạn 10 - Giao diện và kiểm thử

**Folder:** `templates`, `static`, `src/test`.

**Kiểm tra tối thiểu:**

1. Guest/Customer/Staff/Admin đúng quyền.
2. Mua, hủy và xử lý đơn.
3. Thuê, gia hạn, trả và quá hạn.
4. Ví/voucher không bị thay đổi khi transaction thất bại.
5. Không hard-delete dữ liệu đã có lịch sử.
6. Chạy `mvn clean test` và test tích hợp với SQL Server.

## URL và lỗi thường gặp

- URL đầy đủ: `docs/CONTROLLER_URLS.md`.
- Cấu hình, tài khoản mẫu, lỗi SQL/TCP/port/upload: `README.md`.
- Thiết kế và test case: `docs/BookStoreRental_RDS.docx`, `docs/BookStoreRental_SDS.docx`.
