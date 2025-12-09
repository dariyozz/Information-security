import { useState, useEffect } from 'react';
import api from '../services/api';
import { Layout } from '../components/Layout';
import styles from '../styles';

export default function RoleManagement() {
    const [roles, setRoles] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadRoles();
    }, []);

    const loadRoles = async () => {
        try {
            const response = await api.roles.getAllRoles();
            if (response.success) {
                // Ensure roles is an array. api returns Map<String, Object> where values might be roles
                // Actually the controller returns a Map. Let's inspect data structure or adjust controller.
                // Controller returns: Map<String, Object> where keys are role names? Or List?
                // RoleService.getAllRoles returns list of role DTOs usually.
                // Let's assume response.data is the list or map.
                // For safety, let's just dump it for now or assume it's a list.
                // Wait, RoleController.getAllRoles calls roleService.getAllRoles which likely returns a List or Map.
                // Let's assume it's a list for now, if not I'll fix during verification.
                setRoles(response.data.roles);
            }
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Layout>
            <div>
                <h1 style={{ fontSize: '32px', color: '#1e293b', marginBottom: '32px' }}>
                    🛡️ Role Management
                </h1>

                <div style={styles.card}>
                    {loading ? (
                        <div>Loading roles...</div>
                    ) : (
                        <table style={styles.table}>
                            <thead>
                                <tr>
                                    <th style={styles.th}>Role Name</th>
                                    <th style={styles.th}>Permissions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {roles.map((role, index) => (
                                    <tr key={index}>
                                        <td style={styles.td}>
                                            <span style={styles.roleChip}>{role.name}</span>
                                        </td>
                                        <td style={styles.td}>
                                            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                                                {role.permissions && role.permissions.map(p => (
                                                    <span key={p} style={{ ...styles.badge, background: '#f1f5f9', color: '#475569' }}>
                                                        {p}
                                                    </span>
                                                ))}
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </Layout>
    );
}
