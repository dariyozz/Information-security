package infosec.securityimplementations.config;

import infosec.securityimplementations.entity.Permission;
import infosec.securityimplementations.entity.Role;
import infosec.securityimplementations.entity.RolePermission;
import infosec.securityimplementations.entity.User;
import infosec.securityimplementations.entity.UserRole;
import infosec.securityimplementations.repository.PermissionRepository;
import infosec.securityimplementations.repository.RolePermissionRepository;
import infosec.securityimplementations.repository.RoleRepository;
import infosec.securityimplementations.repository.UserRepository;
import infosec.securityimplementations.repository.UserRoleRepository;
import infosec.securityimplementations.service.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Initialize default roles, permissions, and test users
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordService passwordService;

    @Override
    public void run(String... args) {
        log.info("Initializing default data...");

        // Initialize roles
        initializeRoles();

        // Initialize permissions
        initializePermissions();

        // Map roles to permissions
        mapRolePermissions();

        // Create test users for each role
        createTestUsers();

        log.info("Data initialization complete!");
    }

    private void initializeRoles() {
        List<Role> roles = new ArrayList<>();

        // Organizational roles (hierarchy: ADMIN > MANAGER > USER)
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roles.add(Role.builder()
                    .name("ADMIN")
                    .roleType(Role.RoleType.ORGANIZATIONAL)
                    .description("Administrator with full system access")
                    .build());
        }

        if (roleRepository.findByName("MANAGER").isEmpty()) {
            roles.add(Role.builder()
                    .name("MANAGER")
                    .roleType(Role.RoleType.ORGANIZATIONAL)
                    .description("Manager with elevated privileges")
                    .build());
        }

        if (roleRepository.findByName("USER").isEmpty()) {
            roles.add(Role.builder()
                    .name("USER")
                    .roleType(Role.RoleType.ORGANIZATIONAL)
                    .description("Standard user with basic access")
                    .build());
        }

        // Resource-specific roles
        if (roleRepository.findByName("DOCUMENT_VIEWER").isEmpty()) {
            roles.add(Role.builder()
                    .name("DOCUMENT_VIEWER")
                    .roleType(Role.RoleType.RESOURCE_SPECIFIC)
                    .description("Can view documents")
                    .build());
        }

        if (roleRepository.findByName("DOCUMENT_EDITOR").isEmpty()) {
            roles.add(Role.builder()
                    .name("DOCUMENT_EDITOR")
                    .roleType(Role.RoleType.RESOURCE_SPECIFIC)
                    .description("Can edit documents")
                    .build());
        }

        if (!roles.isEmpty()) {
            roleRepository.saveAll(roles);
            log.info("Created {} roles", roles.size());
        }
    }

    private void initializePermissions() {
        List<Permission> permissions = new ArrayList<>();

        // Document permissions
        if (permissionRepository.findByName("READ_DOCUMENTS").isEmpty()) {
            permissions.add(Permission.builder()
                    .name("READ_DOCUMENTS")
                    .resource("DOCUMENT")
                    .action("READ")
                    .description("Read document content")
                    .build());
        }

        if (permissionRepository.findByName("WRITE_DOCUMENTS").isEmpty()) {
            permissions.add(Permission.builder()
                    .name("WRITE_DOCUMENTS")
                    .resource("DOCUMENT")
                    .action("WRITE")
                    .description("Create and edit documents")
                    .build());
        }

        if (permissionRepository.findByName("DELETE_DOCUMENTS").isEmpty()) {
            permissions.add(Permission.builder()
                    .name("DELETE_DOCUMENTS")
                    .resource("DOCUMENT")
                    .action("DELETE")
                    .description("Delete documents")
                    .build());
        }

        // User management permissions
        if (permissionRepository.findByName("MANAGE_USERS").isEmpty()) {
            permissions.add(Permission.builder()
                    .name("MANAGE_USERS")
                    .resource("USER")
                    .action("MANAGE")
                    .description("Manage user accounts")
                    .build());
        }

        if (permissionRepository.findByName("ASSIGN_ROLES").isEmpty()) {
            permissions.add(Permission.builder()
                    .name("ASSIGN_ROLES")
                    .resource("ROLE")
                    .action("ASSIGN")
                    .description("Assign roles to users")
                    .build());
        }

        if (!permissions.isEmpty()) {
            permissionRepository.saveAll(permissions);
            log.info("Created {} permissions", permissions.size());
        }
    }

    private void mapRolePermissions() {
        // ADMIN gets all permissions
        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
        if (adminRole != null) {
            List<Permission> allPermissions = permissionRepository.findAll();
            for (Permission permission : allPermissions) {
                if (rolePermissionRepository.findByRoleId(adminRole.getId()).stream()
                        .noneMatch(rp -> rp.getPermissionId().equals(permission.getId()))) {
                    rolePermissionRepository.save(RolePermission.builder()
                            .roleId(adminRole.getId())
                            .permissionId(permission.getId())
                            .build());
                }
            }
        }

        // MANAGER gets document read/write permissions
        Role managerRole = roleRepository.findByName("MANAGER").orElse(null);
        if (managerRole != null) {
            assignPermissionToRole(managerRole, "READ_DOCUMENTS");
            assignPermissionToRole(managerRole, "WRITE_DOCUMENTS");
        }

        // USER gets document read permission
        Role userRole = roleRepository.findByName("USER").orElse(null);
        if (userRole != null) {
            // Permissions removed to enforce JIT access testing
            // assignPermissionToRole(userRole, "READ_DOCUMENTS");
        }

        // DOCUMENT_VIEWER gets read permission
        Role viewerRole = roleRepository.findByName("DOCUMENT_VIEWER").orElse(null);
        if (viewerRole != null) {
            assignPermissionToRole(viewerRole, "READ_DOCUMENTS");
        }

        // DOCUMENT_EDITOR gets read and write permissions
        Role editorRole = roleRepository.findByName("DOCUMENT_EDITOR").orElse(null);
        if (editorRole != null) {
            assignPermissionToRole(editorRole, "READ_DOCUMENTS");
            assignPermissionToRole(editorRole, "WRITE_DOCUMENTS");
        }

        log.info("Role-permission mappings created");
    }

    private void assignPermissionToRole(Role role, String permissionName) {
        Permission permission = permissionRepository.findByName(permissionName).orElse(null);
        if (permission != null) {
            if (rolePermissionRepository.findByRoleId(role.getId()).stream()
                    .noneMatch(rp -> rp.getPermissionId().equals(permission.getId()))) {
                rolePermissionRepository.save(RolePermission.builder()
                        .roleId(role.getId())
                        .permissionId(permission.getId())
                        .build());
            }
        }
    }

    private void createTestUsers() {
        // Create Admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .passwordHash(passwordService.hashPassword("admin123"))
                    .emailVerified(true)
                    .build();
            admin = userRepository.save(admin);

            // Assign ADMIN role
            Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
            if (adminRole != null) {
                userRoleRepository.save(UserRole.builder()
                        .userId(admin.getId())
                        .roleId(adminRole.getId())
                        .build());
            }

            log.info("Created test admin user (username: admin, password: admin123) - Verified: true");
        } else {
            userRepository.findByUsername("admin").ifPresent(u -> {
                if (!u.getEmailVerified()) {
                    u.setEmailVerified(true);
                    userRepository.save(u);
                }
            });
        }

        // Create Manager user
        if (userRepository.findByUsername("manager").isEmpty()) {
            User manager = User.builder()
                    .username("manager")
                    .email("manager@example.com")
                    .passwordHash(passwordService.hashPassword("manager123"))
                    .emailVerified(true)
                    .build();
            manager = userRepository.save(manager);

            // Assign MANAGER role
            Role managerRole = roleRepository.findByName("MANAGER").orElse(null);
            if (managerRole != null) {
                userRoleRepository.save(UserRole.builder()
                        .userId(manager.getId())
                        .roleId(managerRole.getId())
                        .build());
            }

            log.info("Created test manager user (username: manager, password: manager123)");
        }

        // Create regular User
        if (userRepository.findByUsername("user").isEmpty()) {
            User user = User.builder()
                    .username("user")
                    .email("user@example.com")
                    .passwordHash(passwordService.hashPassword("user123"))
                    .emailVerified(false)
                    .build();
            user = userRepository.save(user);

            // Assign USER role
            Role userRole = roleRepository.findByName("USER").orElse(null);
            if (userRole != null) {
                userRoleRepository.save(UserRole.builder()
                        .userId(user.getId())
                        .roleId(userRole.getId())
                        .build());
            }

            log.info("Created test user (username: user, password: user123)");
        } else {
            // Update existing user to be unverified for testing
            userRepository.findByUsername("user").ifPresent(u -> {
                if (u.getEmailVerified()) {
                    u.setEmailVerified(false);
                    userRepository.save(u);
                    log.info("Reset 'user' email verification to false for testing");
                }
            });
        }
    }
}
