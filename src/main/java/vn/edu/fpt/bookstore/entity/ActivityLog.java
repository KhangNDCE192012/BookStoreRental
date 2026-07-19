package vn.edu.fpt.bookstore.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "ix_activity_logs_actor_id", columnList = "actor_id"),
        @Index(name = "ix_activity_logs_created_at", columnList = "created_at"),
        @Index(name = "ix_activity_logs_entity", columnList = "entity_type,entity_id")
})
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "activity_log_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", foreignKey = @ForeignKey(name = "fk_activity_logs_users"))
    private User actor;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "details", length = 2000)
    private String details;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public ActivityLog() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
