package vn.edu.fpt.bookstore.controller.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.service.CurrentUserService;
import vn.edu.fpt.bookstore.service.WalletService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/customer/wallet")
public class WalletController {
    private final CurrentUserService currentUserService;
    private final WalletService walletService;

    public WalletController(CurrentUserService currentUserService, WalletService walletService) {
        this.currentUserService = currentUserService;
        this.walletService = walletService;
    }

    @GetMapping
    public String wallet(Model model) {
        User user = currentUserService.requireCurrentUser();
        model.addAttribute("wallet", walletService.getOrCreate(user));
        model.addAttribute("transactions", walletService.transactions(user));
        return "customer/wallet";
    }

    @PostMapping("/top-up")
    public String topUp(@RequestParam BigDecimal amount, RedirectAttributes redirectAttributes) {
        try {
            walletService.topUp(currentUserService.requireCurrentUser(), amount);
            redirectAttributes.addFlashAttribute("success", "Nạp tiền mô phỏng thành công");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/wallet";
    }
}
