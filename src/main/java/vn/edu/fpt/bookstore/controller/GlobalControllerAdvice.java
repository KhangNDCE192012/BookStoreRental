package vn.edu.fpt.bookstore.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.repository.UserRepository;

@ControllerAdvice
public class GlobalControllerAdvice {
    private final UserRepository userRepository;

    public GlobalControllerAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("currentUser")
    public User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsernameIgnoreCase(authentication.getName()).orElse(null);
    }
}
