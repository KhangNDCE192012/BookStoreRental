package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.bookstore.entity.Payment;
import vn.edu.fpt.bookstore.entity.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByPurchaseOrder_Id(UUID orderId);
    Optional<Payment> findByRentalOrder_Id(UUID orderId);

    @EntityGraph(attributePaths = {"purchaseOrder", "rentalOrder", "user"})
    List<Payment> findAllByPaymentStatusAndPaidAtBetweenOrderByPaidAtDesc(
            PaymentStatus status,
            LocalDateTime from,
            LocalDateTime to
    );
}
