package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.entity.enums.RoleName;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findAllByRole_NameOrderByCreatedAtDesc(RoleName roleName);
}
