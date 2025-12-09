import { useState, useEffect } from 'react';
import api from '../services/api';
import { Layout } from '../components/Layout';
import styles from '../styles';

export default function UserManagement() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Role Management Modal State
    const [selectedUser, setSelectedUser] = useState(null);
    const [userRoles, setUserRoles] = useState([]);
    const [allRoles, setAllRoles] = useState([]);
    const [roleLoading, setRoleLoading] = useState(false);
    const [roleError, setRoleError] = useState(null);

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        try {
            const response = await api.users.getAll();
            if (response.success) {
                setUsers(response.data);
            } else {
                setError(response.message);
            }
        } catch (err) {
            setError('Failed to load users');
        } finally {
            setLoading(false);
        }
    };

    const handleBlock = async (userId, isBlocked) => {
        try {
            const action = isBlocked ? api.users.unblock : api.users.block;
            await action(userId);
            loadUsers(); // Reload list
        } catch (err) {
            alert('Action failed');
        }
    };

    const openRoleModal = async (user) => {
        setSelectedUser(user);
        setRoleLoading(true);
        setRoleError(null);
        try {
            const [rolesRes, allRolesRes] = await Promise.all([
                api.roles.getUserRoles(user.id),
                api.roles.getAllRoles()
            ]);

            if (rolesRes.success) setUserRoles(rolesRes.data.roles);
            if (allRolesRes.success) setAllRoles(allRolesRes.data.roles);
        } catch (err) {
            setRoleError("Failed to load roles");
        } finally {
            setRoleLoading(false);
        }
    };

    const closeRoleModal = () => {
        setSelectedUser(null);
        setUserRoles([]);
    };

    const handleAssignRole = async (roleName) => {
        if (!selectedUser) return;
        try {
            const res = await api.roles.assignRole(selectedUser.id, roleName);
            if (res.success) {
                // Refresh user roles
                const rolesRes = await api.roles.getUserRoles(selectedUser.id);
                if (rolesRes.success) setUserRoles(rolesRes.data.roles);
            } else {
                alert(res.message);
            }
        } catch (err) {
            alert("Failed to assign role");
        }
    };

    const handleRevokeRole = async (roleName) => {
        if (!selectedUser) return;
        if (!confirm(`Revoke ${roleName} from ${selectedUser.username}?`)) return;
        try {
            const res = await api.roles.revokeRole(selectedUser.id, roleName);
            if (res.success) {
                // Refresh user roles
                const rolesRes = await api.roles.getUserRoles(selectedUser.id);
                if (rolesRes.success) setUserRoles(rolesRes.data.roles);
            } else {
                alert(res.message);
            }
        } catch (err) {
            alert("Failed to revoke role");
        }
    };

    // Derived state for available roles to add
    const availableRolesToAdd = allRoles.filter(
        ar => !userRoles.some(ur => ur.name === ar.name)
    );

    return (
        <Layout>
            <div>
                <h1 style={{ fontSize: '32px', color: '#1e293b', marginBottom: '32px' }}>
                    👥 User Management
                </h1>

                {error && (
                    <div style={styles.alertError}>
                        {error}
                    </div>
                )}

                <div style={styles.card}>
                    {loading ? (
                        <div>Loading users...</div>
                    ) : (
                        <table style={styles.table}>
                            <thead>
                                <tr>
                                    <th style={styles.th}>ID</th>
                                    <th style={styles.th}>Username</th>
                                    <th style={styles.th}>Email</th>
                                    <th style={styles.th}>Verified</th>
                                    <th style={styles.th}>Status</th>
                                    <th style={styles.th}>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {users.map(user => (
                                    <tr key={user.id}>
                                        <td style={styles.td}>{user.id}</td>
                                        <td style={styles.td}>{user.username}</td>
                                        <td style={styles.td}>{user.email}</td>
                                        <td style={styles.td}>
                                            {user.emailVerified ? '✅' : '❌'}
                                        </td>
                                        <td style={styles.td}>
                                            <span style={user.blocked ? styles.badgeDanger : styles.badgeSuccess}>
                                                {user.blocked ? 'Blocked' : 'Active'}
                                            </span>
                                        </td>
                                        <td style={styles.td}>
                                            <div style={{ display: 'flex', gap: '8px' }}>
                                                <button
                                                    onClick={() => handleBlock(user.id, user.blocked)}
                                                    style={{
                                                        ...styles.btn,
                                                        ...(user.blocked ? styles.btnSuccess : styles.btnDanger),
                                                        padding: '6px 12px',
                                                        fontSize: '12px'
                                                    }}
                                                >
                                                    {user.blocked ? 'Unblock' : 'Block'}
                                                </button>
                                                <button
                                                    onClick={() => openRoleModal(user)}
                                                    style={{
                                                        ...styles.btn,
                                                        background: '#e2e8f0',
                                                        color: '#475569',
                                                        padding: '6px 12px',
                                                        fontSize: '12px'
                                                    }}
                                                >
                                                    Roles
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>

                {/* Role Management Modal */}
                {selectedUser && (
                    <div style={{
                        position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
                        background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center',
                        zIndex: 1000
                    }}>
                        <div style={{
                            background: 'white', padding: '24px', borderRadius: '12px',
                            width: '500px', maxWidth: '90%', maxHeight: '80vh', overflowY: 'auto'
                        }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                                <h2 style={{ margin: 0, fontSize: '20px', color:"black" }}>Manage Roles: {selectedUser.username}</h2>
                                <button onClick={closeRoleModal} style={{ background: 'none', border: 'none', fontSize: '34px', cursor: 'pointer', color:"black" }}>×</button>
                            </div>

                            {roleLoading ? (
                                <div>Loading roles...</div>
                            ) : (
                                <div>
                                    <div style={{ marginBottom: '20px' }}>
                                        <h3 style={{ fontSize: '14px', color: '#64748b', marginBottom: '10px' }}>Current Roles</h3>
                                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                                            {userRoles.length === 0 && <div style={{ color: '#94a3b8', fontStyle: 'italic' }}>No roles assigned</div>}
                                            {userRoles.map(role => (
                                                <div key={role.name} style={{
                                                    background: '#eff6ff', color: '#1d4ed8', padding: '4px 12px',
                                                    borderRadius: '16px', fontSize: '14px', display: 'flex', alignItems: 'center', gap: '8px'
                                                }}>
                                                    {role.name}
                                                    <span
                                                        onClick={() => handleRevokeRole(role.name)}
                                                        style={{ cursor: 'pointer', fontWeight: 'bold' }}
                                                        title="Remove role"
                                                    >
                                                        ×
                                                    </span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>

                                    <div>
                                        <h3 style={{ fontSize: '14px', color: '#64748b', marginBottom: '10px' }}>Add Role</h3>
                                        <div style={{ display: 'flex', gap: '8px' }}>
                                            <select
                                                style={{ ...styles.input, flex: 1 }}
                                                onChange={(e) => {
                                                    if (e.target.value) handleAssignRole(e.target.value);
                                                    e.target.value = ""; // Reset select
                                                }}
                                            >
                                                <option value="">Select a role to assign...</option>
                                                {availableRolesToAdd.map(role => (
                                                    <option key={role.name} value={role.name}>
                                                        {role.name}
                                                    </option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>
                            )}

                            <div style={{ marginTop: '24px', textAlign: 'right' }}>
                                <button onClick={closeRoleModal} style={{ ...styles.btn, background: '#f1f5f9', color: '#475569' }}>
                                    Done
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </Layout>
    );
}
