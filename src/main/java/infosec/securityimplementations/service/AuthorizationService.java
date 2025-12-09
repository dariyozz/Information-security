package infosec.securityimplementations.service;

import infosec.securityimplementations.entity.*;
import infosec.securityimplementations.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manual authorization service - implements role-based access control
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final TemporaryAccessRepository temporaryAccessRepository;

    /**
     * Get all roles for a user
     */
    public List<Role> getUserRoles(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        return roleRepository.findAllById(roleIds);
    }

    /**
     * Get all permissions for a user (through their roles)
     */
    public Set<Permission> getUserPermissions(Long userId) {
        List<Role> roles = getUserRoles(userId);
        Set<Permission> permissions = new HashSet<>();

        for (Role role : roles) {
            List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(role.getId());
            permissions.addAll(permissionRepository.findAllById(permissionIds));
        }

        return permissions;
    }

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(Long userId, String roleName) {
        List<Role> roles = getUserRoles(userId);
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }

    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(Long userId, String... roleNames) {
        List<Role> roles = getUserRoles(userId);
        Set<String> userRoleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return Arrays.stream(roleNames).anyMatch(userRoleNames::contains);
    }

    /**
     * Check if user has a specific permission
     */
    public boolean hasPermission(Long userId, String permissionName) {
        Set<Permission> permissions = getUserPermissions(userId);
        return permissions.stream().anyMatch(p -> p.getName().equals(permissionName));
    }

    /**
     * Check if user has permission for a resource action
     */
    public boolean hasResourcePermission(Long userId, String resource, String action) {
        Set<Permission> permissions = getUserPermissions(userId);
        return permissions.stream().anyMatch(p -> p.getResource().equals(resource) && p.getAction().equals(action));
    }

    /**
     * Check organizational role hierarchy
     * ADMIN > MANAGER > USER
     */
    public boolean hasOrganizationalRoleLevel(Long userId, String requiredRole) {
        List<Role> roles = getUserRoles(userId);

        // Define role hierarchy
        Map<String, Integer> roleHierarchy = Map.of(
                "ADMIN", 3,
                "MANAGER", 2,
                "USER", 1);

        int requiredLevel = roleHierarchy.getOrDefault(requiredRole, 0);

        return roles.stream()
                .filter(role -> role.getRoleType() == Role.RoleType.ORGANIZATIONAL)
                .anyMatch(role -> roleHierarchy.getOrDefault(role.getName(), 0) >= requiredLevel);
    }

    /**
     * Check if user has temporary (JIT) access to a resource
     */
    public boolean hasTemporaryAccess(Long userId, String resourceId) {
        Optional<TemporaryAccess> access = temporaryAccessRepository
                .findByUserIdAndResourceIdAndRevokedFalse(userId, resourceId);

        return access.isPresent() && access.get().isActive();
    }

    /**
     * Manual access control check - combines role, permission, and JIT access
     */
    public boolean canAccess(Long userId, String resource, String action, String resourceId) {
        // Check direct permission
        if (hasResourcePermission(userId, resource, action)) {
            return true;
        }

        // Check temporary access if resourceId is provided
        if (resourceId != null && hasTemporaryAccess(userId, resourceId)) {
            return true;
        }

        return false;
    }
}
