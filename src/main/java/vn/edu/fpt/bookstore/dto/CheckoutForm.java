package vn.edu.fpt.bookstore.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CheckoutForm {
    @NotNull(message = "Vui lòng chọn địa chỉ giao nhận")
    private UUID addressId;
    private String voucherCode;

    public UUID getAddressId() { return addressId; }
    public void setAddressId(UUID addressId) { this.addressId = addressId; }
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
}
