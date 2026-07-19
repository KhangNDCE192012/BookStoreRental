package vn.edu.fpt.bookstore.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.bookstore.dto.CheckoutForm;
import vn.edu.fpt.bookstore.entity.*;
import vn.edu.fpt.bookstore.entity.enums.*;
import vn.edu.fpt.bookstore.repository.*;
import vn.edu.fpt.bookstore.successfullyDat.MoneyUtils;
import vn.edu.fpt.bookstore.successfullyDat.OrderCodeGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseOrderService {
    private static final BigDecimal SHIPPING_FEE = BigDecimal.valueOf(30000);

    private final PurchaseOrderRepository orderRepository;
    private final BookCopyRepository bookCopyRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;
    private final UserService userService;
    private final WalletService walletService;
    private final VoucherService voucherService;
    private final ActivityLogService activityLogService;

    public PurchaseOrderService(PurchaseOrderRepository orderRepository,
                                BookCopyRepository bookCopyRepository,
                                PaymentRepository paymentRepository,
                                CartService cartService,
                                UserService userService,
                                WalletService walletService,
                                VoucherService voucherService,
                                ActivityLogService activityLogService) {
        this.orderRepository = orderRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.paymentRepository = paymentRepository;
        this.cartService = cartService;
        this.userService = userService;
        this.walletService = walletService;
        this.voucherService = voucherService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public PurchaseOrder checkout(User user, CheckoutForm form) {
        List<CartItem> items = cartService.itemsByType(user, CartItemType.PURCHASE);
        if (items.isEmpty()) throw new IllegalArgumentException("Giỏ mua sách đang trống");
        Address address = userService.requireAddress(user, form.getAddressId());

        BigDecimal subtotal = items.stream()
                .map(item -> item.getBook().getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        VoucherService.VoucherResult voucherResult = voucherService.consume(form.getVoucherCode(), user, subtotal);
        BigDecimal total = MoneyUtils.nonNegative(subtotal.add(SHIPPING_FEE).subtract(voucherResult.discount()));

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderCode(OrderCodeGenerator.purchaseCode());
        order.setUser(user);
        order.setVoucher(voucherResult.voucher());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setShippingAddress(address.getFullAddress());
        order.setShippingFee(SHIPPING_FEE);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(voucherResult.discount());
        order.setTotalAmount(total);
        order.setPaymentMethod(PaymentMethod.WALLET);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderStatus(OrderStatus.PENDING);

        for (CartItem item : items) {
            List<BookCopy> copies = bookCopyRepository.findAvailableCopiesForUpdate(
                    item.getBook().getId(), BookCopyStatus.AVAILABLE, PageRequest.of(0, item.getQuantity()));
            if (copies.size() < item.getQuantity()) {
                throw new IllegalStateException("Sách '" + item.getBook().getTitle() + "' vừa hết hàng. Vui lòng cập nhật giỏ");
            }
            for (BookCopy copy : copies) {
                copy.setStatus(BookCopyStatus.RESERVED);
                bookCopyRepository.save(copy);
                PurchaseOrderDetail detail = new PurchaseOrderDetail();
                detail.setBook(item.getBook());
                detail.setBookCopy(copy);
                detail.setUnitPrice(item.getBook().getPurchasePrice());
                detail.setQuantity(1);
                order.addDetail(detail);
            }
        }

        orderRepository.save(order);
        walletService.debit(user, total, WalletTransactionType.PURCHASE_PAYMENT,
                "PURCHASE_ORDER", order.getId().toString(), "Thanh toán đơn mua " + order.getOrderCode());
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        orderRepository.save(order);

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setPurchaseOrder(order);
        payment.setAmount(total);
        payment.setPaymentMethod(PaymentMethod.WALLET);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setProviderReference("WALLET-" + order.getOrderCode());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        cartService.clearByType(user, CartItemType.PURCHASE);
        activityLogService.log(user, "CREATE_PURCHASE_ORDER", "PurchaseOrder", order.getId().toString(), order.getOrderCode());
        return order;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> customerOrders(User user) {
        return orderRepository.findAllByUser_IdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public PurchaseOrder requireCustomerOrder(User user, UUID id) {
        PurchaseOrder order = orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mua"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền xem đơn mua này");
        }
        return order;
    }

    @Transactional(readOnly = true)
    public PurchaseOrder requireById(UUID id) {
        return orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mua"));
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> allOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void cancelByCustomer(User user, UUID orderId, String reason) {
        PurchaseOrder order = requireCustomerOrder(user, orderId);
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ được hủy đơn khi nhân viên chưa xử lý");
        }
        releaseAndRefund(order, OrderStatus.CANCELLED, user, reason == null ? "Khách hàng hủy" : reason);
    }

    @Transactional
    public void approve(User staff, UUID orderId) {
        PurchaseOrder order = requireById(orderId);
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Đơn mua đã được xử lý");
        }
        for (PurchaseOrderDetail detail : order.getDetails()) {
            if (detail.getBookCopy().getStatus() != BookCopyStatus.RESERVED) {
                throw new IllegalStateException("Trạng thái cuốn sách không hợp lệ để bán");
            }
            detail.getBookCopy().setStatus(BookCopyStatus.SOLD);
            bookCopyRepository.save(detail.getBookCopy());
        }
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setProcessedBy(staff);
        orderRepository.save(order);
        activityLogService.log(staff, "APPROVE_PURCHASE_ORDER", "PurchaseOrder", order.getId().toString(), order.getOrderCode());
    }

    @Transactional
    public void complete(User staff, UUID orderId) {
        PurchaseOrder order = requireById(orderId);
        if (order.getOrderStatus() != OrderStatus.CONFIRMED && order.getOrderStatus() != OrderStatus.SHIPPING) {
            throw new IllegalStateException("Đơn mua chưa ở trạng thái có thể hoàn tất");
        }
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        order.setProcessedBy(staff);
        orderRepository.save(order);
        activityLogService.log(staff, "COMPLETE_PURCHASE_ORDER", "PurchaseOrder", order.getId().toString(), order.getOrderCode());
    }

    @Transactional
    public void reject(User staff, UUID orderId, String reason) {
        PurchaseOrder order = requireById(orderId);
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Đơn mua đã được xử lý");
        }
        releaseAndRefund(order, OrderStatus.REJECTED, staff, reason == null ? "Nhân viên từ chối" : reason);
    }

    private void releaseAndRefund(PurchaseOrder order, OrderStatus finalStatus, User actor, String reason) {
        for (PurchaseOrderDetail detail : order.getDetails()) {
            BookCopy copy = detail.getBookCopy();
            if (copy.getStatus() == BookCopyStatus.RESERVED) {
                copy.setStatus(BookCopyStatus.AVAILABLE);
                bookCopyRepository.save(copy);
            }
        }
        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            walletService.credit(order.getUser(), order.getTotalAmount(), WalletTransactionType.REFUND,
                    "PURCHASE_ORDER", order.getId().toString(), "Hoàn tiền đơn mua " + order.getOrderCode());
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            paymentRepository.findByPurchaseOrder_Id(order.getId()).ifPresent(payment -> {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            });
        }
        voucherService.restore(order.getVoucher(), order.getUser());
        order.setOrderStatus(finalStatus);
        order.setCancelReason(reason);
        order.setProcessedBy(actor);
        orderRepository.save(order);
        activityLogService.log(actor, finalStatus.name() + "_PURCHASE_ORDER", "PurchaseOrder", order.getId().toString(), reason);
    }
}
