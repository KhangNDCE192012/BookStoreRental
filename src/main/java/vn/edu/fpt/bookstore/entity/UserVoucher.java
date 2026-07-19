package vn.edu.fpt.bookstore.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_vouchers",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_vouchers_user_voucher", columnNames = {"user_id", "voucher_id"}))
public class UserVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_voucher_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_vouchers_users"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voucher_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_vouchers_vouchers"))
    private Voucher voucher;

    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    public UserVoucher() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}
