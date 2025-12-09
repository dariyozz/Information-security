package infosec.securityimplementations.controller;

import infosec.securityimplementations.dto.ApiResponse;
import infosec.securityimplementations.entity.User;
import infosec.securityimplementations.service.SessionService;
import infosec.securityimplementations.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {

    private final UserService userService;
    private final SessionService sessionService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionService.validateSession(sessionToken).isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        return ResponseEntity.ok(ApiResponse.success("Users retrieved", userService.getAllUsers()));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @PathVariable Long id,
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        Long adminId = sessionService.validateSession(sessionToken).orElse(null);
        if (adminId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        Map<String, Object> result = userService.blockUser(id, adminId);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(ApiResponse.success((String) result.get("message")));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error((String) result.get("message")));
        }
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @PathVariable Long id,
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        Long adminId = sessionService.validateSession(sessionToken).orElse(null);
        if (adminId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        Map<String, Object> result = userService.unblockUser(id, adminId);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(ApiResponse.success((String) result.get("message")));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error((String) result.get("message")));
        }
    }
}
