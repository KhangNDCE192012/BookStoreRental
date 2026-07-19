package vn.edu.fpt.bookstore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.bookstore.dto.VoucherForm;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.entity.UserVoucher;
import vn.edu.fpt.bookstore.entity.Voucher;
import vn.edu.fpt.bookstore.entity.enums.VoucherType;
import vn.edu.fpt.bookstore.repository.UserVoucherRepository;
import vn.edu.fpt.bookstore.repository.VoucherRepository;
import vn.edu.fpt.bookstore.successfullyDat.MoneyUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VoucherService {
    public record VoucherResult(Voucher voucher, BigDecimal discount) {
    }

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final ActivityLogService activityLogService;

    public VoucherService(VoucherRepository voucherRepository,
                          UserVoucherRepository userVoucherRepository,
                          ActivityLogService activityLogService) {
        this.voucherRepository = voucherRepository;
        this.userVoucherRepository = userVoucherRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public List<Voucher> all() {
        return voucherRepository.findAll();
    }

    @Transactional
    public VoucherResult consume(String code, User user, BigDecimal applicableAmount) {
        if (code == null || code.isBlank()) return new VoucherResult(null, BigDecimal.ZERO);
        Voucher voucher = voucherRepository.findWithLockByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Voucher không tồn tại"));
        LocalDateTime now = LocalDateTime.now();
        if (!voucher.isActive() || voucher.getQuantity() <= 0 || now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            throw new IllegalArgumentException("Voucher đã hết hạn hoặc hết lượt sử dụng");
        }
        if (applicableAmount.compareTo(voucher.getMinimumOrderAmount()) < 0) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu của voucher");
        }
        UserVoucher usage = userVoucherRepository.findByUser_IdAndVoucher_Id(user.getId(), voucher.getId())
                .orElseGet(() -> {
                    UserVoucher created = new UserVoucher();
                    created.setUser(user);
                    created.setVoucher(voucher);
                    created.setUsageCount(0);
                    return created;
                });
        if (usage.getUsageCount() >= voucher.getPerUserLimit()) {
            throw new IllegalArgumentException("Bạn đã sử dụng hết số lần cho phép của voucher này");
        }

        BigDecimal discount;
        if (voucher.getType() == VoucherType.PERCENT) {
            discount = applicableAmount.multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (voucher.getMaximumDiscount() != null) discount = discount.min(voucher.getMaximumDiscount());
        } else {
            discount = voucher.getDiscountValue();
        }
        discount = MoneyUtils.nonNegative(discount.min(applicableAmount));

        voucher.setQuantity(voucher.getQuantity() - 1);
        voucherRepository.save(voucher);
        usage.setUsageCount(usage.getUsageCount() + 1);
        usage.setLastUsedAt(now);
        userVoucherRepository.save(usage);
        return new VoucherResult(voucher, discount);
    }

    @Transactional
    public void restore(Voucher voucher, User user) {
        if (voucher == null) return;
        Voucher locked = voucherRepository.findWithLockByCodeIgnoreCase(voucher.getCode())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy voucher để hoàn lượt"));
        locked.setQuantity(locked.getQuantity() + 1);
        voucherRepository.save(locked);
        userVoucherRepository.findByUser_IdAndVoucher_Id(user.getId(), voucher.getId()).ifPresent(usage -> {
            usage.setUsageCount(Math.max(0, usage.getUsageCount() - 1));
            userVoucherRepository.save(usage);
        });
    }

    @Transactional
    public Voucher save(UUID id, VoucherForm form, User actor) {
        if (form.getEndDate().isBefore(form.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        if (form.getType() == VoucherType.PERCENT && form.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Voucher phần trăm không được vượt quá 100% ");
        }
        voucherRepository.findByCodeIgnoreCase(form.getCode().trim())
                .filter(existing -> id == null || !existing.getId().equals(id))
                .ifPresent(existing -> { throw new IllegalArgumentException("Mã voucher đã tồn tại"); });
        Voucher voucher = id == null ? new Voucher() : voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
        voucher.setCode(form.getCode().trim().toUpperCase());
        voucher.setName(form.getName().trim());
        voucher.setType(form.getType());
        voucher.setDiscountValue(MoneyUtils.normalize(form.getDiscountValue()));
        voucher.setMinimumOrderAmount(MoneyUtils.normalize(form.getMinimumOrderAmount()));
        voucher.setMaximumDiscount(form.getMaximumDiscount() == null ? null : MoneyUtils.normalize(form.getMaximumDiscount()));
        voucher.setStartDate(form.getStartDate());
        voucher.setEndDate(form.getEndDate());
        voucher.setQuantity(form.getQuantity());
        voucher.setPerUserLimit(form.getPerUserLimit());
        voucher.setActive(form.isActive());
        if (voucher.getCreatedBy() == null) voucher.setCreatedBy(actor);
        voucherRepository.save(voucher);
        activityLogService.log(actor, id == null ? "CREATE_VOUCHER" : "UPDATE_VOUCHER", "Voucher", voucher.getId().toString(), voucher.getCode());
        return voucher;
    }
}
