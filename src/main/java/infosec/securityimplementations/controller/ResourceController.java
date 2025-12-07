package infosec.securityimplementations.controller;

import infosec.securityimplementations.dto.ApiResponse;
import infosec.securityimplementations.service.AuthorizationService;
import infosec.securityimplementations.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Protected resources controller for testing RBAC
 */
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ResourceController {

    private final SessionService sessionService;
    private final AuthorizationService authorizationService;

    /**
     * Admin-only resource
     */
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Map<String, String>>> adminResource(
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Long userId = sessionService.validateSession(sessionToken).orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        // Manual authorization check
        if (!authorizationService.hasRole(userId, "ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.error(
                    "Access denied. Admin role required."));
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Admin resource accessed successfully",
                Map.of("data", "This is admin-only data", "level", "ADMIN")));
    }

    /**
     * Manager-level resource
     */
    @GetMapping("/manager")
    public ResponseEntity<ApiResponse<Map<String, String>>> managerResource(
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Long userId = sessionService.validateSession(sessionToken).orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        // Manual authorization check with role hierarchy
        if (!authorizationService.hasOrganizationalRoleLevel(userId, "MANAGER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(
                    "Access denied. Manager role or higher required."));
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Manager resource accessed successfully",
                Map.of("data", "This is manager-level data", "level", "MANAGER")));
    }

    /**
     * User-level resource
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Map<String, String>>> userResource(
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Long userId = sessionService.validateSession(sessionToken).orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        // Manual authorization check
        if (!authorizationService.hasOrganizationalRoleLevel(userId, "USER")) {
            return ResponseEntity.status(403).body(ApiResponse.error(
                    "Access denied. User role required."));
        }

        return ResponseEntity.ok(ApiResponse.success(
                "User resource accessed successfully",
                Map.of("data", "This is user-level data", "level", "USER")));
    }

    /**
     * Resource-specific access test (requires JIT access or specific permission)
     */
    @GetMapping("/document/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> documentResource(
            @PathVariable String id,
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Long userId = sessionService.validateSession(sessionToken).orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        // Manual authorization check - check permission or JIT access
        boolean hasPermission = authorizationService.hasResourcePermission(userId, "DOCUMENT", "READ");
        boolean hasJitAccess = authorizationService.hasTemporaryAccess(userId, id);

        if (!hasPermission && !hasJitAccess) {
            return ResponseEntity.status(403).body(ApiResponse.error(
                    "Access denied. Document read permission or temporary access required."));
        }

        String accessType = hasPermission ? "permanent permission" : "temporary JIT access";

        return ResponseEntity.ok(ApiResponse.success(
                "Document accessed successfully via " + accessType,
                Map.of(
                        "documentId", id,
                        "data", "Document content for ID: " + id,
                        "accessType", accessType)));
    }
}
