package vn.edu.fpt.bookstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.bookstore.entity.Book;
import vn.edu.fpt.bookstore.entity.enums.BookStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
    Optional<Book> findByIsbnIgnoreCase(String isbn);

    @Query("""
        select b from Book b
        where b.status = :status
          and (:keyword is null or lower(b.title) like lower(concat('%', :keyword, '%'))
               or lower(b.author) like lower(concat('%', :keyword, '%'))
               or lower(b.isbn) like lower(concat('%', :keyword, '%')))
          and (:categoryId is null or b.category.id = :categoryId)
          and (:author is null or lower(b.author) like lower(concat('%', :author, '%')))
          and (:minPrice is null or b.purchasePrice >= :minPrice)
          and (:maxPrice is null or b.purchasePrice <= :maxPrice)
        """)
    Page<Book> search(@Param("status") BookStatus status,
                      @Param("keyword") String keyword,
                      @Param("categoryId") UUID categoryId,
                      @Param("author") String author,
                      @Param("minPrice") BigDecimal minPrice,
                      @Param("maxPrice") BigDecimal maxPrice,
                      Pageable pageable);
}
