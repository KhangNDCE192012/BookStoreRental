package vn.edu.fpt.bookstore.controller.customer;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.bookstore.dto.AddressForm;
import vn.edu.fpt.bookstore.dto.ChangePasswordForm;
import vn.edu.fpt.bookstore.dto.ProfileForm;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.service.AuthService;
import vn.edu.fpt.bookstore.service.CurrentUserService;
import vn.edu.fpt.bookstore.service.UserService;

import java.util.UUID;

@Controller
@RequestMapping("/customer/profile")
public class ProfileController {
    private final CurrentUserService currentUserService;
    private final UserService userService;
    private final AuthService authService;

    public ProfileController(CurrentUserService currentUserService, UserService userService, AuthService authService) {
        this.currentUserService = currentUserService;
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    public String profile(Model model) {
        User user = currentUserService.requireCurrentUser();
        ProfileForm profileForm = new ProfileForm();
        profileForm.setFullName(user.getFullName());
        profileForm.setEmail(user.getEmail());
        profileForm.setPhone(user.getPhone());
        model.addAttribute("user", user);
        model.addAttribute("profileForm", profileForm);
        model.addAttribute("addressForm", new AddressForm());
        model.addAttribute("addresses", userService.addressesOf(user));
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "customer/profile";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute ProfileForm profileForm,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu hồ sơ không hợp lệ");
            return "redirect:/customer/profile";
        }
        try {
            userService.updateProfile(currentUserService.requireCurrentUser(), profileForm);
            redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/profile";
    }

    @PostMapping("/addresses")
    public String addAddress(@Valid @ModelAttribute AddressForm addressForm,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu địa chỉ không hợp lệ");
            return "redirect:/customer/profile";
        }
        try {
            userService.saveAddress(currentUserService.requireCurrentUser(), addressForm);
            redirectAttributes.addFlashAttribute("success", "Đã thêm địa chỉ");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/profile";
    }

    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            userService.deactivateAddress(currentUserService.requireCurrentUser(), id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa địa chỉ");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute ChangePasswordForm changePasswordForm,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới chưa đúng yêu cầu");
            return "redirect:/customer/profile";
        }
        try {
            authService.changePassword(currentUserService.requireCurrentUser(), changePasswordForm);
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/profile";
    }
}
