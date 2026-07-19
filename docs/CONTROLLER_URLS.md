# Controller URL List

## PublicController

| Method | URL | View/Action |
|---|---|---|
| GET | `/` | Trang chủ |
| GET | `/books` | Danh sách/tìm kiếm/lọc |
| GET | `/books/{id}` | Chi tiết sách |

## AuthController

| Method | URL | View/Action |
|---|---|---|
| GET | `/auth/login` | Form login |
| POST | `/auth/login` | Spring Security xử lý |
| GET/POST | `/auth/register` | Đăng ký |
| GET/POST | `/auth/forgot-password` | Tạo reset token |
| GET/POST | `/auth/reset-password` | Đặt mật khẩu mới |
| POST | `/auth/logout` | Spring Security logout |

## Customer

| Method | URL | Chức năng |
|---|---|---|
| GET | `/customer/profile` | Hồ sơ và địa chỉ |
| POST | `/customer/profile/update` | Cập nhật hồ sơ |
| POST | `/customer/profile/addresses` | Thêm địa chỉ |
| POST | `/customer/profile/addresses/{id}/delete` | Vô hiệu địa chỉ |
| POST | `/customer/profile/change-password` | Đổi mật khẩu |
| GET | `/customer/wallet` | Ví và lịch sử |
| POST | `/customer/wallet/top-up` | Nạp ví mô phỏng |
| GET | `/customer/cart` | Giỏ hàng |
| POST | `/customer/cart/add/{bookId}` | Thêm mua/thuê |
| POST | `/customer/cart/items/{itemId}/update` | Cập nhật dòng |
| POST | `/customer/cart/items/{itemId}/remove` | Xóa dòng |
| POST | `/customer/purchases/checkout` | Tạo đơn mua |
| GET | `/customer/purchases` | Lịch sử mua |
| GET | `/customer/purchases/{id}` | Chi tiết đơn mua |
| POST | `/customer/purchases/{id}/cancel` | Hủy đơn mua PENDING |
| POST | `/customer/rentals/checkout` | Tạo đơn thuê |
| GET | `/customer/rentals` | Lịch sử thuê |
| GET | `/customer/rentals/{id}` | Chi tiết đơn thuê |
| POST | `/customer/rentals/{id}/cancel` | Hủy đơn thuê PENDING |
| POST | `/customer/rentals/{id}/request-return` | Yêu cầu trả |
| POST | `/customer/rentals/details/{detailId}/extension` | Yêu cầu gia hạn |

## Staff

| Method | URL | Chức năng |
|---|---|---|
| GET | `/staff` | Dashboard xử lý đơn |
| GET | `/staff/purchases/{id}` | Chi tiết đơn mua |
| POST | `/staff/purchases/{id}/approve` | Xác nhận bán |
| POST | `/staff/purchases/{id}/reject` | Từ chối, hoàn tiền |
| POST | `/staff/purchases/{id}/complete` | Hoàn tất |
| GET | `/staff/rentals/{id}` | Chi tiết đơn thuê |
| POST | `/staff/rentals/{id}/approve` | Xác nhận giao sách |
| POST | `/staff/rentals/{id}/reject` | Từ chối, hoàn tiền |
| POST | `/staff/rentals/details/{detailId}/return` | Xử lý trả |
| POST | `/staff/extensions/{id}/process` | Duyệt/từ chối gia hạn |
| GET | `/staff/revenue` | Doanh thu |
| GET | `/staff/revenue/export.csv` | Xuất CSV |

## Admin

| Method | URL | Chức năng |
|---|---|---|
| GET | `/admin` | Dashboard |
| GET | `/admin/activity-logs` | Nhật ký |
| GET | `/admin/books` | Danh sách Book |
| GET | `/admin/books/new` | Form thêm Book |
| GET | `/admin/books/{id}/edit` | Form sửa Book |
| POST | `/admin/books/save` | Lưu Book |
| GET | `/admin/books/{bookId}/copies` | Kho BookCopy |
| POST | `/admin/book-copies/save` | Lưu BookCopy |
| GET | `/admin/categories` | Thể loại |
| POST | `/admin/categories/save` | Lưu thể loại |
| GET | `/admin/users` | Khách hàng và Staff |
| POST | `/admin/users/staff` | Tạo Staff |
| POST | `/admin/users/{id}/toggle-lock` | Khóa/mở khóa non-Admin |
| GET | `/admin/vouchers` | Voucher |
| GET | `/admin/vouchers/new` | Form tạo voucher |
| GET | `/admin/vouchers/{id}/edit` | Form sửa voucher |
| POST | `/admin/vouchers/save` | Lưu voucher |
| GET | `/admin/revenue` | Doanh thu |
