package vn.edu.fpt.bookstore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.entity.Wallet;
import vn.edu.fpt.bookstore.entity.WalletTransaction;
import vn.edu.fpt.bookstore.entity.enums.WalletTransactionType;
import vn.edu.fpt.bookstore.repository.WalletRepository;
import vn.edu.fpt.bookstore.repository.WalletTransactionRepository;
import vn.edu.fpt.bookstore.successfullyDat.MoneyUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Wallet getOrCreate(User user) {
        return walletRepository.findByUser_Id(user.getId()).orElseGet(() -> {
            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setBalance(BigDecimal.ZERO);
            return walletRepository.save(wallet);
        });
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> transactions(User user) {
        return transactionRepository.findTop100ByWallet_User_IdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public void topUp(User user, BigDecimal amount) {
        BigDecimal normalized = MoneyUtils.normalize(amount);
        if (normalized.compareTo(BigDecimal.valueOf(10000)) < 0 || normalized.compareTo(BigDecimal.valueOf(100_000_000)) > 0) {
            throw new IllegalArgumentException("Số tiền nạp phải từ 10.000 đến 100.000.000 đồng");
        }
        credit(user, normalized, WalletTransactionType.TOP_UP, "TOPUP", null, "Nạp tiền mô phỏng vào ví");
    }

    @Transactional
    public void debit(User user, BigDecimal amount, WalletTransactionType type, String referenceType, String referenceId, String description) {
        BigDecimal normalized = MoneyUtils.normalize(amount);
        if (normalized.signum() < 0) throw new IllegalArgumentException("Số tiền trừ không hợp lệ");
        Wallet wallet = walletRepository.findByUserIdForUpdate(user.getId())
                .orElseThrow(() -> new IllegalStateException("Người dùng chưa có ví"));
        if (wallet.getBalance().compareTo(normalized) < 0) {
            throw new IllegalStateException("Số dư ví không đủ. Cần " + normalized.toPlainString() + " đồng");
        }
        wallet.setBalance(MoneyUtils.normalize(wallet.getBalance().subtract(normalized)));
        walletRepository.save(wallet);
        saveTransaction(wallet, normalized.negate(), type, referenceType, referenceId, description);
    }

    @Transactional
    public void credit(User user, BigDecimal amount, WalletTransactionType type, String referenceType, String referenceId, String description) {
        BigDecimal normalized = MoneyUtils.normalize(amount);
        if (normalized.signum() < 0) throw new IllegalArgumentException("Số tiền cộng không hợp lệ");
        Wallet wallet = walletRepository.findByUserIdForUpdate(user.getId()).orElseGet(() -> {
            Wallet created = new Wallet();
            created.setUser(user);
            created.setBalance(BigDecimal.ZERO);
            return walletRepository.save(created);
        });
        wallet.setBalance(MoneyUtils.normalize(wallet.getBalance().add(normalized)));
        walletRepository.save(wallet);
        saveTransaction(wallet, normalized, type, referenceType, referenceId, description);
    }

    private void saveTransaction(Wallet wallet, BigDecimal signedAmount, WalletTransactionType type,
                                 String referenceType, String referenceId, String description) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(MoneyUtils.normalize(signedAmount));
        transaction.setBalanceAfter(wallet.getBalance());
        transaction.setType(type);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }
}
