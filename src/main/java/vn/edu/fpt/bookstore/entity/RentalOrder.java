package vn.edu.fpt.bookstore.entity;

import jakarta.persistence.*;
import vn.edu.fpt.bookstore.entity.enums.PaymentMethod;
import vn.edu.fpt.bookstore.entity.enums.PaymentStatus;
import vn.edu.fpt.bookstore.entity.enums.RentalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rental_orders",
        uniqueConstraints = @UniqueConstraint(name = "uq_rental_orders_code", columnNames = "rental_code"),
        indexes = {
                @Index(name = "ix_rental_orders_user_id", columnList = "user_id"),
                @Index(name = "ix_rental_orders_status", columnList = "rental_status"),
                @Index(name = "ix_rental_orders_due_date", columnList = "due_date")
        })
public class RentalOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rental_order_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "rental_code", nullable = false, length = 30)
    private String rentalCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_rental_orders_users"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", foreignKey = @ForeignKey(name = "fk_rental_orders_vouchers"))
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by", foreignKey = @ForeignKey(name = "fk_rental_orders_staff"))
    private User processedBy;

    @Column(name = "receiver_name", nullable = false, length = 120)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Column(name = "rental_date")
    private LocalDateTime rentalDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "rental_days", nullable = false)
    private int rentalDays;

    @Column(name = "deposit_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(name = "rental_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal rentalFee = BigDecimal.ZERO;

    @Column(name = "late_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(name = "damage_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal damageFee = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_status", nullable = false, length = 30)
    private RentalStatus rentalStatus;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @OneToMany(mappedBy = "rentalOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RentalOrderDetail> details = new ArrayList<>();

    public RentalOrder() {
    }

    public void addDetail(RentalOrderDetail detail) {
        details.add(detail);
        detail.setRentalOrder(this);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getRentalCode() { return rentalCode; }
    public void setRentalCode(String rentalCode) { this.rentalCode = rentalCode; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Voucher getVoucher() { return voucher; }
    public void setVoucher(Voucher voucher) { this.voucher = voucher; }
    public User getProcessedBy() { return processedBy; }
    public void setProcessedBy(User processedBy) { this.processedBy = processedBy; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public LocalDateTime getRentalDate() { return rentalDate; }
    public void setRentalDate(LocalDateTime rentalDate) { this.rentalDate = rentalDate; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }
    public int getRentalDays() { return rentalDays; }
    public void setRentalDays(int rentalDays) { this.rentalDays = rentalDays; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public BigDecimal getRentalFee() { return rentalFee; }
    public void setRentalFee(BigDecimal rentalFee) { this.rentalFee = rentalFee; }
    public BigDecimal getLateFee() { return lateFee; }
    public void setLateFee(BigDecimal lateFee) { this.lateFee = lateFee; }
    public BigDecimal getDamageFee() { return damageFee; }
    public void setDamageFee(BigDecimal damageFee) { this.damageFee = damageFee; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public RentalStatus getRentalStatus() { return rentalStatus; }
    public void setRentalStatus(RentalStatus rentalStatus) { this.rentalStatus = rentalStatus; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public List<RentalOrderDetail> getDetails() { return details; }
    public void setDetails(List<RentalOrderDetail> details) { this.details = details; }
}
