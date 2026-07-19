package vn.edu.fpt.bookstore.entity;

import jakarta.persistence.*;
import vn.edu.fpt.bookstore.entity.enums.BookStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "books",
        uniqueConstraints = @UniqueConstraint(name = "uq_books_isbn", columnNames = "isbn"),
        indexes = {
                @Index(name = "ix_books_title", columnList = "title"),
                @Index(name = "ix_books_author", columnList = "author"),
                @Index(name = "ix_books_category_id", columnList = "category_id"),
                @Index(name = "ix_books_status", columnList = "status")
        })
public class Book extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "book_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "isbn", nullable = false, length = 20)
    private String isbn;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    @Column(name = "author", nullable = false, length = 180)
    private String author;

    @Column(name = "publisher", length = 180)
    private String publisher;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "description", columnDefinition = "nvarchar(max)")
    private String description;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(name = "purchase_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "rental_price_per_day", nullable = false, precision = 18, scale = 2)
    private BigDecimal rentalPricePerDay;

    @Column(name = "rental_deposit", nullable = false, precision = 18, scale = 2)
    private BigDecimal rentalDeposit;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_books_categories"))
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookStatus status = BookStatus.ACTIVE;

    public Book() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getRentalPricePerDay() {
        return rentalPricePerDay;
    }

    public void setRentalPricePerDay(BigDecimal rentalPricePerDay) {
        this.rentalPricePerDay = rentalPricePerDay;
    }

    public BigDecimal getRentalDeposit() {
        return rentalDeposit;
    }

    public void setRentalDeposit(BigDecimal rentalDeposit) {
        this.rentalDeposit = rentalDeposit;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }
}
