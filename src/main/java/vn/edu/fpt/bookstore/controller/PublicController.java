package vn.edu.fpt.bookstore.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.bookstore.entity.Book;
import vn.edu.fpt.bookstore.service.BookService;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
public class PublicController {

    private final BookService bookService;

    public PublicController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Truy cập localhost:8080 sẽ chuyển sang /home.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    /**
     * Trang chủ dành cho Guest và Customer.
     */
    @GetMapping("/home")
    public String home(Model model) {

        model.addAttribute(
                "books",
                bookService.search(
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        8
                ).getContent()
        );

        model.addAttribute(
                "categories",
                bookService.activeCategories()
        );

        return "public/index";
    }

    /**
     * Danh sách sách dành cho Guest và Customer.
     */
    @GetMapping("/books")
    public String books(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Book> result = bookService.search(
                keyword,
                categoryId,
                author,
                minPrice,
                maxPrice,
                page,
                12
        );

        model.addAttribute("page", result);
        model.addAttribute("books", result.getContent());
        model.addAttribute(
                "categories",
                bookService.activeCategories()
        );

        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("author", author);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "public/books";
    }

    /**
     * Chi tiết sách dành cho Guest và Customer.
     */
    @GetMapping("/books/{id}")
    public String detail(
            @PathVariable UUID id,
            Model model) {

        Book book = bookService.requireActive(id);

        model.addAttribute("book", book);
        model.addAttribute(
                "availableCopies",
                bookService.availableCopies(id)
        );

        return "public/book-detail";
    }
}