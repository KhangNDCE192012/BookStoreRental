package vn.edu.fpt.bookstore.controller.customer;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.bookstore.dto.CheckoutForm;
import vn.edu.fpt.bookstore.entity.RentalOrder;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.service.CurrentUserService;
import vn.edu.fpt.bookstore.service.RentalOrderService;

import java.util.UUID;

@Controller
@RequestMapping("/customer/rentals")
public class RentalOrderController {
    private final CurrentUserService currentUserService;
    private final RentalOrderService rentalOrderService;

    public RentalOrderController(CurrentUserService currentUserService, RentalOrderService rentalOrderService) {
        this.currentUserService = currentUserService;
        this.rentalOrderService = rentalOrderService;
    }

    @PostMapping("/checkout")
    public String checkout(@Valid CheckoutForm checkoutForm,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn địa chỉ giao nhận");
            return "redirect:/customer/cart";
        }
        try {
            RentalOrder order = rentalOrderService.checkout(currentUserService.requireCurrentUser(), checkoutForm);
            redirectAttributes.addFlashAttribute("success", "Tạo đơn thuê thành công: " + order.getRentalCode());
            return "redirect:/customer/rentals/" + order.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/cart";
        }
    }

    @GetMapping
    public String history(Model model) {
        User user = currentUserService.requireCurrentUser();
        model.addAttribute("orders", rentalOrderService.customerOrders(user));
        model.addAttribute("extensionRequests", rentalOrderService.customerExtensionRequests(user));
        return "customer/rental-history";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        model.addAttribute("order", rentalOrderService.requireCustomerOrder(currentUserService.requireCurrentUser(), id));
        return "customer/rental-detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable UUID id,
                         @RequestParam(required = false) String reason,
                         RedirectAttributes redirectAttributes) {
        try {
            rentalOrderService.cancelByCustomer(currentUserService.requireCurrentUser(), id, reason);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn thuê và hoàn tiền");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/rentals/" + id;
    }

    @PostMapping("/{id}/request-return")
    public String requestReturn(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            rentalOrderService.requestReturn(currentUserService.requireCurrentUser(), id);
            redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu trả sách");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/rentals/" + id;
    }

    @PostMapping("/details/{detailId}/extension")
    public String extension(@PathVariable UUID detailId,
                            @RequestParam int extraDays,
                            @RequestParam(required = false) String reason,
                            @RequestParam UUID orderId,
                            RedirectAttributes redirectAttributes) {
        try {
            rentalOrderService.requestExtension(currentUserService.requireCurrentUser(), detailId, extraDays, reason);
            redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu gia hạn");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/rentals/" + orderId;
    }
}
