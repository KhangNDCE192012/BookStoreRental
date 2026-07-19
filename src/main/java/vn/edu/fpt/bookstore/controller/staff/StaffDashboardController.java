package vn.edu.fpt.bookstore.controller.staff;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.bookstore.dto.ReturnProcessForm;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.service.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/staff")
public class StaffDashboardController {
    private final CurrentUserService currentUserService;
    private final PurchaseOrderService purchaseOrderService;
    private final RentalOrderService rentalOrderService;
    private final RevenueService revenueService;

    public StaffDashboardController(CurrentUserService currentUserService,
                                    PurchaseOrderService purchaseOrderService,
                                    RentalOrderService rentalOrderService,
                                    RevenueService revenueService) {
        this.currentUserService = currentUserService;
        this.purchaseOrderService = purchaseOrderService;
        this.rentalOrderService = rentalOrderService;
        this.revenueService = revenueService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("purchaseOrders", purchaseOrderService.allOrders());
        model.addAttribute("rentalOrders", rentalOrderService.allOrders());
        model.addAttribute("extensionRequests", rentalOrderService.pendingExtensions());
        return "staff/dashboard";
    }

    @GetMapping("/purchases/{id}")
    public String purchaseDetail(@PathVariable UUID id, Model model) {
        model.addAttribute("order", purchaseOrderService.requireById(id));
        return "staff/purchase-detail";
    }

    @PostMapping("/purchases/{id}/approve")
    public String approvePurchase(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        return run(() -> purchaseOrderService.approve(currentUserService.requireCurrentUser(), id),
                "Đã xác nhận đơn mua", "/staff/purchases/" + id, redirectAttributes);
    }

    @PostMapping("/purchases/{id}/reject")
    public String rejectPurchase(@PathVariable UUID id,
                                 @RequestParam(required = false) String reason,
                                 RedirectAttributes redirectAttributes) {
        return run(() -> purchaseOrderService.reject(currentUserService.requireCurrentUser(), id, reason),
                "Đã từ chối và hoàn tiền đơn mua", "/staff/purchases/" + id, redirectAttributes);
    }

    @PostMapping("/purchases/{id}/complete")
    public String completePurchase(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        return run(() -> purchaseOrderService.complete(currentUserService.requireCurrentUser(), id),
                "Đã hoàn tất đơn mua", "/staff/purchases/" + id, redirectAttributes);
    }

    @GetMapping("/rentals/{id}")
    public String rentalDetail(@PathVariable UUID id, Model model) {
        model.addAttribute("order", rentalOrderService.requireById(id));
        model.addAttribute("returnProcessForm", new ReturnProcessForm());
        return "staff/rental-detail";
    }

    @PostMapping("/rentals/{id}/approve")
    public String approveRental(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        return run(() -> rentalOrderService.approve(currentUserService.requireCurrentUser(), id),
                "Đã xác nhận giao sách thuê", "/staff/rentals/" + id, redirectAttributes);
    }

    @PostMapping("/rentals/{id}/reject")
    public String rejectRental(@PathVariable UUID id,
                               @RequestParam(required = false) String reason,
                               RedirectAttributes redirectAttributes) {
        return run(() -> rentalOrderService.reject(currentUserService.requireCurrentUser(), id, reason),
                "Đã từ chối và hoàn tiền đơn thuê", "/staff/rentals/" + id, redirectAttributes);
    }

    @PostMapping("/rentals/details/{detailId}/return")
    public String processReturn(@PathVariable UUID detailId,
                                @RequestParam UUID orderId,
                                @Valid @ModelAttribute ReturnProcessForm returnProcessForm,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu trả sách không hợp lệ");
            return "redirect:/staff/rentals/" + orderId;
        }
        try {
            BigDecimal depositRefund = rentalOrderService.processReturn(
                    currentUserService.requireCurrentUser(), detailId, returnProcessForm);
            redirectAttributes.addFlashAttribute("success",
                    "Đã ghi nhận trả sách và hoàn " + depositRefund.toPlainString()
                            + " đ tiền cọc vào ví khách hàng");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/rentals/" + orderId;
    }

    @PostMapping("/extensions/{id}/process")
    public String processExtension(@PathVariable UUID id,
                                   @RequestParam boolean approve,
                                   @RequestParam(required = false) String note,
                                   RedirectAttributes redirectAttributes) {
        return run(() -> rentalOrderService.processExtension(currentUserService.requireCurrentUser(), id, approve, note),
                approve ? "Đã duyệt gia hạn" : "Đã từ chối gia hạn", "/staff", redirectAttributes);
    }

    @GetMapping("/revenue")
    public String revenue(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                          Model model) {
        model.addAttribute("summary", revenueService.summarize(from, to));
        model.addAttribute("from", from == null ? LocalDate.now().withDayOfMonth(1) : from);
        model.addAttribute("to", to == null ? LocalDate.now() : to);
        return "staff/revenue";
    }

    @GetMapping("/revenue/export.csv")
    public ResponseEntity<byte[]> exportRevenue(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        RevenueService.RevenueSummary summary = revenueService.summarize(from, to);
        StringBuilder csv = new StringBuilder("Payment ID,Type,Order Code,Amount,Paid At\n");
        summary.payments().forEach(payment -> {
            String type = payment.getPurchaseOrder() != null ? "PURCHASE" : "RENTAL";
            String code = payment.getPurchaseOrder() != null ? payment.getPurchaseOrder().getOrderCode() : payment.getRentalOrder().getRentalCode();
            csv.append(payment.getId()).append(',').append(type).append(',').append(code).append(',')
                    .append(payment.getAmount()).append(',').append(payment.getPaidAt()).append('\n');
        });
        byte[] data = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=revenue.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(data);
    }

    private String run(Runnable action, String success, String redirect, RedirectAttributes attributes) {
        try {
            action.run();
            attributes.addFlashAttribute("success", success);
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + redirect;
    }
}
