package vn.edu.fpt.bookstore.controller.admin;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.bookstore.dto.VoucherForm;
import vn.edu.fpt.bookstore.entity.Voucher;
import vn.edu.fpt.bookstore.entity.enums.VoucherType;
import vn.edu.fpt.bookstore.repository.VoucherRepository;
import vn.edu.fpt.bookstore.service.CurrentUserService;
import vn.edu.fpt.bookstore.service.VoucherService;

import java.util.UUID;

@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {
    private final VoucherService voucherService;
    private final VoucherRepository voucherRepository;
    private final CurrentUserService currentUserService;

    public AdminVoucherController(VoucherService voucherService,
                                  VoucherRepository voucherRepository,
                                  CurrentUserService currentUserService) {
        this.voucherService = voucherService;
        this.voucherRepository = voucherRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("vouchers", voucherService.all());
        return "admin/vouchers";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("voucherForm", new VoucherForm());
        model.addAttribute("types", VoucherType.values());
        return "admin/voucher-form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
        VoucherForm form = new VoucherForm();
        form.setCode(voucher.getCode());
        form.setName(voucher.getName());
        form.setType(voucher.getType());
        form.setDiscountValue(voucher.getDiscountValue());
        form.setMinimumOrderAmount(voucher.getMinimumOrderAmount());
        form.setMaximumDiscount(voucher.getMaximumDiscount());
        form.setStartDate(voucher.getStartDate());
        form.setEndDate(voucher.getEndDate());
        form.setQuantity(voucher.getQuantity());
        form.setPerUserLimit(voucher.getPerUserLimit());
        form.setActive(voucher.isActive());
        model.addAttribute("voucher", voucher);
        model.addAttribute("voucherForm", form);
        model.addAttribute("types", VoucherType.values());
        return "admin/voucher-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) UUID id,
                       @Valid @ModelAttribute VoucherForm voucherForm,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("types", VoucherType.values());
            if (id != null) model.addAttribute("voucher", voucherRepository.findById(id).orElse(null));
            return "admin/voucher-form";
        }
        try {
            voucherService.save(id, voucherForm, currentUserService.requireCurrentUser());
            redirectAttributes.addFlashAttribute("success", "Đã lưu voucher");
            return "redirect:/admin/vouchers";
        } catch (RuntimeException e) {
            bindingResult.reject("voucher", e.getMessage());
            model.addAttribute("types", VoucherType.values());
            if (id != null) model.addAttribute("voucher", voucherRepository.findById(id).orElse(null));
            return "admin/voucher-form";
        }
    }
}
