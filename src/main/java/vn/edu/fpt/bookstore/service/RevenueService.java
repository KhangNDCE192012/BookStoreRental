package vn.edu.fpt.bookstore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.bookstore.entity.Payment;
import vn.edu.fpt.bookstore.entity.enums.PaymentStatus;
import vn.edu.fpt.bookstore.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RevenueService {
    public record RevenueSummary(BigDecimal totalRevenue, BigDecimal purchaseRevenue,
                                 BigDecimal rentalRevenue, int paymentCount, List<Payment> payments) {
    }

    private final PaymentRepository paymentRepository;

    public RevenueService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public RevenueSummary summarize(LocalDate fromDate, LocalDate toDate) {
        LocalDate from = fromDate == null ? LocalDate.now().withDayOfMonth(1) : fromDate;
        LocalDate to = toDate == null ? LocalDate.now() : toDate;
        LocalDateTime fromTime = from.atStartOfDay();
        LocalDateTime toTime = to.plusDays(1).atStartOfDay().minusNanos(1);
        List<Payment> payments = paymentRepository.findAllByPaymentStatusAndPaidAtBetweenOrderByPaidAtDesc(
                PaymentStatus.SUCCESS, fromTime, toTime);
        BigDecimal purchase = payments.stream().filter(p -> p.getPurchaseOrder() != null)
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rental = payments.stream().filter(p -> p.getRentalOrder() != null)
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RevenueSummary(purchase.add(rental), purchase, rental, payments.size(), payments);
    }
}
