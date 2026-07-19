package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.bookstore.entity.Category;
import vn.edu.fpt.bookstore.entity.enums.BookStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByStatusOrderByNameAsc(BookStatus status);
    Optional<Category> findByNameIgnoreCase(String name);
}
