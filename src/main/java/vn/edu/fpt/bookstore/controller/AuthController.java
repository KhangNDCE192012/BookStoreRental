package vn.edu.fpt.bookstore.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.bookstore.dto.RegisterForm;
import vn.edu.fpt.bookstore.dto.ResetPasswordForm;
import vn.edu.fpt.bookstore.service.AuthService;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registerForm")) model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm registerForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(registerForm);

            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công. Bạn có thể đăng nhập ngay.");

            return "redirect:/auth/login";

        } catch (IllegalArgumentException | IllegalStateException e) {

            bindingResult.reject("register.failed", e.getMessage());

            return "auth/register";

        } catch (Exception e) {

            e.printStackTrace();

            bindingResult.reject("register.failed", "Không thể tạo tài khoản. Vui lòng kiểm tra database hoặc console.");

            return "auth/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        try {
            String resetUrl = authService.createResetToken(email);
            model.addAttribute("success", "Đã tạo yêu cầu đặt lại mật khẩu.");
            model.addAttribute("resetUrl", resetUrl);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetForm(@RequestParam String token, Model model) {
        if (!authService.isValidResetToken(token)) {
            model.addAttribute("error", "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn");
            return "auth/reset-password";
        }
        ResetPasswordForm form = new ResetPasswordForm();
        form.setToken(token);
        model.addAttribute("resetPasswordForm", form);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String reset(@Valid @ModelAttribute ResetPasswordForm resetPasswordForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) return "auth/reset-password";
        try {
            authService.resetPassword(resetPasswordForm);
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            bindingResult.reject("reset", e.getMessage());
            return "auth/reset-password";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied(Authentication authentication) {

        if (authentication == null) {
            return "redirect:/auth/login";
        }
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        boolean isStaff = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_STAFF"));
        boolean isCustomer = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_CUSTOMER"));
        if (isAdmin) {
            return "redirect:/admin";
        }
        if (isStaff) {
            return "redirect:/staff";
        }
        if (isCustomer) {
            return "redirect:/home";
        }
        return "redirect:/auth/login";
    }
}
