package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.bookstore.entity.UserVoucher;

import java.util.Optional;
import java.util.UUID;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, UUID> {
    Optional<UserVoucher> findByUser_IdAndVoucher_Id(UUID userId, UUID voucherId);
}
