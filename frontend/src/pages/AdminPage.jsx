import {useEffect, useState} from "react";
import api from "../services/api.js";
import {useAuth} from "../context/AuthContext.jsx";
import {Navigate} from "react-router-dom";
import {Layout} from "../components/Layout.jsx";
import styles from "../styles.js";

export default function AdminRequestsPage() {
    const { isAdmin } = useAuth();
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (isAdmin) loadRequests();
    }, [isAdmin]);

    const loadRequests = async () => {
        try {
            const response = await api.jit.getPending();
            if (response.success) {
                console.log(response)
                setRequests(response.data.requests || []);
            }
        } catch (error) {
            console.error('Failed to load requests:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = async (id) => {
        try {
            await api.jit.approve(id);
            await loadRequests();
        } catch (error) {
            console.error('Failed to approve:', error);
        }
    };

    const handleReject = async (id) => {
        try {
            await api.jit.reject(id);
            await loadRequests();
        } catch (error) {
            console.error('Failed to reject:', error);
        }
    };

    if (!isAdmin) return <Navigate to="/dashboard" />;

    return (
        <Layout>
            <div>
                <h1 style={{ fontSize: '32px', color: '#1e293b', marginBottom: '32px' }}>
                    📋 Access Request Management
                </h1>

                <div style={styles.card}>
                    <div style={styles.cardHeader}>
                        <h2 style={{ margin: 0, fontSize: '20px', color: '#1e293b' }}>⏳ Pending Requests</h2>
                        <div style={{ ...styles.badge, ...styles.badgeWarning }}>{requests.length} Pending</div>
                    </div>

                    {loading ? (
                        <div style={{ textAlign: 'center', padding: '40px', color: '#64748b' }}>Loading...</div>
                    ) : requests.length === 0 ? (
                        <div style={{ textAlign: 'center', padding: '40px', color: '#64748b' }}>
                            <div style={{ fontSize: '48px', marginBottom: '16px' }}>✅</div>
                            <div>No pending requests</div>
                        </div>
                    ) : (
                        <div style={{ display: 'grid', gap: '16px' }}>
                            {requests.map(req => (
                                <div key={req.id} style={{
                                    padding: '20px',
                                    background: '#fffbeb',
                                    borderRadius: '12px',
                                    border: '2px solid #fde68a',
                                }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px' }}>
                                        <div>
                                            <div style={{ fontWeight: '700', fontSize: '16px', color: '#1e293b', marginBottom: '4px' }}>
                                                {req.resourceType}: {req.resourceId}
                                            </div>
                                            <div style={{ fontSize: '14px', color: '#64748b' }}>
                                                User ID: {req.userId} • Duration: {req.durationMinutes} min
                                            </div>
                                        </div>
                                    </div>

                                    <div style={{ fontSize: '14px', color: '#475569', marginBottom: '16px', padding: '12px', background: 'white', borderRadius: '8px' }}>
                                        <strong>Justification:</strong><br />
                                        {req.reason}
                                    </div>

                                    <div style={{ fontSize: '13px', color: '#64748b', marginBottom: '16px' }}>
                                        Requested: {new Date(req.grantedAt).toLocaleString()}
                                    </div>

                                    <div style={{ display: 'flex', gap: '12px' }}>
                                        <button
                                            onClick={() => handleApprove(req.id)}
                                            style={{ ...styles.btn, ...styles.btnSuccess, flex: 1 }}
                                        >
                                            ✅ Approve
                                        </button>
                                        <button
                                            onClick={() => handleReject(req.id)}
                                            style={{ ...styles.btn, ...styles.btnDanger, flex: 1 }}
                                        >
                                            ❌ Reject
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </Layout>
    );
}
