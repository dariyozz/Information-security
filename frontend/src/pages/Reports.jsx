import { useState, useEffect } from 'react';
import api from '../services/api';
import { Layout } from '../components/Layout';
import styles from '../styles';

export default function Reports() {
    const [stats, setStats] = useState({ totalUsers: 0, totalAccessRequests: 0 });

    useEffect(() => {
        loadStats();
    }, []);

    const loadStats = async () => {
        try {
            const response = await api.reports.getStats();
            if (response.success) {
                setStats(response.data);
            }
        } catch (err) {
            console.error(err);
        }
    };

    return (
        <Layout>
            <div>
                <h1 style={{ fontSize: '32px', color: '#1e293b', marginBottom: '32px' }}>
                    📈 System Reports
                </h1>

                <div style={styles.statsGrid}>
                    <div style={styles.statCard}>
                        <div style={{ fontSize: '14px', color: '#64748b', marginBottom: '8px' }}>Total Users</div>
                        <div style={{ fontSize: '32px', fontWeight: '700', color: '#1e293b' }}>
                            {stats.totalUsers}
                        </div>
                    </div>
                    <div style={styles.statCard}>
                        <div style={{ fontSize: '14px', color: '#64748b', marginBottom: '8px' }}>Total JIT Requests</div>
                        <div style={{ fontSize: '32px', fontWeight: '700', color: '#1e293b' }}>
                            {stats.totalAccessRequests}
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
}
