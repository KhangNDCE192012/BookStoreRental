package vn.edu.fpt.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.bookstore.entity.WalletTransaction;

import java.util.List;
import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    List<WalletTransaction> findTop100ByWallet_User_IdOrderByCreatedAtDesc(UUID userId);
}
