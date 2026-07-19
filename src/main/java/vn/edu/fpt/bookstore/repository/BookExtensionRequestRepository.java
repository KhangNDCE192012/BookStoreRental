package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.bookstore.entity.BookExtensionRequest;
import vn.edu.fpt.bookstore.entity.enums.ExtensionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookExtensionRequestRepository extends JpaRepository<BookExtensionRequest, UUID> {
    @EntityGraph(attributePaths = {
            "rentalOrderDetail",
            "rentalOrderDetail.book",
            "rentalOrderDetail.bookCopy",
            "rentalOrderDetail.rentalOrder",
            "rentalOrderDetail.rentalOrder.user"
    })
    List<BookExtensionRequest> findAllByStatusOrderByCreatedAtAsc(ExtensionStatus status);

    @EntityGraph(attributePaths = {
            "rentalOrderDetail",
            "rentalOrderDetail.book",
            "rentalOrderDetail.bookCopy",
            "rentalOrderDetail.rentalOrder"
    })
    List<BookExtensionRequest> findAllByRentalOrderDetail_RentalOrder_User_IdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = {
            "rentalOrderDetail",
            "rentalOrderDetail.book",
            "rentalOrderDetail.bookCopy",
            "rentalOrderDetail.rentalOrder"
    })
    Optional<BookExtensionRequest> findByIdAndRentalOrderDetail_RentalOrder_User_Id(UUID id, UUID userId);

    boolean existsByRentalOrderDetail_IdAndStatus(UUID detailId, ExtensionStatus status);
}
