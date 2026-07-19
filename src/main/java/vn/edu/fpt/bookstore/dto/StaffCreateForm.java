package vn.edu.fpt.bookstore.dto;

import jakarta.validation.constraints.*;

public class StaffCreateForm {
    @NotBlank @Size(min = 4, max = 50)
    private String username;
    @NotBlank @Email @Size(max = 150)
    private String email;
    @NotBlank @Size(max = 120)
    private String fullName;
    @Pattern(regexp = "^$|^[0-9+() -]{9,20}$")
    private String phone;
    @NotBlank @Size(min = 8, max = 72)
    private String password;
    @NotBlank @Size(max = 30)
    private String employeeCode;
    @Size(max = 80)
    private String position;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}
