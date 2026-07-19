# Validation Report

Ngày kiểm tra: 19/07/2026

## Kiểm tra đã thực hiện trong môi trường tạo project

- 102 file Java trong `src/main/java` đã compile cú pháp và liên kết method nội bộ thành công bằng `javac --release 17 -proc:none` với bộ stub framework cục bộ.
- 30 template Thymeleaf/HTML đã parse thành công bằng `lxml.html`.
- SQL script có đủ 21 bảng nghiệp vụ bắt buộc và 3 mật khẩu mẫu ở dạng BCrypt cost 12.
- `BookStoreRental_RDS.docx` đã render và kiểm tra trực quan đủ 12 trang.
- `BookStoreRental_SDS.docx` đã render và kiểm tra trực quan đủ 16 trang.
- Không phát hiện placeholder `TODO`, `FIXME`, “phần còn lại tự làm” trong source bàn giao.

## Giới hạn môi trường

Môi trường tạo artifact không có Maven runtime và SQL Server. Vì vậy, trên máy đích cần chạy:

```bash
mvn clean test
mvn spring-boot:run
```

Sau đó thực hiện checklist tích hợp SQL Server trong `README.md` và `docs/PHASED_IMPLEMENTATION_GUIDE.md`.
