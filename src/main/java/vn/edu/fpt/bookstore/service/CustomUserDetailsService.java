package vn.edu.fpt.bookstore.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.entity.enums.UserStatus;
import vn.edu.fpt.bookstore.repository.UserRepository;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DisabledException("Tài khoản đang bị khóa hoặc ngừng hoạt động");
        }

        String authority = "ROLE_" + user.getRole().getName().name();
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                true,
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}
