import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Layout.css';

export default function Layout({ children }) {
    const { user, logout } = useAuth();
    const location = useLocation();

    const isAdmin = user?.roles?.some(role => role.name === 'ADMIN');

    const isActive = (path) => location.pathname === path;

    return (
        <div className="layout-container">
            {/* Left Sidebar */}
            <aside className="sidebar">
                <div className="sidebar-header">
                    <h2>🛡️ SecApp</h2>
                    <span className="role-badge">{isAdmin ? 'ADMIN' : 'USER'}</span>
                </div>

                <nav className="sidebar-nav">
                    <div className="nav-section">
                        <h3>Main</h3>
                        <Link to="/dashboard" className={`nav-item ${isActive('/dashboard') ? 'active' : ''}`}>
                            📊 Dashboard
                        </Link>
                        <Link to="/resources" className={`nav-item ${isActive('/resources') ? 'active' : ''}`}>
                            🔒 Resources
                        </Link>
                    </div>

                    <div className="nav-section">
                        <h3>Access</h3>
                        <Link to="/jit-access" className={`nav-item ${isActive('/jit-access') ? 'active' : ''}`}>
                            ⏱️ JIT Access
                        </Link>
                    </div>

                    {isAdmin && (
                        <div className="nav-section">
                            <h3>Admin</h3>
                            <div className="nav-item disabled">
                                👥 User Mgmt (Soon)
                            </div>
                        </div>
                    )}
                </nav>

                <div className="sidebar-footer">
                    <div className="user-profile">
                        <div className="avatar">{user?.username?.charAt(0).toUpperCase()}</div>
                        <div className="user-details">
                            <span className="username">{user?.username}</span>
                            <span className="email-status">{user?.emailVerified ? 'Verified' : 'Unverified'}</span>
                        </div>
                    </div>
                    <button onClick={logout} className="logout-btn-sidebar" title="Logout">
                        🚪
                    </button>
                </div>
            </aside>

            {/* Main Content Area */}
            <main className="main-content">
                {children}
            </main>
        </div>
    );
}
