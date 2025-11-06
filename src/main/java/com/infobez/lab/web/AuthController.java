package com.infobez.lab.web;

import com.infobez.lab.dto.RegisterRequest;
import com.infobez.lab.models.User;
import com.infobez.lab.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/register
     * Регистрација на нов корисник
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.registerUser(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Корисникот е успешно регистриран!");
            response.put("username", user.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * POST /api/auth/login
     * Најава на корисник
     * - Автентикација преку AuthenticationManager
     * - Генерирање на session key
     * - Испраќање на JSESSIONID cookie
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> credentials,
            HttpSession session,
            HttpServletResponse response) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            // Автентикација и креирање на сесија
            String sessionId = authService.loginUser(username, password, session);

            // Креирај HttpOnly cookie со session ID
            Cookie sessionCookie = new Cookie("JSESSIONID", sessionId);
            sessionCookie.setHttpOnly(true);  // Заштита од XSS
            sessionCookie.setSecure(true);   // Постави на true за HTTPS
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(24 * 60 * 60); // 24 часа

            response.addCookie(sessionCookie);

            // Земи ги податоците за корисникот
            User user = authService.getUserByUsername(username);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", "Успешна најава!");
            responseBody.put("username", user.getUsername());
            responseBody.put("email", user.getEmail());
            responseBody.put("sessionId", sessionId);

            return ResponseEntity.ok(responseBody);

        } catch (RuntimeException e) {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", false);
            responseBody.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
        }
    }

    /**
     * POST /api/auth/logout
     * Одјава на корисник
     * - Уништување на сесија
     * - Бришење на JSESSIONID cookie
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        try {
            // Уништи ја сесијата
            authService.logoutUser(session);

            // Избриши го session cookie
            Cookie sessionCookie = new Cookie("JSESSIONID", null);
            sessionCookie.setHttpOnly(true);
            sessionCookie.setSecure(false);
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(0); // Избриши веднаш

            response.addCookie(sessionCookie);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", "Успешна одјава!");

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", false);
            responseBody.put("message", "Грешка при одјава!");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }

    /**
     * GET /api/auth/session
     * Проверка на активна сесија
     */
    @GetMapping("/session")
    public ResponseEntity<?> checkSession(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (authService.isSessionActive(session)) {
            response.put("success", true);
            response.put("message", "Сесијата е активна");
            response.put("sessionId", session.getId());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Сесијата не е активна");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * POST /api/auth/change-password
     * Промена на лозинка (бара автентикација)
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            authService.changePassword(username, oldPassword, newPassword);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Лозинката е успешно променета!");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}