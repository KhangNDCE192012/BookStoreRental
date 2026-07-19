package vn.edu.fpt.bookstore.controller.admin;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.bookstore.dto.StaffCreateForm;
import vn.edu.fpt.bookstore.service.CurrentUserService;
import vn.edu.fpt.bookstore.service.UserService;

import java.util.UUID;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public AdminUserController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String users(Model model) {
        model.addAttribute("customers", userService.customers());
        model.addAttribute("staffs", userService.staffs());
        model.addAttribute("staffCreateForm", new StaffCreateForm());
        return "admin/users";
    }

    @PostMapping("/staff")
    public String createStaff(@Valid @ModelAttribute StaffCreateForm staffCreateForm,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu nhân viên không hợp lệ");
            return "redirect:/admin/users";
        }
        try {
            userService.createStaff(staffCreateForm);
            redirectAttributes.addFlashAttribute("success", "Đã tạo tài khoản nhân viên");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle-lock")
    public String toggleLock(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleLock(id, currentUserService.requireCurrentUser());
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái tài khoản");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
