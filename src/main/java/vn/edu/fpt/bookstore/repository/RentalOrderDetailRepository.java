package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.bookstore.entity.RentalOrderDetail;

import java.util.Optional;
import java.util.UUID;

public interface RentalOrderDetailRepository extends JpaRepository<RentalOrderDetail, UUID> {
    Optional<RentalOrderDetail> findByIdAndRentalOrder_User_Id(UUID id, UUID userId);
}
