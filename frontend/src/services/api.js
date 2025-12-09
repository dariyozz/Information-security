import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

const api = {
    async request(endpoint, options = {}) {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            ...options,
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
        });
        return response.json();
    },

    auth: {
        register: (data) => api.request('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
        verifyEmail: (data) => api.request('/auth/verify-email', { method: 'POST', body: JSON.stringify(data) }),
        resendCode: (email) => api.request('/auth/resend-code', { method: 'POST', body: JSON.stringify({ email }) }),
        login: (data) => api.request('/auth/login', { method: 'POST', body: JSON.stringify(data) }),
        verify2FA: (data) => api.request('/auth/verify-2fa', { method: 'POST', body: JSON.stringify(data) }),
        logout: () => api.request('/auth/logout', { method: 'POST' }),
        getCurrentUser: () => api.request('/auth/me'),
    },

    roles: {
        getUserRoles: (userId) => api.request(`/roles/user/${userId}`),
        getAllRoles: () => api.request('/roles/all'),
        assignRole: (userId, roleName) => api.request(`/roles/assign?userId=${userId}&roleName=${roleName}`, { method: 'POST' }),
        revokeRole: (userId, roleName) => api.request(`/roles/revoke?userId=${userId}&roleName=${roleName}`, { method: 'DELETE' }),
    },

    resources: {
        getAdmin: () => api.request('/resources/admin'),
        getManager: () => api.request('/resources/manager'),
        getUser: () => api.request('/resources/user'),
        getDocument: (id) => api.request(`/resources/document/${id}`),
    },

    jit: {
        request: (data) => api.request('/jit/request', { method: 'POST', body: JSON.stringify(data) }),
        getMyAccess: () => api.request('/jit/my-access'),
        getPending: () => api.request('/jit/pending'),
        approve: (id) => api.request(`/jit/approve/${id}`, { method: 'POST' }),
        reject: (id) => api.request(`/jit/reject/${id}`, { method: 'POST' }),
        revoke: (id) => api.request(`/jit/revoke/${id}`, { method: 'POST' }),
    },

    users: {
        getAll: () => api.request('/users/all'),
        block: (id) => api.request(`/users/${id}/block`, { method: 'POST' }),
        unblock: (id) => api.request(`/users/${id}/unblock`, { method: 'POST' }),
    },

    reports: {
        getStats: () => api.request('/reports/stats'),
    },
};
export default api;
