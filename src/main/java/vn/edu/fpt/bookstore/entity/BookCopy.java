package vn.edu.fpt.bookstore.entity;

import jakarta.persistence.*;
import vn.edu.fpt.bookstore.entity.enums.BookCondition;
import vn.edu.fpt.bookstore.entity.enums.BookCopyStatus;

import java.util.UUID;

@Entity
@Table(name = "book_copies",
        uniqueConstraints = @UniqueConstraint(name = "uq_book_copies_copy_code", columnNames = "copy_code"),
        indexes = {
                @Index(name = "ix_book_copies_book_id", columnList = "book_id"),
                @Index(name = "ix_book_copies_status", columnList = "status")
        })
public class BookCopy extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "book_copy_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "copy_code", nullable = false, length = 50)
    private String copyCode;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_book_copies_books"))
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_condition", nullable = false, length = 20)
    private BookCondition condition = BookCondition.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookCopyStatus status = BookCopyStatus.AVAILABLE;

    @Column(name = "shelf_location", length = 100)
    private String shelfLocation;

    public BookCopy() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCopyCode() {
        return copyCode;
    }

    public void setCopyCode(String copyCode) {
        this.copyCode = copyCode;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public BookCondition getCondition() {
        return condition;
    }

    public void setCondition(BookCondition condition) {
        this.condition = condition;
    }

    public BookCopyStatus getStatus() {
        return status;
    }

    public void setStatus(BookCopyStatus status) {
        this.status = status;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }
}
