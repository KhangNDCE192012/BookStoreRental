package vn.edu.fpt.bookstore.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import vn.edu.fpt.bookstore.entity.enums.VoucherType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VoucherForm {
    @NotBlank @Size(max = 50)
    private String code;
    @NotBlank @Size(max = 150)
    private String name;
    @NotNull
    private VoucherType type;
    @NotNull @DecimalMin("0.01")
    private BigDecimal discountValue;
    @NotNull @DecimalMin("0.00")
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;
    @DecimalMin("0.00")
    private BigDecimal maximumDiscount;
    @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;
    @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
    @Min(0)
    private int quantity;
    @Min(1)
    private int perUserLimit = 1;
    private boolean active = true;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public VoucherType getType() { return type; }
    public void setType(VoucherType type) { this.type = type; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getMinimumOrderAmount() { return minimumOrderAmount; }
    public void setMinimumOrderAmount(BigDecimal minimumOrderAmount) { this.minimumOrderAmount = minimumOrderAmount; }
    public BigDecimal getMaximumDiscount() { return maximumDiscount; }
    public void setMaximumDiscount(BigDecimal maximumDiscount) { this.maximumDiscount = maximumDiscount; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getPerUserLimit() { return perUserLimit; }
    public void setPerUserLimit(int perUserLimit) { this.perUserLimit = perUserLimit; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
