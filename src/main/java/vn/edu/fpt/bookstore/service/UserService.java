package vn.edu.fpt.bookstore.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.bookstore.dto.AddressForm;
import vn.edu.fpt.bookstore.dto.ProfileForm;
import vn.edu.fpt.bookstore.dto.StaffCreateForm;
import vn.edu.fpt.bookstore.entity.*;
import vn.edu.fpt.bookstore.entity.enums.RoleName;
import vn.edu.fpt.bookstore.entity.enums.UserStatus;
import vn.edu.fpt.bookstore.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final WalletRepository walletRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       AddressRepository addressRepository,
                       StaffProfileRepository staffProfileRepository,
                       WalletRepository walletRepository,
                       CartRepository cartRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.addressRepository = addressRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.walletRepository = walletRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User requireById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
    }

    @Transactional
    public void updateProfile(User user, ProfileForm form) {
        userRepository.findByEmailIgnoreCase(form.getEmail().trim())
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> { throw new IllegalArgumentException("Email đã được sử dụng"); });
        user.setFullName(form.getFullName().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPhone(form.getPhone() == null || form.getPhone().isBlank() ? null : form.getPhone().trim());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<Address> addressesOf(User user) {
        return addressRepository.findAllByUser_IdAndActiveTrueOrderByDefaultAddressDescCreatedAtDesc(user.getId());
    }

    @Transactional
    public Address saveAddress(User user, AddressForm form) {
        if (form.isDefaultAddress()) {
            for (Address existing : addressesOf(user)) {
                existing.setDefaultAddress(false);
                addressRepository.save(existing);
            }
        }
        Address address = new Address();
        address.setUser(user);
        address.setReceiverName(form.getReceiverName().trim());
        address.setReceiverPhone(form.getReceiverPhone().trim());
        address.setAddressLine(form.getAddressLine().trim());
        address.setProvince(form.getProvince().trim());
        address.setDistrict(form.getDistrict().trim());
        address.setWard(form.getWard().trim());
        address.setDefaultAddress(form.isDefaultAddress() || addressesOf(user).isEmpty());
        address.setActive(true);
        return addressRepository.save(address);
    }

    @Transactional
    public void deactivateAddress(User user, UUID addressId) {
        Address address = addressRepository.findByIdAndUser_IdAndActiveTrue(addressId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ"));
        address.setActive(false);
        address.setDefaultAddress(false);
        addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public Address requireAddress(User user, UUID addressId) {
        return addressRepository.findByIdAndUser_IdAndActiveTrue(addressId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Địa chỉ giao nhận không hợp lệ"));
    }

    @Transactional(readOnly = true)
    public List<User> customers() {
        return userRepository.findAllByRole_NameOrderByCreatedAtDesc(RoleName.CUSTOMER);
    }

    @Transactional(readOnly = true)
    public List<User> staffs() {
        return userRepository.findAllByRole_NameOrderByCreatedAtDesc(RoleName.STAFF);
    }

    @Transactional
    public User createStaff(StaffCreateForm form) {
        if (userRepository.existsByUsernameIgnoreCase(form.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (staffProfileRepository.existsByEmployeeCodeIgnoreCase(form.getEmployeeCode())) {
            throw new IllegalArgumentException("Mã nhân viên đã tồn tại");
        }
        Role role = roleRepository.findByName(RoleName.STAFF)
                .orElseThrow(() -> new IllegalStateException("Database chưa có role STAFF"));
        User user = new User();
        user.setUsername(form.getUsername().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setFullName(form.getFullName().trim());
        user.setPhone(form.getPhone());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        StaffProfile profile = new StaffProfile();
        profile.setUser(user);
        profile.setEmployeeCode(form.getEmployeeCode().trim().toUpperCase());
        profile.setPosition(form.getPosition());
        profile.setHireDate(LocalDate.now());
        staffProfileRepository.save(profile);
        return user;
    }

    @Transactional
    public void toggleLock(UUID userId, User admin) {
        User target = requireById(userId);
        if (target.getId().equals(admin.getId())) {
            throw new IllegalArgumentException("Bạn không thể khóa chính mình");
        }
        if (target.getRole().getName() == RoleName.ADMIN) {
            throw new IllegalArgumentException("Không được thay đổi trạng thái tài khoản Admin bằng chức năng này");
        }
        target.setStatus(target.getStatus() == UserStatus.ACTIVE ? UserStatus.LOCKED : UserStatus.ACTIVE);
        userRepository.save(target);
    }

    @Transactional
    public void ensureWalletAndCart(User user) {
        if (walletRepository.findByUser_Id(user.getId()).isEmpty()) {
            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setBalance(BigDecimal.ZERO);
            walletRepository.save(wallet);
        }
        if (cartRepository.findByUser_Id(user.getId()).isEmpty()) {
            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }
    }
}
