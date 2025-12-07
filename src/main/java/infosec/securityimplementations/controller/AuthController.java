package infosec.securityimplementations.controller;

import infosec.securityimplementations.dto.*;
import infosec.securityimplementations.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller - manual implementation without Spring Security
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Register a new user
     * Manual validation and password hashing
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @Valid @RequestBody RegisterRequest request) {

        Map<String, Object> result = authenticationService.register(request);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(ApiResponse.success(
                    (String) result.get("message"),
                    result));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    (String) result.get("message")));
        }
    }

    /**
     * Verify email with code
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {

        Map<String, Object> result = authenticationService.verifyEmail(request);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(ApiResponse.success((String) result.get("message")));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    (String) result.get("message")));
        }
    }

    /**
     * Resend verification code
     */
    @PostMapping("/resend-code")
    public ResponseEntity<ApiResponse<Void>> resendCode(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email is required"));
        }

        Map<String, Object> result = authenticationService.resendVerificationCode(email);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(ApiResponse.success((String) result.get("message")));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    (String) result.get("message")));
        }
    }

    /**
     * Login - Step 1: Verify password
     * Manual credential verification
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody LoginRequest request) {

        Map<String, Object> result = authenticationService.login(request);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(ApiResponse.success(
                    (String) result.get("message"),
                    result));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    (String) result.get("message")));
        }
    }

    /**
     * Login - Step 2: Verify 2FA code
     * Manual 2FA verification and session creation
     */
    @PostMapping("/verify-2fa")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verify2FA(
            @Valid @RequestBody Verify2FARequest request,
            HttpServletResponse response) {

        Map<String, Object> result = authenticationService.verify2FA(request);

        if ((boolean) result.get("success")) {
            // Set HTTP-only cookie with session token
            String sessionToken = (String) result.get("sessionToken");
            Cookie cookie = new Cookie("SESSION_TOKEN", sessionToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(30 * 60); // 30 minutes
            response.addCookie(cookie);

            return ResponseEntity.ok(ApiResponse.success(
                    (String) result.get("message"),
                    result));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    (String) result.get("message")));
        }
    }

    /**
     * Logout - invalidate session
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken,
            HttpServletResponse response) {

        if (sessionToken != null) {
            authenticationService.logout(sessionToken);

            // Clear cookie
            Cookie cookie = new Cookie("SESSION_TOKEN", "");
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    /**
     * Get current user info
     * Requires valid session
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Map<String, Object> result = authenticationService.getCurrentUser(sessionToken);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(ApiResponse.success("User info retrieved", result));
        } else {
            return ResponseEntity.status(401).body(ApiResponse.error(
                    (String) result.get("message")));
        }
    }
}
