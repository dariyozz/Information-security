import {useState, useEffect} from 'react';
import api from "../services/api.js";
import {Layout} from "../components/Layout.jsx";
import styles from "../styles.js";

export default function JitAccessPage() {
    const [myRequests, setMyRequests] = useState([]);
    const [formData, setFormData] = useState({ resourceId: '', resourceType: 'DOCUMENT', reason: '', durationMinutes: 30 });
    const [message, setMessage] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadMyRequests();
    }, []);

    const loadMyRequests = async () => {
        try {
            const response = await api.jit.getMyAccess();
            if (response.success) {
                setMyRequests(response.data.accessList || []);
            }
        } catch (error) {
            console.error('Failed to load requests:', error);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);

        try {
            const response = await api.jit.request(formData);
            if (response.success) {
                setMessage({ type: 'success', text: 'Access request submitted!' });
                setFormData({ resourceId: '', resourceType: 'DOCUMENT', reason: '', durationMinutes: 30 });
                await loadMyRequests();
            } else {
                setMessage({ type: 'error', text: response.message });
            }
        } catch (error) {
            setMessage({ type: 'error', text: 'Failed to submit request' });
        } finally {
            setLoading(false);
        }
    };

    const handleRevoke = async (id) => {
        try {
            await api.jit.revoke(id);
            await loadMyRequests();
        } catch (error) {
            console.error('Failed to revoke:', error);
        }
    };

    return (
        <Layout>
            <div>
                <h1 style={{ fontSize: '32px', color: '#1e293b', marginBottom: '32px' }}>
                    ⏱️ Just-in-Time Access
                </h1>

                <div style={styles.card}>
                    <div style={styles.cardHeader}>
                        <h2 style={{ margin: 0, fontSize: '20px', color: '#1e293b' }}>🎫 Request Temporary Access</h2>
                    </div>

                    {message && (
                        <div style={{
                            padding: '12px',
                            borderRadius: '8px',
                            marginBottom: '20px',
                            background: message.type === 'error' ? '#fee2e2' : '#d1fae5',
                            color: message.type === 'error' ? '#991b1b' : '#065f46',
                        }}>
                            {message.text}
                        </div>
                    )}

                    <form onSubmit={handleSubmit}>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Resource ID</label>
                                <input
                                    type="text"
                                    value={formData.resourceId}
                                    onChange={(e) => setFormData({ ...formData, resourceId: e.target.value })}
                                    style={styles.input}
                                    placeholder="e.g., doc-123"
                                    required
                                />
                            </div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Resource Type</label>
                                <select
                                    value={formData.resourceType}
                                    onChange={(e) => setFormData({ ...formData, resourceType: e.target.value })}
                                    style={styles.input}
                                >
                                    <option value="DOCUMENT">Document</option>
                                    <option value="DATABASE">Database</option>
                                    <option value="SERVER">Server</option>
                                    <option value="API">API</option>
                                </select>
                            </div>
                        </div>

                        <div style={styles.formGroup}>
                            <label style={styles.label}>Justification</label>
                            <textarea
                                value={formData.reason}
                                onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
                                style={{ ...styles.input, minHeight: '80px', resize: 'vertical' }}
                                placeholder="Explain why you need temporary access..."
                                required
                            />
                        </div>

                        <div style={styles.formGroup}>
                            <label style={styles.label}>Duration (minutes)</label>
                            <input
                                type="number"
                                value={formData.durationMinutes}
                                onChange={(e) => setFormData({ ...formData, durationMinutes: parseInt(e.target.value) })}
                                style={styles.input}
                                min="1"
                                max="120"
                                required
                            />
                        </div>

                        <button type="submit" disabled={loading} style={{ ...styles.btn, ...styles.btnPrimary }}>
                            {loading ? '⏳ Submitting...' : '📤 Submit Request'}
                        </button>
                    </form>
                </div>

                <div style={styles.card}>
                    <div style={styles.cardHeader}>
                        <h2 style={{ margin: 0, fontSize: '20px', color: '#1e293b' }}>📋 My Access History</h2>
                    </div>

                    {myRequests.length === 0 ? (
                        <div style={{ textAlign: 'center', padding: '40px', color: '#64748b' }}>
                            <div style={{ fontSize: '48px', marginBottom: '16px' }}>📭</div>
                            <div>No access requests yet</div>
                        </div>
                    ) : (
                        <div style={{ display: 'grid', gap: '16px' }}>
                            {myRequests.map(req => (
                                <div key={req.id} style={{
                                    padding: '20px',
                                    background: '#f8fafc',
                                    borderRadius: '12px',
                                    border: '2px solid #e2e8f0',
                                }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '12px' }}>
                                        <div>
                                            <div style={{ fontWeight: '700', fontSize: '16px', color: '#1e293b', marginBottom: '4px' }}>
                                                {req.resourceType}: {req.resourceId}
                                            </div>
                                            <div style={{ fontSize: '14px', color: '#64748b' }}>
                                                Duration: {req.durationMinutes} minutes
                                            </div>
                                        </div>
                                        <div style={{
                                            ...styles.badge,
                                            ...(req.status === 'APPROVED' ? styles.badgeSuccess :
                                                req.status === 'PENDING' ? styles.badgeWarning :
                                                    req.status === 'REJECTED' ? styles.badgeDanger : styles.badgeInfo)
                                        }}>
                                            {req.status}
                                        </div>
                                    </div>

                                    <div style={{ fontSize: '14px', color: '#475569', marginBottom: '12px' }}>
                                        <strong>Reason:</strong> {req.reason}
                                    </div>

                                    <div style={{ fontSize: '13px', color: '#64748b' }}>
                                        Requested: {new Date(req.grantedAt).toLocaleString()}
                                        {req.expiresAt && ` • Expires: ${new Date(req.expiresAt).toLocaleString()}`}
                                    </div>

                                    {req.status === 'APPROVED' && !req.revoked && (
                                        <button
                                            onClick={() => handleRevoke(req.id)}
                                            style={{ ...styles.btn, ...styles.btnDanger, marginTop: '12px' }}
                                        >
                                            🚫 Revoke Access
                                        </button>
                                    )}
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </Layout>
    );
}