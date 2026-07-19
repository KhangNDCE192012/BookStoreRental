package vn.edu.fpt.bookstore.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.fpt.bookstore.service.*;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
    private final BookService bookService;
    private final UserService userService;
    private final PurchaseOrderService purchaseOrderService;
    private final RentalOrderService rentalOrderService;
    private final ActivityLogService activityLogService;

    public AdminDashboardController(BookService bookService,
                                    UserService userService,
                                    PurchaseOrderService purchaseOrderService,
                                    RentalOrderService rentalOrderService,
                                    ActivityLogService activityLogService) {
        this.bookService = bookService;
        this.userService = userService;
        this.purchaseOrderService = purchaseOrderService;
        this.rentalOrderService = rentalOrderService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("bookCount", bookService.allBooks().size());
        model.addAttribute("customerCount", userService.customers().size());
        model.addAttribute("staffCount", userService.staffs().size());
        model.addAttribute("purchaseCount", purchaseOrderService.allOrders().size());
        model.addAttribute("rentalCount", rentalOrderService.allOrders().size());
        model.addAttribute("logs", activityLogService.latest().stream().limit(20).toList());
        return "admin/dashboard";
    }

    @GetMapping("/activity-logs")
    public String logs(Model model) {
        model.addAttribute("logs", activityLogService.latest());
        return "admin/activity-logs";
    }
}
