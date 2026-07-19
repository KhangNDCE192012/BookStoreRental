package vn.edu.fpt.bookstore.controller.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.entity.enums.CartItemType;
import vn.edu.fpt.bookstore.service.CartService;
import vn.edu.fpt.bookstore.service.CurrentUserService;
import vn.edu.fpt.bookstore.service.UserService;

import java.util.UUID;

@Controller
@RequestMapping("/customer/cart")
public class CartController {
    private final CurrentUserService currentUserService;
    private final CartService cartService;
    private final UserService userService;

    public CartController(CurrentUserService currentUserService, CartService cartService, UserService userService) {
        this.currentUserService = currentUserService;
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public String cart(Model model) {
        User user = currentUserService.requireCurrentUser();
        model.addAttribute("cart", cartService.getOrCreate(user));
        model.addAttribute("purchaseSubtotal", cartService.purchaseSubtotal(user));
        model.addAttribute("rentalSubtotal", cartService.rentalSubtotal(user));
        model.addAttribute("addresses", userService.addressesOf(user));
        return "customer/cart";
    }

    @PostMapping("/add/{bookId}")
    public String add(@PathVariable UUID bookId,
                      @RequestParam CartItemType type,
                      @RequestParam(defaultValue = "1") int quantity,
                      @RequestParam(required = false) Integer rentalDays,
                      RedirectAttributes redirectAttributes) {
        try {
            cartService.add(currentUserService.requireCurrentUser(), bookId, type, quantity, rentalDays);
            redirectAttributes.addFlashAttribute("success", "Đã thêm sách vào giỏ");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/cart";
    }

    @PostMapping("/items/{itemId}/update")
    public String update(@PathVariable UUID itemId,
                         @RequestParam int quantity,
                         @RequestParam(required = false) Integer rentalDays,
                         RedirectAttributes redirectAttributes) {
        try {
            cartService.update(currentUserService.requireCurrentUser(), itemId, quantity, rentalDays);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật giỏ hàng");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/cart";
    }

    @PostMapping("/items/{itemId}/remove")
    public String remove(@PathVariable UUID itemId, RedirectAttributes redirectAttributes) {
        try {
            cartService.remove(currentUserService.requireCurrentUser(), itemId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/cart";
    }
}
