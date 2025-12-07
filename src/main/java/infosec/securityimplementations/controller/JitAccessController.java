package infosec.securityimplementations.controller;

import infosec.securityimplementations.dto.ApiResponse;
import infosec.securityimplementations.dto.JitAccessRequest;
import infosec.securityimplementations.service.JitAccessService;
import infosec.securityimplementations.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Just-in-Time Access Controller
 */
@RestController
@RequestMapping("/api/jit")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class JitAccessController {

    private final JitAccessService jitAccessService;
    private final SessionService sessionService;

    /**
     * Request temporary access to a resource
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Map<String, Object>>> requestAccess(
            @Valid @RequestBody JitAccessRequest request,
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Long userId = sessionService.validateSession(sessionToken).orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        Map<String, Object> result = jitAccessService.requestAccess(userId, request);

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
     * Check access status for a specific resource
     */
    @GetMapping("/status/{resourceId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStatus(
            @PathVariable String resourceId,
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Long userId = sessionService.validateSession(sessionToken).orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        Map<String, Object> result = jitAccessService.checkAccessStatus(userId, resourceId);
        return ResponseEntity.ok(ApiResponse.success("Status retrieved", result));
    }

    /**
     * Revoke temporary access
     */
    @PostMapping("/revoke/{accessId}")
    public ResponseEntity<ApiResponse<Void>> revokeAccess(
            @PathVariable Long accessId,
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Long userId = sessionService.validateSession(sessionToken).orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        Map<String, Object> result = jitAccessService.revokeAccess(accessId, userId);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(ApiResponse.success((String) result.get("message")));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    (String) result.get("message")));
        }
    }

    /**
     * Get all temporary access grants for current user
     */
    @GetMapping("/my-access")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyAccess(
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Long userId = sessionService.validateSession(sessionToken).orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        Map<String, Object> result = jitAccessService.getUserAccess(userId);
        return ResponseEntity.ok(ApiResponse.success("Access list retrieved", result));
    }

    /**
     * Get pending requests (Admin only)
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPendingRequests(
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Long userId = sessionService.validateSession(sessionToken).orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        Map<String, Object> result = jitAccessService.getPendingRequests(userId);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(ApiResponse.success("Pending requests retrieved", result));
        } else {
            return ResponseEntity.status(403).body(ApiResponse.error(
                    (String) result.get("message")));
        }
    }

    /**
     * Approve access (Admin only)
     */
    @PostMapping("/approve/{accessId}")
    public ResponseEntity<ApiResponse<Void>> approveAccess(
            @PathVariable Long accessId,
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Long userId = sessionService.validateSession(sessionToken).orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        Map<String, Object> result = jitAccessService.approveRequest(accessId, userId);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(ApiResponse.success((String) result.get("message")));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    (String) result.get("message")));
        }
    }

    /**
     * Reject access (Admin only)
     */
    @PostMapping("/reject/{accessId}")
    public ResponseEntity<ApiResponse<Void>> rejectAccess(
            @PathVariable Long accessId,
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Long userId = sessionService.validateSession(sessionToken).orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        Map<String, Object> result = jitAccessService.rejectRequest(accessId, userId);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(ApiResponse.success((String) result.get("message")));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    (String) result.get("message")));
        }
    }
}
