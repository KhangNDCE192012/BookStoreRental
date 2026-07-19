package vn.edu.fpt.bookstore.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.bookstore.entity.BookCopy;
import vn.edu.fpt.bookstore.entity.enums.BookCopyStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookCopyRepository extends JpaRepository<BookCopy, UUID> {
    List<BookCopy> findAllByBook_IdOrderByCopyCodeAsc(UUID bookId);
    long countByBook_IdAndStatus(UUID bookId, BookCopyStatus status);
    Optional<BookCopy> findByCopyCodeIgnoreCase(String copyCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select bc from BookCopy bc where bc.book.id = :bookId and bc.status = :status order by bc.createdAt asc")
    List<BookCopy> findAvailableCopiesForUpdate(@Param("bookId") UUID bookId,
                                                @Param("status") BookCopyStatus status,
                                                Pageable pageable);
}
