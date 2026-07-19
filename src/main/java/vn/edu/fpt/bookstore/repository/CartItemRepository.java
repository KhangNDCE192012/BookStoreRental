package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.bookstore.entity.CartItem;
import vn.edu.fpt.bookstore.entity.enums.CartItemType;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByCart_IdAndBook_IdAndItemType(UUID cartId, UUID bookId, CartItemType itemType);
    Optional<CartItem> findByIdAndCart_User_Id(UUID id, UUID userId);
}
