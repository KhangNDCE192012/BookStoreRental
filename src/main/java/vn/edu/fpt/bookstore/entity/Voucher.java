package vn.edu.fpt.bookstore.entity;

import jakarta.persistence.*;
import vn.edu.fpt.bookstore.entity.enums.VoucherType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vouchers",
        uniqueConstraints = @UniqueConstraint(name = "uq_vouchers_code", columnNames = "code"),
        indexes = @Index(name = "ix_vouchers_active_dates", columnList = "active,start_date,end_date"))
public class Voucher extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "voucher_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "voucher_type", nullable = false, length = 20)
    private VoucherType type;

    @Column(name = "discount_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "minimum_order_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @Column(name = "maximum_discount", precision = 18, scale = 2)
    private BigDecimal maximumDiscount;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "per_user_limit", nullable = false)
    private int perUserLimit = 1;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_vouchers_users"))
    private User createdBy;

    public Voucher() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VoucherType getType() {
        return type;
    }

    public void setType(VoucherType type) {
        this.type = type;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public void setMinimumOrderAmount(BigDecimal minimumOrderAmount) {
        this.minimumOrderAmount = minimumOrderAmount;
    }

    public BigDecimal getMaximumDiscount() {
        return maximumDiscount;
    }

    public void setMaximumDiscount(BigDecimal maximumDiscount) {
        this.maximumDiscount = maximumDiscount;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPerUserLimit() {
        return perUserLimit;
    }

    public void setPerUserLimit(int perUserLimit) {
        this.perUserLimit = perUserLimit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}
