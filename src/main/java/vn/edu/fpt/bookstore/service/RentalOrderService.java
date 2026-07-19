package vn.edu.fpt.bookstore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.bookstore.dto.CheckoutForm;
import vn.edu.fpt.bookstore.dto.ReturnProcessForm;
import vn.edu.fpt.bookstore.entity.*;
import vn.edu.fpt.bookstore.entity.enums.*;
import vn.edu.fpt.bookstore.repository.*;
import vn.edu.fpt.bookstore.successfullyDat.MoneyUtils;
import vn.edu.fpt.bookstore.successfullyDat.OrderCodeGenerator;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class RentalOrderService {
    private final RentalOrderRepository rentalOrderRepository;
    private final RentalOrderDetailRepository detailRepository;
    private final BookCopyRepository bookCopyRepository;
    private final PaymentRepository paymentRepository;
    private final BookReturnRecordRepository returnRecordRepository;
    private final BookExtensionRequestRepository extensionRequestRepository;
    private final CartService cartService;
    private final UserService userService;
    private final WalletService walletService;
    private final VoucherService voucherService;
    private final ActivityLogService activityLogService;
    private final BigDecimal lateFeePerDay;
    private final int maxExtensionDays;

    public RentalOrderService(RentalOrderRepository rentalOrderRepository,
                              RentalOrderDetailRepository detailRepository,
                              BookCopyRepository bookCopyRepository,
                              PaymentRepository paymentRepository,
                              BookReturnRecordRepository returnRecordRepository,
                              BookExtensionRequestRepository extensionRequestRepository,
                              CartService cartService,
                              UserService userService,
                              WalletService walletService,
                              VoucherService voucherService,
                              ActivityLogService activityLogService,
                              @Value("${app.rental.late-fee-per-day:10000}") BigDecimal lateFeePerDay,
                              @Value("${app.rental.max-extension-days:14}") int maxExtensionDays) {
        this.rentalOrderRepository = rentalOrderRepository;
        this.detailRepository = detailRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.paymentRepository = paymentRepository;
        this.returnRecordRepository = returnRecordRepository;
        this.extensionRequestRepository = extensionRequestRepository;
        this.cartService = cartService;
        this.userService = userService;
        this.walletService = walletService;
        this.voucherService = voucherService;
        this.activityLogService = activityLogService;
        this.lateFeePerDay = lateFeePerDay;
        this.maxExtensionDays = maxExtensionDays;
    }

    @Transactional
    public RentalOrder checkout(User user, CheckoutForm form) {
        List<CartItem> items = cartService.itemsByType(user, CartItemType.RENTAL);
        if (items.isEmpty()) throw new IllegalArgumentException("Giỏ thuê sách đang trống");
        Address address = userService.requireAddress(user, form.getAddressId());

        BigDecimal rentalFee = items.stream()
                .map(item -> item.getBook().getRentalPricePerDay()
                        .multiply(BigDecimal.valueOf(item.getRentalDays()))
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deposit = items.stream()
                .map(item -> item.getBook().getRentalDeposit().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int maxDays = items.stream().map(CartItem::getRentalDays).max(Integer::compareTo).orElse(1);
        VoucherService.VoucherResult voucherResult = voucherService.consume(form.getVoucherCode(), user, rentalFee);
        BigDecimal total = MoneyUtils.nonNegative(rentalFee.subtract(voucherResult.discount()).add(deposit));

        RentalOrder order = new RentalOrder();
        order.setRentalCode(OrderCodeGenerator.rentalCode());
        order.setUser(user);
        order.setVoucher(voucherResult.voucher());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setShippingAddress(address.getFullAddress());
        order.setRentalDays(maxDays);
        order.setRentalFee(rentalFee);
        order.setDepositAmount(deposit);
        order.setDiscountAmount(voucherResult.discount());
        order.setTotalAmount(total);
        order.setPaymentMethod(PaymentMethod.WALLET);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setRentalStatus(RentalStatus.PENDING);

        for (CartItem item : items) {
            List<BookCopy> copies = bookCopyRepository.findAvailableCopiesForUpdate(
                    item.getBook().getId(), BookCopyStatus.AVAILABLE, PageRequest.of(0, item.getQuantity()));
            if (copies.size() < item.getQuantity()) {
                throw new IllegalStateException("Sách '" + item.getBook().getTitle() + "' vừa hết bản cho thuê");
            }
            for (BookCopy copy : copies) {
                copy.setStatus(BookCopyStatus.RESERVED);
                bookCopyRepository.save(copy);
                RentalOrderDetail detail = new RentalOrderDetail();
                detail.setBook(item.getBook());
                detail.setBookCopy(copy);
                detail.setRentalDays(item.getRentalDays());
                detail.setRentalFee(item.getBook().getRentalPricePerDay().multiply(BigDecimal.valueOf(item.getRentalDays())));
                detail.setDepositAmount(item.getBook().getRentalDeposit());
                order.addDetail(detail);
            }
        }

        rentalOrderRepository.save(order);
        walletService.debit(user, total, WalletTransactionType.RENTAL_PAYMENT,
                "RENTAL_ORDER", order.getId().toString(), "Thanh toán đơn thuê " + order.getRentalCode());
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        rentalOrderRepository.save(order);

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setRentalOrder(order);
        payment.setAmount(total);
        payment.setPaymentMethod(PaymentMethod.WALLET);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setProviderReference("WALLET-" + order.getRentalCode());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        cartService.clearByType(user, CartItemType.RENTAL);
        activityLogService.log(user, "CREATE_RENTAL_ORDER", "RentalOrder", order.getId().toString(), order.getRentalCode());
        return order;
    }

    @Transactional(readOnly = true)
    public List<RentalOrder> customerOrders(User user) {
        return rentalOrderRepository.findAllByUser_IdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public List<BookExtensionRequest> customerExtensionRequests(User user) {
        return extensionRequestRepository.findAllByRentalOrderDetail_RentalOrder_User_IdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public RentalOrder requireCustomerOrder(User user, UUID id) {
        RentalOrder order = requireById(id);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền xem đơn thuê này");
        }
        return order;
    }

    @Transactional(readOnly = true)
    public RentalOrder requireById(UUID id) {
        return rentalOrderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn thuê"));
    }

    @Transactional(readOnly = true)
    public List<RentalOrder> allOrders() {
        return rentalOrderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void cancelByCustomer(User user, UUID orderId, String reason) {
        RentalOrder order = requireCustomerOrder(user, orderId);
        if (order.getRentalStatus() != RentalStatus.PENDING) {
            throw new IllegalStateException("Chỉ được hủy đơn thuê khi nhân viên chưa xử lý");
        }
        releaseAndRefund(order, RentalStatus.CANCELLED, user, reason == null ? "Khách hàng hủy" : reason);
    }

    @Transactional
    public void approve(User staff, UUID orderId) {
        RentalOrder order = requireById(orderId);
        if (order.getRentalStatus() != RentalStatus.PENDING) throw new IllegalStateException("Đơn thuê đã được xử lý");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime latestDue = now;
        for (RentalOrderDetail detail : order.getDetails()) {
            BookCopy copy = detail.getBookCopy();
            if (copy.getStatus() != BookCopyStatus.RESERVED) throw new IllegalStateException("Cuốn sách không còn được giữ cho đơn thuê");
            copy.setStatus(BookCopyStatus.RENTED);
            bookCopyRepository.save(copy);
            detail.setRentalDate(now);
            detail.setDueDate(now.plusDays(detail.getRentalDays()));
            detailRepository.save(detail);
            if (detail.getDueDate().isAfter(latestDue)) latestDue = detail.getDueDate();
        }
        order.setRentalDate(now);
        order.setDueDate(latestDue);
        order.setRentalStatus(RentalStatus.RENTING);
        order.setProcessedBy(staff);
        rentalOrderRepository.save(order);
        activityLogService.log(staff, "APPROVE_RENTAL_ORDER", "RentalOrder", order.getId().toString(), order.getRentalCode());
    }

    @Transactional
    public void reject(User staff, UUID orderId, String reason) {
        RentalOrder order = requireById(orderId);
        if (order.getRentalStatus() != RentalStatus.PENDING) throw new IllegalStateException("Đơn thuê đã được xử lý");
        releaseAndRefund(order, RentalStatus.REJECTED, staff, reason == null ? "Nhân viên từ chối" : reason);
    }

    @Transactional
    public void requestReturn(User user, UUID orderId) {
        RentalOrder order = requireCustomerOrder(user, orderId);
        if (order.getRentalStatus() != RentalStatus.RENTING && order.getRentalStatus() != RentalStatus.OVERDUE) {
            throw new IllegalStateException("Đơn thuê chưa ở trạng thái có thể yêu cầu trả");
        }
        order.setRentalStatus(RentalStatus.RETURN_REQUESTED);
        rentalOrderRepository.save(order);
        activityLogService.log(user, "REQUEST_RETURN", "RentalOrder", order.getId().toString(), order.getRentalCode());
    }

    @Transactional
    public BookExtensionRequest requestExtension(User user, UUID detailId, int extraDays, String reason) {
        if (extraDays < 1 || extraDays > maxExtensionDays) {
            throw new IllegalArgumentException("Số ngày gia hạn phải từ 1 đến " + maxExtensionDays);
        }
        RentalOrderDetail detail = detailRepository.findByIdAndRentalOrder_User_Id(detailId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách đang thuê"));
        RentalOrder order = detail.getRentalOrder();
        if (order.getRentalStatus() != RentalStatus.RENTING || detail.getDueDate() == null || detail.getDueDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Sách đã quá hạn hoặc không ở trạng thái được gia hạn");
        }
        if (extensionRequestRepository.existsByRentalOrderDetail_IdAndStatus(detailId, ExtensionStatus.PENDING)) {
            throw new IllegalStateException("Sách này đã có yêu cầu gia hạn đang chờ xử lý");
        }
        BookExtensionRequest request = new BookExtensionRequest();
        request.setRentalOrderDetail(detail);
        request.setExtraDays(extraDays);
        request.setRequestedDueDate(detail.getDueDate().plusDays(extraDays));
        request.setExtraFee(detail.getBook().getRentalPricePerDay().multiply(BigDecimal.valueOf(extraDays)));
        request.setReason(reason);
        request.setStatus(ExtensionStatus.PENDING);
        extensionRequestRepository.save(request);
        activityLogService.log(user, "REQUEST_EXTENSION", "BookExtensionRequest", request.getId().toString(), "Extra days: " + extraDays);
        return request;
    }

    @Transactional(readOnly = true)
    public List<BookExtensionRequest> pendingExtensions() {
        return extensionRequestRepository.findAllByStatusOrderByCreatedAtAsc(ExtensionStatus.PENDING);
    }

    @Transactional
    public void processExtension(User staff, UUID requestId, boolean approve, String note) {
        BookExtensionRequest request = extensionRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu gia hạn"));
        if (request.getStatus() != ExtensionStatus.PENDING) throw new IllegalStateException("Yêu cầu đã được xử lý");
        if (approve) {
            RentalOrderDetail detail = request.getRentalOrderDetail();
            RentalOrder order = detail.getRentalOrder();
            if (order.getRentalStatus() != RentalStatus.RENTING) throw new IllegalStateException("Đơn thuê không còn được gia hạn");
            walletService.debit(order.getUser(), request.getExtraFee(), WalletTransactionType.EXTENSION_FEE,
                    "EXTENSION_REQUEST", request.getId().toString(), "Phí gia hạn sách " + detail.getBook().getTitle());
            detail.setDueDate(request.getRequestedDueDate());
            detail.setRentalDays(detail.getRentalDays() + request.getExtraDays());
            detail.setRentalFee(detail.getRentalFee().add(request.getExtraFee()));
            detailRepository.save(detail);
            order.setRentalFee(order.getRentalFee().add(request.getExtraFee()));
            order.setTotalAmount(order.getTotalAmount().add(request.getExtraFee()));
            LocalDateTime latestDue = order.getDetails().stream().map(RentalOrderDetail::getDueDate)
                    .filter(d -> d != null).max(LocalDateTime::compareTo).orElse(order.getDueDate());
            order.setDueDate(latestDue);
            rentalOrderRepository.save(order);
            request.setStatus(ExtensionStatus.APPROVED);
        } else {
            request.setStatus(ExtensionStatus.REJECTED);
        }
        request.setProcessedBy(staff);
        request.setProcessedAt(LocalDateTime.now());
        request.setStaffNote(note);
        extensionRequestRepository.save(request);
        activityLogService.log(staff, approve ? "APPROVE_EXTENSION" : "REJECT_EXTENSION", "BookExtensionRequest", request.getId().toString(), note);
    }

    @Transactional
    public void processReturn(User staff, UUID detailId, ReturnProcessForm form) {
        RentalOrderDetail detail = detailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết thuê"));
        RentalOrder order = requireById(detail.getRentalOrder().getId());
        if (order.getRentalStatus() != RentalStatus.RETURN_REQUESTED && order.getRentalStatus() != RentalStatus.OVERDUE && order.getRentalStatus() != RentalStatus.RENTING) {
            throw new IllegalStateException("Đơn thuê chưa ở trạng thái xử lý trả sách");
        }
        if (returnRecordRepository.existsByRentalOrderDetail_Id(detailId)) {
            throw new IllegalStateException("Cuốn sách này đã được ghi nhận trả");
        }
        LocalDateTime now = LocalDateTime.now();
        int lateDays = detail.getDueDate() != null && now.isAfter(detail.getDueDate())
                ? (int) Math.max(1, ChronoUnit.DAYS.between(detail.getDueDate().toLocalDate(), now.toLocalDate())) : 0;
        BigDecimal lateFee = lateFeePerDay.multiply(BigDecimal.valueOf(lateDays));
        BigDecimal damageFee = form.getDamageFee() == null ? BigDecimal.ZERO : MoneyUtils.nonNegative(form.getDamageFee());
        BigDecimal lostFee = BigDecimal.ZERO;
        BookCopy copy = detail.getBookCopy();
        BookCondition before = copy.getCondition();

        switch (form.getConditionAfter()) {
            case GOOD -> {
                copy.setCondition(BookCondition.GOOD);
                copy.setStatus(BookCopyStatus.AVAILABLE);
                damageFee = BigDecimal.ZERO;
            }
            case FAIR -> {
                copy.setCondition(BookCondition.FAIR);
                copy.setStatus(BookCopyStatus.AVAILABLE);
            }
            case DAMAGED -> {
                copy.setCondition(BookCondition.DAMAGED);
                copy.setStatus(BookCopyStatus.DAMAGED);
            }
            case LOST -> {
                copy.setStatus(BookCopyStatus.LOST);
                lostFee = detail.getBook().getPurchasePrice();
            }
        }
        bookCopyRepository.save(copy);
        BigDecimal penalties = lateFee.add(damageFee).add(lostFee);
        BigDecimal depositRefund = detail.getDepositAmount().subtract(penalties).max(BigDecimal.ZERO);
        BigDecimal extraCharge = penalties.subtract(detail.getDepositAmount()).max(BigDecimal.ZERO);
        if (extraCharge.signum() > 0) {
            walletService.debit(order.getUser(), extraCharge,
                    form.getConditionAfter() == ReturnCondition.LOST ? WalletTransactionType.LOST_FEE : WalletTransactionType.DAMAGE_FEE,
                    "RENTAL_DETAIL", detail.getId().toString(), "Phí phát sinh khi trả sách " + detail.getBook().getTitle());
        }
        if (depositRefund.signum() > 0) {
            walletService.credit(order.getUser(), depositRefund, WalletTransactionType.DEPOSIT_REFUND,
                    "RENTAL_DETAIL", detail.getId().toString(), "Hoàn cọc sách " + detail.getBook().getTitle());
        }

        detail.setReturnDate(now);
        detail.setLateFee(lateFee);
        detail.setDamageFee(damageFee.add(lostFee));
        detailRepository.save(detail);

        BookReturnRecord record = new BookReturnRecord();
        record.setRentalOrderDetail(detail);
        record.setReturnedAt(now);
        record.setConditionBefore(before);
        record.setConditionAfter(form.getConditionAfter());
        record.setLateDays(lateDays);
        record.setLateFee(lateFee);
        record.setDamageFee(damageFee);
        record.setLostFee(lostFee);
        record.setDepositRefund(depositRefund);
        record.setNotes(form.getNotes());
        record.setProcessedBy(staff);
        returnRecordRepository.save(record);

        boolean allReturned = order.getDetails().stream().allMatch(d -> d.getId().equals(detailId) || returnRecordRepository.existsByRentalOrderDetail_Id(d.getId()));
        if (allReturned) {
            order.setReturnDate(now);
            order.setLateFee(order.getDetails().stream().map(RentalOrderDetail::getLateFee).reduce(BigDecimal.ZERO, BigDecimal::add));
            order.setDamageFee(order.getDetails().stream().map(RentalOrderDetail::getDamageFee).reduce(BigDecimal.ZERO, BigDecimal::add));
            boolean anyLost = order.getDetails().stream().anyMatch(d -> d.getBookCopy().getStatus() == BookCopyStatus.LOST);
            order.setRentalStatus(anyLost ? RentalStatus.LOST : RentalStatus.RETURNED);
            order.setProcessedBy(staff);
            rentalOrderRepository.save(order);
        }
        activityLogService.log(staff, "PROCESS_BOOK_RETURN", "RentalOrderDetail", detail.getId().toString(), form.getConditionAfter().name());
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void markOverdueOrders() {
        List<RentalOrder> overdue = rentalOrderRepository.findOverdueCandidates(
                List.of(RentalStatus.RENTING), LocalDateTime.now());
        for (RentalOrder order : overdue) {
            order.setRentalStatus(RentalStatus.OVERDUE);
            rentalOrderRepository.save(order);
        }
    }

    private void releaseAndRefund(RentalOrder order, RentalStatus finalStatus, User actor, String reason) {
        for (RentalOrderDetail detail : order.getDetails()) {
            BookCopy copy = detail.getBookCopy();
            if (copy.getStatus() == BookCopyStatus.RESERVED) {
                copy.setStatus(BookCopyStatus.AVAILABLE);
                bookCopyRepository.save(copy);
            }
        }
        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            walletService.credit(order.getUser(), order.getTotalAmount(), WalletTransactionType.REFUND,
                    "RENTAL_ORDER", order.getId().toString(), "Hoàn tiền đơn thuê " + order.getRentalCode());
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            paymentRepository.findByRentalOrder_Id(order.getId()).ifPresent(payment -> {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            });
        }
        voucherService.restore(order.getVoucher(), order.getUser());
        order.setRentalStatus(finalStatus);
        order.setCancelReason(reason);
        order.setProcessedBy(actor);
        rentalOrderRepository.save(order);
        activityLogService.log(actor, finalStatus.name() + "_RENTAL_ORDER", "RentalOrder", order.getId().toString(), reason);
    }
}
