package vn.edu.fpt.bookstore.entity;

import jakarta.persistence.*;
import vn.edu.fpt.bookstore.entity.enums.ExtensionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "book_extension_requests", indexes = @Index(name = "ix_extension_requests_status", columnList = "status"))
public class BookExtensionRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "extension_request_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rental_order_detail_id", nullable = false, foreignKey = @ForeignKey(name = "fk_extension_requests_rental_details"))
    private RentalOrderDetail rentalOrderDetail;

    @Column(name = "extra_days", nullable = false)
    private int extraDays;

    @Column(name = "requested_due_date", nullable = false)
    private LocalDateTime requestedDueDate;

    @Column(name = "extra_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal extraFee = BigDecimal.ZERO;

    @Column(name = "reason", length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ExtensionStatus status = ExtensionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by", foreignKey = @ForeignKey(name = "fk_extension_requests_users"))
    private User processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "staff_note", length = 500)
    private String staffNote;

    public BookExtensionRequest() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public RentalOrderDetail getRentalOrderDetail() { return rentalOrderDetail; }
    public void setRentalOrderDetail(RentalOrderDetail rentalOrderDetail) { this.rentalOrderDetail = rentalOrderDetail; }
    public int getExtraDays() { return extraDays; }
    public void setExtraDays(int extraDays) { this.extraDays = extraDays; }
    public LocalDateTime getRequestedDueDate() { return requestedDueDate; }
    public void setRequestedDueDate(LocalDateTime requestedDueDate) { this.requestedDueDate = requestedDueDate; }
    public BigDecimal getExtraFee() { return extraFee; }
    public void setExtraFee(BigDecimal extraFee) { this.extraFee = extraFee; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public ExtensionStatus getStatus() { return status; }
    public void setStatus(ExtensionStatus status) { this.status = status; }
    public User getProcessedBy() { return processedBy; }
    public void setProcessedBy(User processedBy) { this.processedBy = processedBy; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public String getStaffNote() { return staffNote; }
    public void setStaffNote(String staffNote) { this.staffNote = staffNote; }
}
