import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { roleAPI } from '../services/api';
import './Dashboard.css';

export default function Dashboard() {
    const { user } = useAuth();
    const [roles, setRoles] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadUserRoles();
    }, [user]);

    const loadUserRoles = async () => {
        if (!user) return;
        try {
            const response = await roleAPI.getUserRoles(user.id);
            if (response.data.success) {
                setRoles(response.data.data.roles);
            }
        } catch (error) {
            console.error('Failed to load roles:', error);
        } finally {
            setLoading(false);
        }
    };

    if (!user) return null;

    const isAdmin = roles.some(r => r.name === 'ADMIN');

    return (
        <div className="dashboard-container">
            <header className="page-header">
                <div>
                    <h1>Welcome back, {user.username}!</h1>
                    <p className="subtitle">Here's your security overview for today.</p>
                </div>
            </header>

            <div className="dashboard-grid">
                {/* User Status Card */}
                <div className="dashboard-card status-card">
                    <div className="card-header">
                        <h2>👤 My Identity</h2>
                        <span className={`status-pill ${user.emailVerified ? 'verified' : 'pending'}`}>
                            {user.emailVerified ? 'Verified' : 'Verification Pending'}
                        </span>
                    </div>
                    <div className="card-content">
                        <div className="info-item">
                            <label>Username</label>
                            <span>{user.username}</span>
                        </div>
                        <div className="info-item">
                            <label>Email</label>
                            <span>{user.email}</span>
                        </div>
                        <div className="info-item">
                            <label>User ID</label>
                            <span className="mono">{user.id}</span>
                        </div>
                    </div>
                </div>

                {/* Roles Card */}
                <div className={`dashboard-card roles-card ${isAdmin ? 'admin-highlight' : ''}`}>
                    <div className="card-header">
                        <h2>🔑 Access Roles</h2>
                    </div>
                    <div className="card-content">
                        {loading ? (
                            <p>Loading...</p>
                        ) : (
                            <div className="roles-list">
                                {roles.map(role => (
                                    <div key={role.id} className="role-chip">
                                        <span className="role-icon">
                                            {role.name === 'ADMIN' ? '🛡️' : '👤'}
                                        </span>
                                        <div className="role-info">
                                            <span className="role-name">{role.name}</span>
                                            <span className="role-desc">{role.description}</span>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                {/* Quick Stats (Placeholder for real data) */}
                <div className="dashboard-card stats-card">
                    <div className="card-header">
                        <h2>📈 Session Stats</h2>
                    </div>
                    <div className="card-content stats-grid">
                        <div className="stat-item">
                            <span className="stat-value">Active</span>
                            <span className="stat-label">Session Status</span>
                        </div>
                        <div className="stat-item">
                            <span className="stat-value">{roles.length}</span>
                            <span className="stat-label">Total Roles</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
