package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.bookstore.entity.RentalOrder;
import vn.edu.fpt.bookstore.entity.enums.RentalStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RentalOrderRepository extends JpaRepository<RentalOrder, UUID> {
    List<RentalOrder> findAllByUser_IdOrderByCreatedAtDesc(UUID userId);
    List<RentalOrder> findAllByRentalStatusOrderByCreatedAtAsc(RentalStatus status);
    List<RentalOrder> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"details", "details.book", "details.bookCopy", "user", "voucher"})
    Optional<RentalOrder> findWithDetailsById(UUID id);

    @Query("select r from RentalOrder r where r.rentalStatus in :statuses and r.dueDate < :now")
    List<RentalOrder> findOverdueCandidates(@Param("statuses") List<RentalStatus> statuses, @Param("now") LocalDateTime now);
}
