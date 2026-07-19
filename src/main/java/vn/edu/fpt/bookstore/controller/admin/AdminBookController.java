package vn.edu.fpt.bookstore.controller.admin;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.bookstore.dto.BookCopyForm;
import vn.edu.fpt.bookstore.dto.BookForm;
import vn.edu.fpt.bookstore.entity.Book;
import vn.edu.fpt.bookstore.entity.BookCopy;
import vn.edu.fpt.bookstore.entity.Category;
import vn.edu.fpt.bookstore.entity.enums.BookCondition;
import vn.edu.fpt.bookstore.entity.enums.BookCopyStatus;
import vn.edu.fpt.bookstore.entity.enums.BookStatus;
import vn.edu.fpt.bookstore.service.BookService;
import vn.edu.fpt.bookstore.service.CurrentUserService;

import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminBookController {
    private final BookService bookService;
    private final CurrentUserService currentUserService;

    public AdminBookController(BookService bookService, CurrentUserService currentUserService) {
        this.bookService = bookService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/books")
    public String books(Model model) {
        model.addAttribute("books", bookService.allBooks());
        return "admin/books";
    }

    @GetMapping("/books/new")
    public String newBook(Model model) {
        model.addAttribute("bookForm", new BookForm());
        addBookOptions(model);
        return "admin/book-form";
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(@PathVariable UUID id, Model model) {
        Book book = bookService.requireById(id);
        BookForm form = new BookForm();
        form.setIsbn(book.getIsbn());
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setPublisher(book.getPublisher());
        form.setPublicationYear(book.getPublicationYear());
        form.setLanguage(book.getLanguage());
        form.setPageCount(book.getPageCount());
        form.setDescription(book.getDescription());
        form.setPurchasePrice(book.getPurchasePrice());
        form.setRentalPricePerDay(book.getRentalPricePerDay());
        form.setRentalDeposit(book.getRentalDeposit());
        form.setCategoryId(book.getCategory().getId());
        form.setStatus(book.getStatus());
        model.addAttribute("book", book);
        model.addAttribute("bookForm", form);
        addBookOptions(model);
        return "admin/book-form";
    }

    @PostMapping("/books/save")
    public String saveBook(@RequestParam(required = false) UUID id,
                           @Valid @ModelAttribute BookForm bookForm,
                           BindingResult bindingResult,
                           @RequestParam(required = false) MultipartFile cover,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            if (id != null) model.addAttribute("book", bookService.requireById(id));
            addBookOptions(model);
            return "admin/book-form";
        }
        try {
            Book saved = bookService.saveBook(id, bookForm, cover, currentUserService.requireCurrentUser());
            redirectAttributes.addFlashAttribute("success", "Đã lưu sách " + saved.getTitle());
            return "redirect:/admin/books";
        } catch (RuntimeException e) {
            bindingResult.reject("book", e.getMessage());
            if (id != null) model.addAttribute("book", bookService.requireById(id));
            addBookOptions(model);
            return "admin/book-form";
        }
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", bookService.allCategories());
        model.addAttribute("statuses", BookStatus.values());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam(required = false) UUID id,
                               @RequestParam String name,
                               @RequestParam(required = false) String description,
                               @RequestParam(defaultValue = "ACTIVE") BookStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            bookService.saveCategory(id, name, description, status, currentUserService.requireCurrentUser());
            redirectAttributes.addFlashAttribute("success", "Đã lưu thể loại");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/books/{bookId}/copies")
    public String copies(@PathVariable UUID bookId, Model model) {
        Book book = bookService.requireById(bookId);
        BookCopyForm form = new BookCopyForm();
        form.setBookId(bookId);
        form.setCondition(BookCondition.NEW);
        form.setStatus(BookCopyStatus.AVAILABLE);
        model.addAttribute("book", book);
        model.addAttribute("copies", bookService.copiesOf(bookId));
        model.addAttribute("bookCopyForm", form);
        model.addAttribute("conditions", BookCondition.values());
        model.addAttribute("copyStatuses", BookCopyStatus.values());
        return "admin/book-copies";
    }

    @PostMapping("/book-copies/save")
    public String saveCopy(@RequestParam(required = false) UUID id,
                           @Valid @ModelAttribute BookCopyForm bookCopyForm,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu cuốn sách không hợp lệ");
            return "redirect:/admin/books/" + bookCopyForm.getBookId() + "/copies";
        }
        try {
            bookService.saveCopy(id, bookCopyForm, currentUserService.requireCurrentUser());
            redirectAttributes.addFlashAttribute("success", "Đã lưu cuốn sách vật lý");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/books/" + bookCopyForm.getBookId() + "/copies";
    }

    private void addBookOptions(Model model) {
        model.addAttribute("categories", bookService.allCategories());
        model.addAttribute("statuses", BookStatus.values());
    }
}
