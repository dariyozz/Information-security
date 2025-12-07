package infosec.securityimplementations.controller;

import infosec.securityimplementations.dto.ApiResponse;
import infosec.securityimplementations.service.RoleService;
import infosec.securityimplementations.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class RoleController {

    private final RoleService roleService;
    private final SessionService sessionService;

    /**
     * Assign a role to a user (admin only)
     */
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @RequestParam Long userId,
            @RequestParam String roleName,
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Long requestingUserId = sessionService.validateSession(sessionToken).orElse(null);
        if (requestingUserId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        Map<String, Object> result = roleService.assignRole(userId, roleName, requestingUserId);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(ApiResponse.success((String) result.get("message")));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    (String) result.get("message")));
        }
    }

    /**
     * Revoke a role from a user (admin only)
     */
    @DeleteMapping("/revoke")
    public ResponseEntity<ApiResponse<Void>> revokeRole(
            @RequestParam Long userId,
            @RequestParam String roleName,
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Long requestingUserId = sessionService.validateSession(sessionToken).orElse(null);
        if (requestingUserId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        Map<String, Object> result = roleService.revokeRole(userId, roleName, requestingUserId);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(ApiResponse.success((String) result.get("message")));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    (String) result.get("message")));
        }
    }

    /**
     * Get roles for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserRoles(
            @PathVariable Long userId,
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        if (sessionService.validateSession(sessionToken).isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        Map<String, Object> result = roleService.getUserRoles(userId);
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved", result));
    }

    /**
     * Get all available roles
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllRoles(
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        if (sessionService.validateSession(sessionToken).isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        Map<String, Object> result = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved", result));
    }
}
