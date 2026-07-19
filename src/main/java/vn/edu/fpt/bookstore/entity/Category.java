package vn.edu.fpt.bookstore.entity;

import jakarta.persistence.*;
import vn.edu.fpt.bookstore.entity.enums.BookStatus;

import java.util.UUID;

@Entity
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(name = "uq_categories_name", columnNames = "name"))
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookStatus status = BookStatus.ACTIVE;

    public Category() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }
}
