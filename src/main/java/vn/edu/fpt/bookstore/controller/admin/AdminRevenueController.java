package vn.edu.fpt.bookstore.controller.admin;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.bookstore.service.RevenueService;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/revenue")
public class AdminRevenueController {
    private final RevenueService revenueService;

    public AdminRevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping
    public String revenue(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                          Model model) {
        model.addAttribute("summary", revenueService.summarize(from, to));
        model.addAttribute("from", from == null ? LocalDate.now().withDayOfMonth(1) : from);
        model.addAttribute("to", to == null ? LocalDate.now() : to);
        model.addAttribute("adminView", true);
        return "staff/revenue";
    }
}
