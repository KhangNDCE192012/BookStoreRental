package vn.edu.fpt.bookstore.dto;

import jakarta.validation.constraints.*;
import vn.edu.fpt.bookstore.entity.enums.BookStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class BookForm {
    @NotBlank @Size(max = 20)
    private String isbn;
    @NotBlank @Size(max = 250)
    private String title;
    @NotBlank @Size(max = 180)
    private String author;
    @Size(max = 180)
    private String publisher;
    @Min(1000) @Max(2100)
    private Integer publicationYear;
    @Size(max = 50)
    private String language;
    @Min(1)
    private Integer pageCount;
    private String description;
    @NotNull @DecimalMin(value = "0.0")
    private BigDecimal purchasePrice;
    @NotNull @DecimalMin(value = "0.0")
    private BigDecimal rentalPricePerDay;
    @NotNull @DecimalMin(value = "0.0")
    private BigDecimal rentalDeposit;
    @NotNull
    private UUID categoryId;
    @NotNull
    private BookStatus status = BookStatus.ACTIVE;

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public BigDecimal getRentalPricePerDay() { return rentalPricePerDay; }
    public void setRentalPricePerDay(BigDecimal rentalPricePerDay) { this.rentalPricePerDay = rentalPricePerDay; }
    public BigDecimal getRentalDeposit() { return rentalDeposit; }
    public void setRentalDeposit(BigDecimal rentalDeposit) { this.rentalDeposit = rentalDeposit; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { this.status = status; }
}
