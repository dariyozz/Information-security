import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from "../services/api.js";
import styles from "../styles.js";
import {Layout} from "../components/Layout.jsx";
import {Link} from "react-router-dom";

export default function DashboardPage() {
    const { user, isAdmin, isManager} = useAuth();
    const [stats, setStats] = useState({ jitRequests: 0, activeAccess: 0, roles: 0 });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadStats();
    }, []);

    const loadStats = async () => {
        try {
            const jitResponse = await api.jit.getMyAccess();
            const activeCount = jitResponse.data?.accessList?.filter(a => a.status === 'APPROVED').length || 0;

            setStats({
                jitRequests: jitResponse.data?.accessList?.length || 0,
                activeAccess: activeCount,
                roles: user?.roles?.length || 0,
            });
        } catch (error) {
            console.error('Failed to load stats:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <Layout>
                <div style={{ textAlign: 'center', padding: '60px', color: '#64748b' }}>
                    <div style={{ fontSize: '48px', marginBottom: '16px' }}>⏳</div>
                    <div>Loading dashboard...</div>
                </div>
            </Layout>
        );
    }

    return (
        <Layout>
            <div>
                <div style={{ marginBottom: '32px' }}>
                    <h1 style={{ fontSize: '32px', color: '#1e293b', margin: 0, marginBottom: '8px' }}>
                        Welcome back, {user?.username}! 👋
                    </h1>
                    <p style={{ color: '#64748b', fontSize: '16px', margin: 0 }}>
                        {isAdmin ? 'System Administrator Dashboard' : isManager ? 'Manager Dashboard' : 'User Dashboard'}
                    </p>
                </div>

                <div style={styles.statsGrid}>
                    <div style={styles.statCard}>
                        <div style={{ fontSize: '32px', marginBottom: '12px' }}>🎭</div>
                        <div style={{ fontSize: '28px', fontWeight: '700', color: '#1e293b', marginBottom: '4px' }}>
                            {stats.jitRequests}
                        </div>
                        <div style={{ fontSize: '14px', color: '#64748b', fontWeight: '500' }}>Total Requests</div>
                    </div>

                    {isAdmin && (
                        <div style={styles.statCard}>
                            <div style={{ fontSize: '32px', marginBottom: '12px' }}>🔱</div>
                            <div style={{ fontSize: '28px', fontWeight: '700', color: '#1e293b', marginBottom: '4px' }}>
                                ADMIN
                            </div>
                            <div style={{ fontSize: '14px', color: '#64748b', fontWeight: '500' }}>Full System Access</div>
                        </div>
                    )}
                </div>

                <div style={styles.card}>
                    <div style={styles.cardHeader}>
                        <h2 style={{ margin: 0, fontSize: '20px', color: '#1e293b' }}>🎭 Your Roles & Permissions</h2>
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                        {user?.roles?.map((role, idx) => (
                            <div
                                key={idx}
                                style={{
                                    padding: '16px',
                                    background: 'linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%)',
                                    borderRadius: '12px',
                                    border: '2px solid #e2e8f0',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '16px',
                                }}
                            >
                                <div style={{ fontSize: '32px' }}>
                                    {role.name === 'ADMIN' ? '🔱' : role.name === 'MANAGER' ? '⚡' : '👤'}
                                </div>
                                <div style={{ flex: 1 }}>
                                    <div style={{ fontWeight: '700', fontSize: '16px', color: '#1e293b' }}>{role.name}</div>
                                    <div style={{ fontSize: '14px', color: '#64748b', marginTop: '4px' }}>
                                        {role.name === 'ADMIN' && 'Full system access with all permissions'}
                                        {role.name === 'MANAGER' && 'Elevated privileges for team management'}
                                        {role.name === 'USER' && 'Standard user access rights'}
                                    </div>
                                </div>
                                <div style={{ ...styles.badge, ...styles.badgeSuccess }}>ACTIVE</div>
                            </div>
                        ))}
                    </div>
                </div>

                {isAdmin && (
                    <div style={styles.card}>
                        <div style={styles.cardHeader}>
                            <h2 style={{ margin: 0, fontSize: '20px', color: '#1e293b' }}>🔧 Quick Admin Actions</h2>
                        </div>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '16px' }}>
                            <Link to="/admin/users" style={{ textDecoration: 'none' }}>
                                <div style={{
                                    padding: '20px',
                                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                    borderRadius: '12px',
                                    color: 'white',
                                    cursor: 'pointer',
                                    transition: 'transform 0.2s',
                                }}>
                                    <div style={{ fontSize: '32px', marginBottom: '8px' }}>👥</div>
                                    <div style={{ fontWeight: '600' }}>Manage Users</div>
                                    <div style={{ fontSize: '13px', opacity: 0.9, marginTop: '4px' }}>
                                        View and manage system users
                                    </div>
                                </div>
                            </Link>
                            <Link to="/admin/requests" style={{ textDecoration: 'none' }}>
                                <div style={{
                                    padding: '20px',
                                    background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
                                    borderRadius: '12px',
                                    color: 'white',
                                    cursor: 'pointer',
                                    transition: 'transform 0.2s',
                                }}>
                                    <div style={{ fontSize: '32px', marginBottom: '8px' }}>📋</div>
                                    <div style={{ fontWeight: '600' }}>Access Requests</div>
                                    <div style={{ fontSize: '13px', opacity: 0.9, marginTop: '4px' }}>
                                        Review pending JIT requests
                                    </div>
                                </div>
                            </Link>
                        </div>
                    </div>
                )}
            </div>
        </Layout>
    );
}
