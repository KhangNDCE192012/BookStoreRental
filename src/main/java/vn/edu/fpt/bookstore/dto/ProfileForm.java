package vn.edu.fpt.bookstore.dto;

import jakarta.validation.constraints.*;

public class ProfileForm {
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 120)
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^$|^[0-9+() -]{9,20}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
