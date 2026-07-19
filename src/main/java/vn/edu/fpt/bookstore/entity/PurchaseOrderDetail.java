package vn.edu.fpt.bookstore.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "purchase_order_details", indexes = {
        @Index(name = "ix_purchase_order_details_order_id", columnList = "purchase_order_id"),
        @Index(name = "ix_purchase_order_details_copy_id", columnList = "book_copy_id")
})
public class PurchaseOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "purchase_order_detail_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_purchase_order_details_orders"))
    private PurchaseOrder order;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_purchase_order_details_books"))
    private Book book;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_copy_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_purchase_order_details_copies"))
    private BookCopy bookCopy;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    public PurchaseOrderDetail() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public PurchaseOrder getOrder() { return order; }
    public void setOrder(PurchaseOrder order) { this.order = order; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public BookCopy getBookCopy() { return bookCopy; }
    public void setBookCopy(BookCopy bookCopy) { this.bookCopy = bookCopy; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
