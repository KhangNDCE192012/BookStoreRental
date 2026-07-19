package vn.edu.fpt.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AddressForm {
    @NotBlank @Size(max = 120)
    private String receiverName;
    @NotBlank @Pattern(regexp = "^[0-9+() -]{9,20}$", message = "Số điện thoại không hợp lệ")
    private String receiverPhone;
    @NotBlank @Size(max = 255)
    private String addressLine;
    @NotBlank @Size(max = 100)
    private String province;
    @NotBlank @Size(max = 100)
    private String district;
    @NotBlank @Size(max = 100)
    private String ward;
    private boolean defaultAddress;

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public boolean isDefaultAddress() { return defaultAddress; }
    public void setDefaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; }
}
