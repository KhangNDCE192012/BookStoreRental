package vn.edu.fpt.bookstore.service;

import org.junit.jupiter.api.Test;
import vn.edu.fpt.bookstore.dto.ReturnProcessForm;
import vn.edu.fpt.bookstore.entity.*;
import vn.edu.fpt.bookstore.entity.enums.*;
import vn.edu.fpt.bookstore.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RentalOrderServiceTest {

    @Test
    void processReturnCreditsFullDepositForOnTimeGoodBook() {
        RentalOrderRepository orderRepository = mock(RentalOrderRepository.class);
        RentalOrderDetailRepository detailRepository = mock(RentalOrderDetailRepository.class);
        BookCopyRepository copyRepository = mock(BookCopyRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        BookReturnRecordRepository returnRecordRepository = mock(BookReturnRecordRepository.class);
        BookExtensionRequestRepository extensionRepository = mock(BookExtensionRequestRepository.class);
        WalletService walletService = mock(WalletService.class);
        ActivityLogService activityLogService = mock(ActivityLogService.class);

        RentalOrderService service = new RentalOrderService(
                orderRepository, detailRepository, copyRepository, paymentRepository,
                returnRecordRepository, extensionRepository, mock(CartService.class),
                mock(UserService.class), walletService, mock(VoucherService.class),
                activityLogService, BigDecimal.valueOf(10000), 14);

        UUID orderId = UUID.randomUUID();
        UUID detailId = UUID.randomUUID();
        User customer = new User();
        customer.setId(UUID.randomUUID());
        User staff = new User();
        staff.setId(UUID.randomUUID());
        Book book = new Book();
        book.setTitle("Sách kiểm thử");
        BookCopy copy = new BookCopy();
        copy.setCondition(BookCondition.GOOD);
        copy.setStatus(BookCopyStatus.RENTED);

        RentalOrder order = new RentalOrder();
        order.setId(orderId);
        order.setUser(customer);
        order.setRentalStatus(RentalStatus.RENTING);

        RentalOrderDetail detail = new RentalOrderDetail();
        detail.setId(detailId);
        detail.setBook(book);
        detail.setBookCopy(copy);
        detail.setDueDate(LocalDateTime.now().plusDays(1));
        detail.setDepositAmount(BigDecimal.valueOf(100000));
        order.addDetail(detail);

        ReturnProcessForm form = new ReturnProcessForm();
        form.setConditionAfter(ReturnCondition.GOOD);
        form.setDamageFee(BigDecimal.ZERO);

        when(detailRepository.findById(detailId)).thenReturn(Optional.of(detail));
        when(orderRepository.findWithDetailsById(orderId)).thenReturn(Optional.of(order));
        when(returnRecordRepository.existsByRentalOrderDetail_Id(detailId)).thenReturn(false);

        BigDecimal refunded = service.processReturn(staff, detailId, form);

        assertEquals(0, BigDecimal.valueOf(100000).compareTo(refunded));
        verify(walletService).credit(eq(customer), eq(BigDecimal.valueOf(100000)),
                eq(WalletTransactionType.DEPOSIT_REFUND), eq("RENTAL_DETAIL"),
                eq(detailId.toString()), eq("Hoàn cọc sách Sách kiểm thử"));
        verify(returnRecordRepository).save(any(BookReturnRecord.class));
        assertEquals(RentalStatus.RETURNED, order.getRentalStatus());
    }
}
