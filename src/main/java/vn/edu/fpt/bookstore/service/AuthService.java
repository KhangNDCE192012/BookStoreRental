package vn.edu.fpt.bookstore.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.bookstore.dto.ChangePasswordForm;
import vn.edu.fpt.bookstore.dto.RegisterForm;
import vn.edu.fpt.bookstore.dto.ResetPasswordForm;
import vn.edu.fpt.bookstore.entity.*;
import vn.edu.fpt.bookstore.entity.enums.RoleName;
import vn.edu.fpt.bookstore.entity.enums.UserStatus;
import vn.edu.fpt.bookstore.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WalletRepository walletRepository;
    private final CartRepository cartRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String baseUrl;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       WalletRepository walletRepository,
                       CartRepository cartRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       ObjectProvider<JavaMailSender> mailSenderProvider,
                       @Value("${app.mail.enabled:false}") boolean mailEnabled,
                       @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.walletRepository = walletRepository;
        this.cartRepository = cartRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailEnabled = mailEnabled;
        this.baseUrl = baseUrl;
    }

    @Transactional
    public User register(RegisterForm form) {
        String username = form.getUsername().trim();
        String email = form.getEmail().trim().toLowerCase();
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu nhập lại không khớp");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("Database chưa có role CUSTOMER"));

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(form.getFullName().trim());
        user.setPhone(blankToNull(form.getPhone()));
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(customerRole);
        userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);

        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);
        return user;
    }

    @Transactional
    public void changePassword(User user, ChangePasswordForm form) {
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới nhập lại không khớp");
        }
        if (passwordEncoder.matches(form.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public String createResetToken(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email này"));
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        token.setUsed(false);
        passwordResetTokenRepository.save(token);

        String resetUrl = baseUrl + "/auth/reset-password?token=" + token.getToken();
        if (mailEnabled && mailSender != null) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Đặt lại mật khẩu Book Store Rental");
            message.setText("Mở liên kết sau trong 30 phút để đặt lại mật khẩu: " + resetUrl);
            mailSender.send(message);
        }
        return resetUrl;
    }

    @Transactional
    public void resetPassword(ResetPasswordForm form) {
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu nhập lại không khớp");
        }
        PasswordResetToken token = passwordResetTokenRepository.findByToken(form.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Mã đặt lại mật khẩu không hợp lệ"));
        if (token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Mã đặt lại mật khẩu đã hết hạn hoặc đã sử dụng");
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public boolean isValidResetToken(String tokenValue) {
        return passwordResetTokenRepository.findByToken(tokenValue)
                .filter(token -> !token.isUsed())
                .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
