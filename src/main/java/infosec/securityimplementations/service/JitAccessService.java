package infosec.securityimplementations.service;

import infosec.securityimplementations.dto.JitAccessRequest;
import infosec.securityimplementations.entity.TemporaryAccess;
import infosec.securityimplementations.repository.TemporaryAccessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Just-in-Time (JIT) Access Service
 * Implements temporary, time-limited access to resources
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JitAccessService {

    private final TemporaryAccessRepository temporaryAccessRepository;
    private final AuthorizationService authorizationService;

    @Value("${jit.access.default.duration.minutes:15}")
    private int defaultDurationMinutes;

    /**
     * Request temporary access to a resource
     * JIT Flow:
     * 1. User requests resource
     * 2. App evaluates policies
     * 3. App grants temporary access
     * 4. Access is revoked after completion or expiration
     */
    @Transactional
    public Map<String, Object> requestAccess(Long userId, JitAccessRequest request) {
        Map<String, Object> result = new HashMap<>();

        // Check if user already has active temporary access to this resource
        Optional<TemporaryAccess> existingAccess = temporaryAccessRepository
                .findByUserIdAndResourceIdAndRevokedFalse(userId, request.getResourceId());

        if (existingAccess.isPresent() && existingAccess.get().isActive()) {
            result.put("success", false);
            result.put("message", "You already have active access to this resource");
            result.put("expiresAt", existingAccess.get().getExpiresAt());
            return result;
        }

        // Evaluate policy - for this implementation, we'll grant access if user is
        // authenticated
        // In a real system, this would involve complex policy evaluation
        boolean policyApproved = evaluateAccessPolicy(userId, request);

        if (!policyApproved) {
            result.put("success", false);
            result.put("message", "Access request denied by policy");
            return result;
        }

        // Determine duration
        int durationMinutes = request.getDurationMinutes() != null
                ? request.getDurationMinutes()
                : defaultDurationMinutes;

        // Create access request (PENDING)
        TemporaryAccess access = TemporaryAccess.builder()
                .userId(userId)
                .resourceId(request.getResourceId())
                .resourceType(request.getResourceType())
                .reason(request.getReason())
                .durationMinutes(durationMinutes)
                .status(infosec.securityimplementations.entity.AccessStatus.PENDING)
                .revoked(false)
                .build();

        access = temporaryAccessRepository.save(access);
        log.info("Temporary access requested by user {} for resource {}", userId, request.getResourceId());

        result.put("success", true);
        result.put("message", "Access request submitted and pending approval");
        result.put("access", Map.of(
                "id", access.getId(),
                "resourceId", access.getResourceId(),
                "resourceType", access.getResourceType(),
                "status", access.getStatus(),
                "durationMinutes", durationMinutes));

        return result;
    }

    /**
     * Evaluate access policy
     * This is a simplified implementation - in production, this would involve:
     * - User attributes
     * - Resource sensitivity
     * - Time of day restrictions
     * - Approval workflows
     * - Compliance checks
     */
    private boolean evaluateAccessPolicy(Long userId, JitAccessRequest request) {
        // For this implementation, grant access if user has at least USER role
        return authorizationService.hasOrganizationalRoleLevel(userId, "USER");
    }

    /**
     * Approve access request
     */
    @Transactional
    public Map<String, Object> approveRequest(Long accessId, Long adminId) {
        Map<String, Object> result = new HashMap<>();

        // Verify admin role
        if (!authorizationService.hasRole(adminId, "ADMIN")) {
            result.put("success", false);
            result.put("message", "Only admins can approve requests");
            return result;
        }

        Optional<TemporaryAccess> accessOpt = temporaryAccessRepository.findById(accessId);
        if (accessOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Access request not found");
            return result;
        }

        TemporaryAccess access = accessOpt.get();
        if (access.getStatus() != infosec.securityimplementations.entity.AccessStatus.PENDING) {
            result.put("success", false);
            result.put("message", "Request is not in PENDING state");
            return result;
        }

        // Activate access
        access.setStatus(infosec.securityimplementations.entity.AccessStatus.APPROVED);
        access.setGrantedAt(LocalDateTime.now());
        access.setExpiresAt(LocalDateTime.now().plusMinutes(access.getDurationMinutes()));

        temporaryAccessRepository.save(access);
        log.info("Access request {} approved by admin {}", accessId, adminId);

        result.put("success", true);
        result.put("message", "Access approved successfully");
        return result;
    }

    /**
     * Reject access request
     */
    @Transactional
    public Map<String, Object> rejectRequest(Long accessId, Long adminId) {
        Map<String, Object> result = new HashMap<>();

        // Verify admin role
        if (!authorizationService.hasRole(adminId, "ADMIN")) {
            result.put("success", false);
            result.put("message", "Only admins can reject requests");
            return result;
        }

        Optional<TemporaryAccess> accessOpt = temporaryAccessRepository.findById(accessId);
        if (accessOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Access request not found");
            return result;
        }

        TemporaryAccess access = accessOpt.get();
        access.setStatus(infosec.securityimplementations.entity.AccessStatus.REJECTED);

        temporaryAccessRepository.save(access);
        log.info("Access request {} rejected by admin {}", accessId, adminId);

        result.put("success", true);
        result.put("message", "Access rejected");
        return result;
    }

    /**
     * Manually revoke temporary access
     */
    @Transactional
    public Map<String, Object> revokeAccess(Long accessId, Long requestingUserId) {
        Map<String, Object> result = new HashMap<>();

        Optional<TemporaryAccess> accessOpt = temporaryAccessRepository.findById(accessId);
        if (accessOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Access record not found");
            return result;
        }

        TemporaryAccess access = accessOpt.get();

        // Check if requesting user is the owner or an admin
        boolean isOwner = access.getUserId().equals(requestingUserId);
        boolean isAdmin = authorizationService.hasRole(requestingUserId, "ADMIN");

        if (!isOwner && !isAdmin) {
            result.put("success", false);
            result.put("message", "You don't have permission to revoke this access");
            return result;
        }

        access.setRevoked(true);
        temporaryAccessRepository.save(access);
        log.info("Temporary access {} revoked by user {}", accessId, requestingUserId);

        result.put("success", true);
        result.put("message", "Access revoked successfully");

        return result;
    }

    /**
     * Get all temporary access grants for a user
     */
    public Map<String, Object> getUserAccess(Long userId) {
        Map<String, Object> result = new HashMap<>();

        List<TemporaryAccess> accessList = temporaryAccessRepository.findByUserIdAndRevokedFalse(userId);

        result.put("success", true);
        result.put("accessList", accessList);

        return result;
    }

    /**
     * Check access status for a specific resource
     */
    public Map<String, Object> checkAccessStatus(Long userId, String resourceId) {
        Map<String, Object> result = new HashMap<>();

        Optional<TemporaryAccess> accessOpt = temporaryAccessRepository
                .findByUserIdAndResourceIdAndRevokedFalse(userId, resourceId);

        if (accessOpt.isEmpty()) {
            result.put("hasAccess", false);
            result.put("message", "No active access to this resource");
            return result;
        }

        TemporaryAccess access = accessOpt.get();
        boolean isActive = access.isActive();

        result.put("hasAccess", isActive);
        result.put("access", access);
        result.put("isExpired", access.isExpired());

        return result;
    }

    /**
     * Get all pending access requests (for admins)
     */
    public Map<String, Object> getPendingRequests(Long adminId) {
        Map<String, Object> result = new HashMap<>();

        if (!authorizationService.hasRole(adminId, "ADMIN")) {
            result.put("success", false);
            result.put("message", "Only admins can view pending requests");
            return result;
        }

        List<TemporaryAccess> pendingRequests = temporaryAccessRepository
                .findByStatus(infosec.securityimplementations.entity.AccessStatus.PENDING);

        result.put("success", true);
        result.put("requests", pendingRequests);

        return result;
    }

    /**
     * Scheduled task to clean up expired access
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void cleanupExpiredAccess() {
        List<TemporaryAccess> expiredAccess = temporaryAccessRepository
                .findExpiredAccess(LocalDateTime.now());

        if (!expiredAccess.isEmpty()) {
            expiredAccess.forEach(access -> access.setRevoked(true));
            temporaryAccessRepository.saveAll(expiredAccess);
            log.info("Cleaned up {} expired temporary access grants", expiredAccess.size());
        }
    }
}
