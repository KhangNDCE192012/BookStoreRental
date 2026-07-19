package vn.edu.fpt.bookstore.controller.customer;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.bookstore.dto.CheckoutForm;
import vn.edu.fpt.bookstore.entity.PurchaseOrder;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.service.CurrentUserService;
import vn.edu.fpt.bookstore.service.PurchaseOrderService;

import java.util.UUID;

@Controller
@RequestMapping("/customer/purchases")
public class PurchaseOrderController {
    private final CurrentUserService currentUserService;
    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(CurrentUserService currentUserService, PurchaseOrderService purchaseOrderService) {
        this.currentUserService = currentUserService;
        this.purchaseOrderService = purchaseOrderService;
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
            PurchaseOrder order = purchaseOrderService.checkout(currentUserService.requireCurrentUser(), checkoutForm);
            redirectAttributes.addFlashAttribute("success", "Tạo đơn mua thành công: " + order.getOrderCode());
            return "redirect:/customer/purchases/" + order.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/cart";
        }
    }

    @GetMapping
    public String history(Model model) {
        User user = currentUserService.requireCurrentUser();
        model.addAttribute("orders", purchaseOrderService.customerOrders(user));
        return "customer/purchase-history";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        model.addAttribute("order", purchaseOrderService.requireCustomerOrder(currentUserService.requireCurrentUser(), id));
        return "customer/purchase-detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable UUID id,
                         @RequestParam(required = false) String reason,
                         RedirectAttributes redirectAttributes) {
        try {
            purchaseOrderService.cancelByCustomer(currentUserService.requireCurrentUser(), id, reason);
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn và hoàn tiền về ví");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/purchases/" + id;
    }
}
