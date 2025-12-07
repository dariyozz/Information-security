package infosec.securityimplementations.service;

import infosec.securityimplementations.entity.Role;
import infosec.securityimplementations.entity.UserRole;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthorizationService authorizationService;

    /**
     * Assign a role to a user
     */
    @Transactional
    public Map<String, Object> assignRole(Long userId, String roleName, Long requestingUserId) {
        Map<String, Object> result = new HashMap<>();

        // Check if requesting user has ADMIN role
        if (!authorizationService.hasRole(requestingUserId, "ADMIN")) {
            result.put("success", false);
            result.put("message", "Only admins can assign roles");
            return result;
        }

        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Role not found");
            return result;
        }

        Role role = roleOpt.get();

        // Check if user already has this role
        List<UserRole> existingRoles = userRoleRepository.findByUserId(userId);
        boolean alreadyHasRole = existingRoles.stream()
                .anyMatch(ur -> ur.getRoleId().equals(role.getId()));

        if (alreadyHasRole) {
            result.put("success", false);
            result.put("message", "User already has this role");
            return result;
        }

        UserRole userRole = UserRole.builder()
                .userId(userId)
                .roleId(role.getId())
                .build();

        userRoleRepository.save(userRole);
        log.info("Role {} assigned to user {}", roleName, userId);

        result.put("success", true);
        result.put("message", "Role assigned successfully");

        return result;
    }

    /**
     * Revoke a role from a user
     */
    @Transactional
    public Map<String, Object> revokeRole(Long userId, String roleName, Long requestingUserId) {
        Map<String, Object> result = new HashMap<>();

        // Check if requesting user has ADMIN role
        if (!authorizationService.hasRole(requestingUserId, "ADMIN")) {
            result.put("success", false);
            result.put("message", "Only admins can revoke roles");
            return result;
        }

        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Role not found");
            return result;
        }

        userRoleRepository.deleteByUserIdAndRoleId(userId, roleOpt.get().getId());
        log.info("Role {} revoked from user {}", roleName, userId);

        result.put("success", true);
        result.put("message", "Role revoked successfully");

        return result;
    }

    /**
     * Get all roles for a user
     */
    public Map<String, Object> getUserRoles(Long userId) {
        Map<String, Object> result = new HashMap<>();

        List<Role> roles = authorizationService.getUserRoles(userId);

        result.put("success", true);
        result.put("roles", roles);

        return result;
    }

    /**
     * Get all available roles
     */
    public Map<String, Object> getAllRoles() {
        Map<String, Object> result = new HashMap<>();

        List<Role> roles = roleRepository.findAll();

        result.put("success", true);
        result.put("roles", roles);

        return result;
    }
}
