package vn.edu.fpt.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.edu.fpt.bookstore.entity.enums.BookCondition;
import vn.edu.fpt.bookstore.entity.enums.BookCopyStatus;

import java.util.UUID;

public class BookCopyForm {
    @NotBlank @Size(max = 50)
    private String copyCode;
    @NotNull
    private UUID bookId;
    @NotNull
    private BookCondition condition;
    @NotNull
    private BookCopyStatus status;
    @Size(max = 100)
    private String shelfLocation;

    public String getCopyCode() { return copyCode; }
    public void setCopyCode(String copyCode) { this.copyCode = copyCode; }
    public UUID getBookId() { return bookId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }
    public BookCondition getCondition() { return condition; }
    public void setCondition(BookCondition condition) { this.condition = condition; }
    public BookCopyStatus getStatus() { return status; }
    public void setStatus(BookCopyStatus status) { this.status = status; }
    public String getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(String shelfLocation) { this.shelfLocation = shelfLocation; }
}
