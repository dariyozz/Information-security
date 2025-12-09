import { useState } from 'react';
import {useAuth} from "../context/AuthContext.jsx";
import api from "../services/api.js";
import styles from "../styles.js";
import {Layout} from "../components/Layout.jsx";

export default function ProtectedResources() {
    const { isAdmin, isManager } = useAuth();
    const [results, setResults] = useState({});
    const [loading, setLoading] = useState({});

    const testResource = async (name, apiFn) => {
        setLoading(prev => ({ ...prev, [name]: true }));
        try {
            const response = await apiFn();
            setResults(prev => ({ ...prev, [name]: { success: response.success, ...response } }));
        } catch (error) {
            setResults(prev => ({ ...prev, [name]: { success: false, message: error.message } }));
        } finally {
            setLoading(prev => ({ ...prev, [name]: false }));
        }
    };

    const resources = [
        { id: 'admin', name: 'Admin Resource', icon: '🔱', api: api.resources.getAdmin, required: 'ADMIN role' },
        { id: 'manager', name: 'Manager Resource', icon: '⚡', api: api.resources.getManager, required: 'MANAGER role or higher' },
        { id: 'user', name: 'User Resource', icon: '👤', api: api.resources.getUser, required: 'USER role' },
        { id: 'document', name: 'Document (doc-123)', icon: '📄', api: () => api.resources.getDocument('doc-123'), required: 'READ permission or JIT access' },
    ];

    return (
        <Layout>
            <div>
                <h1 style={{ fontSize: '32px', color: '#1e293b', marginBottom: '32px' }}>
                    🔒 Protected Resources
                </h1>

                <div style={{ display: 'grid', gap: '20px' }}>
                    {resources.map(resource => (
                        <div key={resource.id} style={styles.card}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '16px' }}>
                                <div style={{ fontSize: '40px' }}>{resource.icon}</div>
                                <div style={{ flex: 1 }}>
                                    <h3 style={{ margin: 0, fontSize: '18px', color: '#1e293b' }}>{resource.name}</h3>
                                    <p style={{ margin: '4px 0 0 0', fontSize: '14px', color: '#64748b' }}>
                                        Requires: {resource.required}
                                    </p>
                                </div>
                                <button
                                    onClick={() => testResource(resource.id, resource.api)}
                                    disabled={loading[resource.id]}
                                    style={{ ...styles.btn, ...styles.btnPrimary }}
                                >
                                    {loading[resource.id] ? '⏳ Testing...' : '🧪 Test Access'}
                                </button>
                            </div>

                            {results[resource.id] && (
                                <div style={{
                                    padding: '16px',
                                    borderRadius: '8px',
                                    background: results[resource.id].success ? '#d1fae5' : '#fee2e2',
                                    border: `2px solid ${results[resource.id].success ? '#10b981' : '#ef4444'}`,
                                }}>
                                    <div style={{ fontWeight: '600', marginBottom: '8px', color: results[resource.id].success ? '#065f46' : '#991b1b' }}>
                                        {results[resource.id].success ? '✅ Access Granted' : '❌ Access Denied'}
                                    </div>
                                    <div style={{ fontSize: '14px', color: results[resource.id].success ? '#065f46' : '#991b1b' }}>
                                        {results[resource.id].message}
                                    </div>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            </div>
        </Layout>
    );
}