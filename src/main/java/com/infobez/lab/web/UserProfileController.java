package com.infobez.lab.web;

import com.infobez.lab.models.User;
import com.infobez.lab.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * Protected Controller - Пристап само за логирани корисници
 * Сите endpoints овде бараат активна сесија
 */
@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private final AuthService authService;

    public UserProfileController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * GET /api/user/profile
     * Добивање на профил на тековниот корисник
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            // Земи го тековно автентицираниот корисник
            String username = getCurrentUsername();
            User user = authService.getUserByUsername(username);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Корисникот не е пронајден."));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("enabled", user.isEnabled());
            response.put("userId", user.getId());

            return ResponseEntity.ok(response);

        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("success", false, "message", ex.getReason() != null ? ex.getReason() : "Грешка"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Немате пристап!"));
        }
    }

    /**
     * PUT /api/user/profile
     * Ажурирање на email адреса
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request) {
        try {
            String username = getCurrentUsername();
            String newEmail = request != null ? request.get("email") : null;

            if (newEmail == null || newEmail.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Email е задолжителен."));
            }
            newEmail = newEmail.trim();
            if (!isValidEmail(newEmail)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Невалидна email адреса."));
            }

            User user = authService.getUserByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Корисникот не е пронајден."));
            }

            // Ако email е ист како тековниот, нема потреба од ажурирање
            if (newEmail.equalsIgnoreCase(user.getEmail())) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Нема промени — email е веќе поставен.",
                        "email", user.getEmail()
                ));
            }

            // Провери дали новиот email веќе постои
            User existingUser = authService.getUserByEmail(newEmail);
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Email веќе е зафатен!"));
            }

            authService.updateUserEmail(username, newEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Профилот е успешно ажуриран!");
            response.put("email", newEmail);

            return ResponseEntity.ok(response);

        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("success", false, "message", ex.getReason() != null ? ex.getReason() : "Грешка"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Неуспешно ажурирање на профилот."));
        }
    }

    /**
     * GET /api/user/dashboard
     * Dashboard со информации за корисникот
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        try {
            String username = getCurrentUsername();
            User user = authService.getUserByUsername(username);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Корисникот не е пронајден."));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Добредојде " + username + "!");
            response.put("userInfo", Map.of(
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "accountStatus", user.isEnabled() ? "Активен" : "Неактивен"
            ));
            response.put("stats", Map.of(
                    "loginCount", "15", // Пример - може да се имплементира вистинско броење
                    "lastLogin", "2024-03-15",
                    "accountCreated", "2024-01-10"
            ));

            return ResponseEntity.ok(response);

        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("success", false, "message", ex.getReason() != null ? ex.getReason() : "Грешка"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Немате пристап!"));
        }
    }

    /**
     * DELETE /api/user/account
     * Бришење на сопствениот акаунт
     */
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(@RequestBody Map<String, String> request) {
        try {
            String username = getCurrentUsername();
            String password = request != null ? request.get("password") : null;

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Лозинката е задолжителна."));
            }

            // Потврди ја лозинката пред бришење
            authService.deleteUserAccount(username, password);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Акаунтот е успешно избришан!");

            return ResponseEntity.ok(response);

        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("success", false, "message", ex.getReason() != null ? ex.getReason() : "Грешка"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Бришењето не успеа."));
        }
    }

    /**
     * GET /api/user/settings
     * Корисничко подесувања
     */
    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        try {
            String username = getCurrentUsername();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("username", username);
            response.put("settings", Map.of(
                    "language", "mk",
                    "notifications", true,
                    "theme", "dark"
            ));

            return ResponseEntity.ok(response);

        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("success", false, "message", ex.getReason() != null ? ex.getReason() : "Грешка"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Немате пристап!"));
        }
    }

    /**
     * Helper метод - земање на username од Security Context
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Не сте најавени!");
        }

        return authentication.getName();
    }

    // Едноставна валидација за email
    private boolean isValidEmail(String email) {
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < email.length() - 1;
    }
}