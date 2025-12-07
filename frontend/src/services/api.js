import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true, // Important for cookies
    headers: {
        'Content-Type': 'application/json',
    },
});

export const authAPI = {
    register: (data) => api.post('/auth/register', data),
    verifyEmail: (data) => api.post('/auth/verify-email', data),
    resendCode: (email) => api.post('/auth/resend-code', { email }),
    login: (data) => api.post('/auth/login', data),
    verify2FA: (data) => api.post('/auth/verify-2fa', data),
    logout: () => api.post('/auth/logout'),
    getCurrentUser: () => api.get('/auth/me'),
};

export const roleAPI = {
    assignRole: (userId, roleName) => api.post(`/roles/assign?userId=${userId}&roleName=${roleName}`),
    revokeRole: (userId, roleName) => api.delete(`/roles/revoke?userId=${userId}&roleName=${roleName}`),
    getUserRoles: (userId) => api.get(`/roles/user/${userId}`),
    getAllRoles: () => api.get('/roles/all'),
};

export const resourceAPI = {
    getAdminResource: () => api.get('/resources/admin'),
    getManagerResource: () => api.get('/resources/manager'),
    getUserResource: () => api.get('/resources/user'),
    getDocument: (id) => api.get(`/resources/document/${id}`),
};

export const jitAPI = {
    requestAccess: (data) => api.post('/jit/request', data),
    checkStatus: (resourceId) => api.get(`/jit/status/${resourceId}`),
    revokeAccess: (accessId) => api.post(`/jit/revoke/${accessId}`),
    getMyAccess: () => api.get('/jit/my-access'),
    getPendingRequests: () => api.get('/jit/pending'),
    approveAccess: (accessId) => api.post(`/jit/approve/${accessId}`),
    rejectAccess: (accessId) => api.post(`/jit/reject/${accessId}`),
};

export default api;
