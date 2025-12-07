package infosec.securityimplementations.service;

import infosec.securityimplementations.dto.LoginRequest;
import infosec.securityimplementations.dto.RegisterRequest;
import infosec.securityimplementations.dto.Verify2FARequest;
import infosec.securityimplementations.dto.VerifyEmailRequest;
import infosec.securityimplementations.entity.User;
import infosec.securityimplementations.entity.VerificationCode;
import infosec.securityimplementations.entity.Role;
import infosec.securityimplementations.entity.UserRole;
import infosec.securityimplementations.repository.UserRepository;
import infosec.securityimplementations.repository.RoleRepository;
import infosec.securityimplementations.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manual authentication service - no Spring Security auto-configuration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final VerificationService verificationService;
    private final SessionService sessionService;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        Map<String, Object> result = new HashMap<>();

        // Manual validation - check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            result.put("success", false);
            result.put("message", "Username already exists");
            return result;
        }

        // Manual validation - check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            result.put("success", false);
            result.put("message", "Email already exists");
            return result;
        }

        // Manual password hashing using BCrypt
        String hashedPassword = passwordService.hashPassword(request.getPassword());

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("User registered: {} (ID: {})", user.getUsername(), user.getId());

        // Send email verification code
        verificationService.sendEmailVerificationCode(user.getId(), user.getEmail());

        result.put("success", true);
        result.put("message", "Registration successful. Please check your email for verification code.");
        result.put("userId", user.getId());
        result.put("requiresEmailVerification", true);

        return result;
    }

    /**
     * Verify email with code
     */
    @Transactional
    public Map<String, Object> verifyEmail(VerifyEmailRequest request) {
        Map<String, Object> result = new HashMap<>();

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            result.put("success", false);
            result.put("message", "User not found");
            return result;
        }

        User user = optionalUser.get();

        // Validate verification code
        boolean isValid = verificationService.validateCode(
                user.getId(),
                request.getCode(),
                VerificationCode.CodeType.EMAIL_VERIFICATION);

        if (!isValid) {
            result.put("success", false);
            result.put("message", "Invalid or expired verification code");
            return result;
        }

        // Mark email as verified
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified for user: {}", user.getUsername());

        result.put("success", true);
        result.put("message", "Email verified successfully. You can now log in.");

        return result;
    }

    /**
     * Resend verification code
     */
    @Transactional
    public Map<String, Object> resendVerificationCode(String email) {
        Map<String, Object> result = new HashMap<>();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            // detailed message for dev/testing, generic for prod usually
            result.put("success", false);
            result.put("message", "User not found");
            return result;
        }

        User user = optionalUser.get();
        if (user.getEmailVerified()) {
            result.put("success", false);
            result.put("message", "Email is already verified");
            return result;
        }

        verificationService.sendEmailVerificationCode(user.getId(), user.getEmail());
        log.info("Verification code resent to: {}", email);

        result.put("success", true);
        result.put("message", "Verification code sent to your email");
        return result;
    }

    /**
     * Manual login - Step 1: Verify password
     */
    @Transactional
    public Map<String, Object> login(LoginRequest request) {
        Map<String, Object> result = new HashMap<>();

        // Manual credential verification
        Optional<User> optionalUser = userRepository.findByUsername(request.getUsername());
        if (optionalUser.isEmpty()) {
            result.put("success", false);
            result.put("message", "Invalid username or password");
            return result;
        }

        User user = optionalUser.get();

        // Manual password verification
        boolean passwordMatches = passwordService.verifyPassword(
                request.getPassword(),
                user.getPasswordHash());

        if (!passwordMatches) {
            result.put("success", false);
            result.put("message", "Invalid username or password");
            return result;
        }

        // Check if email is verified
        if (!user.getEmailVerified()) {
            result.put("success", false);
            result.put("message", "Please verify your email before logging in");
            return result;
        }

        // Send 2FA code
        verificationService.send2FACode(user.getId(), user.getEmail());
        log.info("2FA code sent to user: {}", user.getUsername());

        result.put("success", true);
        result.put("message", "Password verified. Please enter the 2FA code sent to your email.");
        result.put("requires2FA", true);

        return result;
    }

    /**
     * Manual login - Step 2: Verify 2FA code and create session
     */
    @Transactional
    public Map<String, Object> verify2FA(Verify2FARequest request) {
        Map<String, Object> result = new HashMap<>();

        Optional<User> optionalUser = userRepository.findByUsername(request.getUsername());
        if (optionalUser.isEmpty()) {
            result.put("success", false);
            result.put("message", "User not found");
            return result;
        }

        User user = optionalUser.get();

        // Validate 2FA code
        boolean isValid = verificationService.validateCode(
                user.getId(),
                request.getCode(),
                VerificationCode.CodeType.TWO_FACTOR);

        if (!isValid) {
            result.put("success", false);
            result.put("message", "Invalid or expired 2FA code");
            return result;
        }

        // Create session
        String sessionToken = sessionService.createSession(user.getId());
        log.info("Session created for user: {}", user.getUsername());

        result.put("success", true);
        result.put("message", "Login successful");
        result.put("sessionToken", sessionToken);

        // Fetch roles for login response too
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List<Role> roles = roleRepository.findAllById(roleIds);

        List<Map<String, String>> roleObjects = roles.stream()
                .map(r -> Map.of("name", r.getName()))
                .collect(Collectors.toList());

        result.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "roles", roleObjects));

        return result;
    }

    /**
     * Logout - invalidate session
     */
    @Transactional
    public Map<String, Object> logout(String sessionToken) {
        Map<String, Object> result = new HashMap<>();

        sessionService.invalidateSession(sessionToken);
        log.info("Session invalidated");

        result.put("success", true);
        result.put("message", "Logged out successfully");

        return result;
    }

    /**
     * Get current user info from session
     */
    public Map<String, Object> getCurrentUser(String sessionToken) {
        Map<String, Object> result = new HashMap<>();

        Optional<Long> userIdOpt = sessionService.validateSession(sessionToken);
        if (userIdOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Invalid or expired session");
            return result;
        }

        Optional<User> optionalUser = userRepository.findById(userIdOpt.get());
        if (optionalUser.isEmpty()) {
            result.put("success", false);
            result.put("message", "User not found");
            return result;
        }

        User user = optionalUser.get();

        // Manual role fetching (since User entity doesn't have roles relationship)
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List<Role> roles = roleRepository.findAllById(roleIds);

        List<Map<String, String>> roleObjects = roles.stream()
                .map(r -> Map.of("name", r.getName()))
                .collect(Collectors.toList());

        result.put("success", true);
        result.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "emailVerified", user.getEmailVerified(),
                "roles", roleObjects));

        return result;
    }
}
