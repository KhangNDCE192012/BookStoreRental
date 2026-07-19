package vn.edu.fpt.bookstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.bookstore.dto.BookCopyForm;
import vn.edu.fpt.bookstore.dto.BookForm;
import vn.edu.fpt.bookstore.entity.Book;
import vn.edu.fpt.bookstore.entity.BookCopy;
import vn.edu.fpt.bookstore.entity.Category;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.entity.enums.BookCopyStatus;
import vn.edu.fpt.bookstore.entity.enums.BookStatus;
import vn.edu.fpt.bookstore.repository.BookCopyRepository;
import vn.edu.fpt.bookstore.repository.BookRepository;
import vn.edu.fpt.bookstore.repository.CategoryRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final ActivityLogService activityLogService;

    public BookService(BookRepository bookRepository,
                       BookCopyRepository bookCopyRepository,
                       CategoryRepository categoryRepository,
                       FileStorageService fileStorageService,
                       ActivityLogService activityLogService) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public Page<Book> search(String keyword, UUID categoryId, String author,
                             BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        return bookRepository.search(BookStatus.ACTIVE,
                blankToNull(keyword), categoryId, blankToNull(author), minPrice, maxPrice,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 48), Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Transactional(readOnly = true)
    public Book requireActive(UUID id) {
        Book book = requireById(id);
        if (book.getStatus() != BookStatus.ACTIVE) {
            throw new IllegalArgumentException("Sách không còn hoạt động");
        }
        return book;
    }

    @Transactional(readOnly = true)
    public Book requireById(UUID id) {
        return bookRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách"));
    }

    @Transactional(readOnly = true)
    public List<Book> allBooks() {
        return bookRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public List<Category> activeCategories() {
        return categoryRepository.findAllByStatusOrderByNameAsc(BookStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Category> allCategories() {
        return categoryRepository.findAll(Sort.by("name"));
    }

    @Transactional(readOnly = true)
    public List<BookCopy> copiesOf(UUID bookId) {
        return bookCopyRepository.findAllByBook_IdOrderByCopyCodeAsc(bookId);
    }

    @Transactional(readOnly = true)
    public long availableCopies(UUID bookId) {
        return bookCopyRepository.countByBook_IdAndStatus(bookId, BookCopyStatus.AVAILABLE);
    }

    @Transactional
    public Book saveBook(UUID id, BookForm form, MultipartFile cover, User actor) {
        Book book = id == null ? new Book() : requireById(id);
        bookRepository.findByIsbnIgnoreCase(form.getIsbn().trim())
                .filter(existing -> id == null || !existing.getId().equals(id))
                .ifPresent(existing -> { throw new IllegalArgumentException("ISBN đã tồn tại"); });
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Thể loại không hợp lệ"));
        book.setIsbn(form.getIsbn().trim());
        book.setTitle(form.getTitle().trim());
        book.setAuthor(form.getAuthor().trim());
        book.setPublisher(blankToNull(form.getPublisher()));
        book.setPublicationYear(form.getPublicationYear());
        book.setLanguage(blankToNull(form.getLanguage()));
        book.setPageCount(form.getPageCount());
        book.setDescription(blankToNull(form.getDescription()));
        book.setPurchasePrice(form.getPurchasePrice());
        book.setRentalPricePerDay(form.getRentalPricePerDay());
        book.setRentalDeposit(form.getRentalDeposit());
        book.setCategory(category);
        book.setStatus(form.getStatus());
        String storedCover = fileStorageService.storeBookCover(cover);
        if (storedCover != null) book.setCoverImage(storedCover);
        bookRepository.save(book);
        activityLogService.log(actor, id == null ? "CREATE_BOOK" : "UPDATE_BOOK", "Book", book.getId().toString(), book.getTitle());
        return book;
    }

    @Transactional
    public Category saveCategory(UUID id, String name, String description, BookStatus status, User actor) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Tên thể loại không được để trống");
        categoryRepository.findByNameIgnoreCase(name.trim())
                .filter(existing -> id == null || !existing.getId().equals(id))
                .ifPresent(existing -> { throw new IllegalArgumentException("Tên thể loại đã tồn tại"); });
        Category category = id == null ? new Category() : categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thể loại"));
        category.setName(name.trim());
        category.setDescription(blankToNull(description));
        category.setStatus(status == null ? BookStatus.ACTIVE : status);
        categoryRepository.save(category);
        activityLogService.log(actor, id == null ? "CREATE_CATEGORY" : "UPDATE_CATEGORY", "Category", category.getId().toString(), category.getName());
        return category;
    }

    @Transactional
    public BookCopy saveCopy(UUID id, BookCopyForm form, User actor) {
        bookCopyRepository.findByCopyCodeIgnoreCase(form.getCopyCode().trim())
                .filter(existing -> id == null || !existing.getId().equals(id))
                .ifPresent(existing -> { throw new IllegalArgumentException("Mã cuốn sách đã tồn tại"); });
        BookCopy copy = id == null ? new BookCopy() : bookCopyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cuốn sách"));
        if (id != null && (copy.getStatus() == BookCopyStatus.RENTED || copy.getStatus() == BookCopyStatus.SOLD)) {
            if (copy.getStatus() != form.getStatus()) {
                throw new IllegalArgumentException("Không thể đổi trạng thái cuốn đang thuê hoặc đã bán bằng màn hình quản lý kho");
            }
        }
        copy.setCopyCode(form.getCopyCode().trim());
        copy.setBook(requireById(form.getBookId()));
        copy.setCondition(form.getCondition());
        copy.setStatus(form.getStatus());
        copy.setShelfLocation(blankToNull(form.getShelfLocation()));
        bookCopyRepository.save(copy);
        activityLogService.log(actor, id == null ? "CREATE_BOOK_COPY" : "UPDATE_BOOK_COPY", "BookCopy", copy.getId().toString(), copy.getCopyCode());
        return copy;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
