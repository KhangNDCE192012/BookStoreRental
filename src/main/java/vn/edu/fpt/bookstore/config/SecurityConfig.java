package vn.edu.fpt.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import vn.edu.fpt.bookstore.service.CustomUserDetailsService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider)
            throws Exception {

        http.authenticationProvider(authenticationProvider);

        http.authorizeHttpRequests(auth -> auth

                // File CSS, JavaScript và hình ảnh
                .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/uploads/**",
                        "/error",
                        "/favicon.ico"
                ).permitAll()

                // Đăng nhập, đăng ký, quên mật khẩu
                .requestMatchers("/auth/**").permitAll()

                /*
                 * Trang công khai:
                 * - Guest được truy cập.
                 * - Customer được truy cập.
                 * - Staff và Admin không được truy cập.
                 */
                .requestMatchers(
                        "/",
                        "/home",
                        "/books",
                        "/books/**"
                ).access(
                        new WebExpressionAuthorizationManager(
                                "isAnonymous() or hasRole('CUSTOMER')"
                        )
                )

                // Customer chỉ được vào khu vực Customer
                .requestMatchers("/customer/**")
                .hasRole("CUSTOMER")

                // Staff chỉ được vào khu vực Staff
                .requestMatchers("/staff/**")
                .hasRole("STAFF")

                // Admin chỉ được vào khu vực Admin
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                .anyRequest()
                .authenticated()
        );

        http.formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("username")
                .passwordParameter("password")

                .successHandler((request, response, authentication) -> {

                    boolean isAdmin =
                            authentication.getAuthorities()
                                    .stream()
                                    .anyMatch(authority ->
                                            authority.getAuthority()
                                                    .equals("ROLE_ADMIN")
                                    );

                    boolean isStaff =
                            authentication.getAuthorities()
                                    .stream()
                                    .anyMatch(authority ->
                                            authority.getAuthority()
                                                    .equals("ROLE_STAFF")
                                    );

                    boolean isCustomer =
                            authentication.getAuthorities()
                                    .stream()
                                    .anyMatch(authority ->
                                            authority.getAuthority()
                                                    .equals("ROLE_CUSTOMER")
                                    );

                    if (isAdmin) {
                        response.sendRedirect("/admin");
                        return;
                    }

                    if (isStaff) {
                        response.sendRedirect("/staff");
                        return;
                    }

                    if (isCustomer) {
                        response.sendRedirect("/home");
                        return;
                    }

                    response.sendRedirect("/auth/login?error");
                })

                .failureUrl("/auth/login?error")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        );

        http.exceptionHandling(exception -> exception
                .accessDeniedPage("/auth/access-denied")
        );

        return http.build();
    }
}