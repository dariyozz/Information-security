package com.infobez.lab.service;

import com.infobez.lab.dto.RegisterRequest;
import com.infobez.lab.models.User;
import com.infobez.lab.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Регистрација на нов корисник
     */
    public User registerUser(RegisterRequest request) {
        // Провери дали username веќе постои
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username веќе постои!");
        }

        // Провери дали email веќе постои
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email веќе е регистриран!");
        }

        // Креирај нов корисник со хеширана лозинка
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        return userRepository.save(user);
    }

    /**
     * Најава на корисник со Spring Security AuthenticationManager
     * Креира сесија и ја зачувува во HttpSession
     */
    public String loginUser(String username, String password, HttpSession session) {
        try {
            // Креирај authentication token
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            // Автентикација преку AuthenticationManager
            // Ова автоматски го повикува CustomUserDetailsService.loadUserByUsername()
            // и ја проверува лозинката
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Постави го authentication во Security Context
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            // Зачувај го Security Context во HTTP сесијата
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    securityContext
            );

            // Врати го session ID
            return session.getId();

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Погрешно корисничко име или лозинка!");
        }
    }

    /**
     * Одјава - уништување на сесија
     */
    public void logoutUser(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.invalidate();
    }

    /**
     * Промени лозинка
     */
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Корисникот не постои!"));

        // Провери стара лозинка
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Старата лозинка е погрешна!");
        }

        // Постави нова лозинка (хеширана)
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Земи корисник по username
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Корисникот не постои!"));
    }

    /**
     * Проверка дали сесијата е активна
     */
    public boolean isSessionActive(HttpSession session) {
        try {
            Object context = session.getAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
            );
            return context != null;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Земи корисник по email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Ажурирање на email адреса
     */
    public void updateUserEmail(String username, String newEmail) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Корисникот не постои!"));

        // Провери дали email веќе постои
        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Email веќе е зафатен!");
        }

        user.setEmail(newEmail);
        userRepository.save(user);
    }

    /**
     * Бришење на корисничкиот акаунт
     */
    public void deleteUserAccount(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Корисникот не постои!"));

        // Потврди ја лозинката
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Погрешна лозинка!");
        }

        userRepository.delete(user);
    }
}