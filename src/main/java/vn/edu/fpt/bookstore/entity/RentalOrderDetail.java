package vn.edu.fpt.bookstore.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rental_order_details", indexes = {
        @Index(name = "ix_rental_order_details_order_id", columnList = "rental_order_id"),
        @Index(name = "ix_rental_order_details_copy_id", columnList = "book_copy_id")
})
public class RentalOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rental_order_detail_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rental_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_rental_order_details_orders"))
    private RentalOrder rentalOrder;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_rental_order_details_books"))
    private Book book;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_copy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_rental_order_details_copies"))
    private BookCopy bookCopy;

    @Column(name = "rental_days", nullable = false)
    private int rentalDays;

    @Column(name = "rental_date")
    private LocalDateTime rentalDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "rental_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal rentalFee = BigDecimal.ZERO;

    @Column(name = "deposit_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(name = "late_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(name = "damage_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal damageFee = BigDecimal.ZERO;

    public RentalOrderDetail() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public RentalOrder getRentalOrder() { return rentalOrder; }
    public void setRentalOrder(RentalOrder rentalOrder) { this.rentalOrder = rentalOrder; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public BookCopy getBookCopy() { return bookCopy; }
    public void setBookCopy(BookCopy bookCopy) { this.bookCopy = bookCopy; }
    public int getRentalDays() { return rentalDays; }
    public void setRentalDays(int rentalDays) { this.rentalDays = rentalDays; }
    public LocalDateTime getRentalDate() { return rentalDate; }
    public void setRentalDate(LocalDateTime rentalDate) { this.rentalDate = rentalDate; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }
    public BigDecimal getRentalFee() { return rentalFee; }
    public void setRentalFee(BigDecimal rentalFee) { this.rentalFee = rentalFee; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public BigDecimal getLateFee() { return lateFee; }
    public void setLateFee(BigDecimal lateFee) { this.lateFee = lateFee; }
    public BigDecimal getDamageFee() { return damageFee; }
    public void setDamageFee(BigDecimal damageFee) { this.damageFee = damageFee; }
}
