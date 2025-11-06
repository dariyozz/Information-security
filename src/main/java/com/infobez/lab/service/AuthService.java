package com.infobez.lab.service;

import com.infobez.lab.dto.RegisterRequest;
import com.infobez.lab.models.User;
import com.infobez.lab.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.security.core.context.SecurityContextHolder.createEmptyContext;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final VerificationService verificationService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       VerificationService verificationService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.verificationService = verificationService;
        this.emailService = emailService;
    }

    /**
     * ФАЗ 1: Регистрација на нов корисник (се испраќа верификациски код)
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
        user.setEnabled(false); // Не е овозможен се додека не се верификува
        user.setTwoFactorEnabled(true);

        // Генерирај верификациски код
        String verificationCode = verificationService.generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(verificationService.generateExpiryTime(10)); // 10 минути

        User savedUser = userRepository.save(user);

        // Испрати верификациски код на email
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);

        return savedUser;
    }

    /**
     * ФАЗ 2: Верификација на email со код
     */
    public void verifyEmail(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Корисникот не постои!"));

        // Провери дали кодот е истечен
        if (verificationService.isCodeExpired(user.getVerificationCodeExpiry())) {
            throw new RuntimeException("Верификацискиот код е истечен!");
        }

        // Провери дали кодот е точен
        if (!code.equals(user.getVerificationCode())) {
            throw new RuntimeException("Погрешен верификациски код!");
        }

        // Овозможи го корисникот
        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);

        // Испрати welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
    }

    /**
     * Повторно испраќање на верификациски код
     */
    public void resendVerificationCode(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Корисникот не постои!"));

        if (user.isEnabled()) {
            throw new RuntimeException("Корисникот веќе е верификуван!");
        }

        // Генерирај нов код
        String verificationCode = verificationService.generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(verificationService.generateExpiryTime(10));
        userRepository.save(user);

        // Испрати нов код
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);
    }

    /**
     * ФАЗ 1 НАЈАВА: Првична автентикација (username + password)
     * Се испраќа 2FA код на email
     */
    public void initiateLogin(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Корисникот не постои!"));

        // Провери дали корисникот е верификуван
        if (!user.isEnabled()) {
            throw new RuntimeException("Акаунтот не е верификуван! Провери го твојот email.");
        }

        // Провери лозинка
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Погрешна лозинка!");
        }

        // Ако 2FA е овозможена, генерирај и испрати код
        if (user.isTwoFactorEnabled()) {
            String twoFactorCode = verificationService.generateVerificationCode();
            user.setTwoFactorCode(twoFactorCode);
            user.setTwoFactorCodeExpiry(verificationService.generateExpiryTime(5)); // 5 минути
            userRepository.save(user);

            // Испрати 2FA код
            emailService.sendTwoFactorCode(user.getEmail(), twoFactorCode);
        }
    }

    /**
     * ФАЗ 2 НАЈАВА: Верификација на 2FA кодот и креирање на сесија
     */
    public String completeTwoFactorLogin(String username, String twoFactorCode, HttpSession session) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Корисникот не постои!"));

        // Провери дали 2FA кодот е истечен
        if (verificationService.isCodeExpired(user.getTwoFactorCodeExpiry())) {
            throw new RuntimeException("2FA кодот е истечен! Најави се повторно.");
        }

        // Провери дали кодот е точен
        if (!twoFactorCode.equals(user.getTwoFactorCode())) {
            throw new RuntimeException("Погрешен 2FA код!");
        }

        // Избриши го 2FA кодот
        user.setTwoFactorCode(null);
        user.setTwoFactorCodeExpiry(null);
        userRepository.save(user);

        // Креирај автентикација и сесија
        return createSession(username, session);
    }

    /**
     * Креирање на сесија (internal helper)
     */
    private String createSession(String username, HttpSession session) {
        // Build a principal & authorities. For simple cases, empty authorities are fine
        var authorities = List.<GrantedAuthority>of();
        var principal = new org.springframework.security.core.userdetails.User(username, "", authorities);

        // 3-arg constructor -> authenticated = true
        var authToken = new UsernamePasswordAuthenticationToken(
                principal, null, authorities
        );

        var securityContext = createEmptyContext();
        securityContext.setAuthentication(authToken);
        SecurityContextHolder.setContext(securityContext);

        // Persist SecurityContext in session
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext
        );

        return session.getId();
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

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Старата лозинка е погрешна!");
        }

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

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Погрешна лозинка!");
        }

        userRepository.delete(user);
    }
}