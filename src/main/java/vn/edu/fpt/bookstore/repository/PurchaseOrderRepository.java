package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.bookstore.entity.PurchaseOrder;
import vn.edu.fpt.bookstore.entity.enums.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    List<PurchaseOrder> findAllByUser_IdOrderByCreatedAtDesc(UUID userId);
    List<PurchaseOrder> findAllByOrderStatusOrderByCreatedAtAsc(OrderStatus status);
    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"details", "details.book", "details.bookCopy", "user", "voucher"})
    Optional<PurchaseOrder> findWithDetailsById(UUID id);
}
