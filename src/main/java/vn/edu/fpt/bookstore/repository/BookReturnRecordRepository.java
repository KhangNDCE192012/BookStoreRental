package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.bookstore.entity.BookReturnRecord;

import java.util.UUID;

public interface BookReturnRecordRepository extends JpaRepository<BookReturnRecord, UUID> {
    boolean existsByRentalOrderDetail_Id(UUID detailId);
}
