package vn.edu.fpt.bookstore.entity;

import jakarta.persistence.*;
import vn.edu.fpt.bookstore.entity.enums.BookCondition;
import vn.edu.fpt.bookstore.entity.enums.ReturnCondition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "book_return_records", uniqueConstraints = @UniqueConstraint(name = "uq_return_record_detail", columnNames = "rental_order_detail_id"))
public class BookReturnRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "book_return_record_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rental_order_detail_id", nullable = false, foreignKey = @ForeignKey(name = "fk_return_records_rental_details"))
    private RentalOrderDetail rentalOrderDetail;

    @Column(name = "returned_at", nullable = false)
    private LocalDateTime returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_before", nullable = false, length = 20)
    private BookCondition conditionBefore;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_after", nullable = false, length = 20)
    private ReturnCondition conditionAfter;

    @Column(name = "late_days", nullable = false)
    private int lateDays;

    @Column(name = "late_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(name = "damage_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal damageFee = BigDecimal.ZERO;

    @Column(name = "lost_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal lostFee = BigDecimal.ZERO;

    @Column(name = "deposit_refund", nullable = false, precision = 18, scale = 2)
    private BigDecimal depositRefund = BigDecimal.ZERO;

    @Column(name = "notes", length = 1000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "processed_by", nullable = false, foreignKey = @ForeignKey(name = "fk_return_records_users"))
    private User processedBy;

    public BookReturnRecord() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public RentalOrderDetail getRentalOrderDetail() { return rentalOrderDetail; }
    public void setRentalOrderDetail(RentalOrderDetail rentalOrderDetail) { this.rentalOrderDetail = rentalOrderDetail; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
    public BookCondition getConditionBefore() { return conditionBefore; }
    public void setConditionBefore(BookCondition conditionBefore) { this.conditionBefore = conditionBefore; }
    public ReturnCondition getConditionAfter() { return conditionAfter; }
    public void setConditionAfter(ReturnCondition conditionAfter) { this.conditionAfter = conditionAfter; }
    public int getLateDays() { return lateDays; }
    public void setLateDays(int lateDays) { this.lateDays = lateDays; }
    public BigDecimal getLateFee() { return lateFee; }
    public void setLateFee(BigDecimal lateFee) { this.lateFee = lateFee; }
    public BigDecimal getDamageFee() { return damageFee; }
    public void setDamageFee(BigDecimal damageFee) { this.damageFee = damageFee; }
    public BigDecimal getLostFee() { return lostFee; }
    public void setLostFee(BigDecimal lostFee) { this.lostFee = lostFee; }
    public BigDecimal getDepositRefund() { return depositRefund; }
    public void setDepositRefund(BigDecimal depositRefund) { this.depositRefund = depositRefund; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public User getProcessedBy() { return processedBy; }
    public void setProcessedBy(User processedBy) { this.processedBy = processedBy; }
}
