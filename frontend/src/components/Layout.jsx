import styles from "../styles.js";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

function Sidebar() {
    const { user, logout, isAdmin, isManager } = useAuth();
    const location = useLocation();

    const isActive = (path) => location.pathname === path;

    const navItems = [
        { icon: '📊', label: 'Dashboard', path: '/dashboard', roles: ['USER', 'MANAGER', 'ADMIN'] },
        { icon: '🔒', label: 'Resources', path: '/resources', roles: ['USER', 'MANAGER', 'ADMIN'] },
        { icon: '⏱️', label: 'JIT Access', path: '/jit-access', roles: ['USER', 'MANAGER', 'ADMIN'] },
        { icon: '👥', label: 'User Management', path: '/admin/users', roles: ['ADMIN'] },
        { icon: '🎭', label: 'Role Management', path: '/admin/roles', roles: ['ADMIN'] },
        { icon: '📋', label: 'Access Requests', path: '/admin/requests', roles: ['ADMIN'] },
        { icon: '📈', label: 'Reports', path: '/admin/reports', roles: ['MANAGER', 'ADMIN'] },
    ];

    const userRoles = user?.roles?.map(r => r.name) || [];
    const filteredItems = navItems.filter(item =>
        item.roles.some(role => userRoles.includes(role))
    );

    return (
        <div style={styles.sidebar}>
            <div style={styles.sidebarHeader}>
                <div style={styles.logo}>
                    <span>🛡️</span>
                    <span>SecureApp</span>
                </div>
                <span style={styles.roleChip}>
                    {isAdmin ? '🔱 ADMIN' : isManager ? '⚡ MANAGER' : '👤 USER'}
                </span>
            </div>

            <nav style={styles.nav}>
                <div style={styles.navSection}>
                    <div style={styles.navTitle}>Navigation</div>
                    {filteredItems.map(item => (
                        <Link
                            key={item.path}
                            to={item.path}
                            style={{
                                ...styles.navItem,
                                ...(isActive(item.path) ? styles.navItemActive : {}),
                            }}
                        >
                            <span>{item.icon}</span>
                            <span>{item.label}</span>
                        </Link>
                    ))}
                </div>
            </nav>

            <div style={styles.sidebarFooter}>
                <div style={styles.userProfile}>
                    <div style={styles.avatar}>
                        {user?.username?.charAt(0).toUpperCase()}
                    </div>
                    <div style={{ flex: 1 }}>
                        <div style={{ fontWeight: '600', fontSize: '14px' }}>{user?.username}</div>
                        <div style={{ fontSize: '12px', color: '#8b92a7' }}>
                            {user?.emailVerified ? '✓ Verified' : '⚠ Unverified'}
                        </div>
                    </div>
                    <button
                        onClick={logout}
                        style={{
                            background: 'rgba(239, 68, 68, 0.1)',
                            border: 'none',
                            color: '#ef4444',
                            padding: '8px',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            fontSize: '18px',
                        }}
                        title="Logout"
                    >
                        🚪
                    </button>
                </div>
            </div>
        </div>
    );
}

export function Layout({ children }) {
    return (
        <div style={styles.layout}>
            <Sidebar />
            <div style={styles.mainContent}>{children}</div>
        </div>
    );
}
