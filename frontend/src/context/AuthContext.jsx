import React, { useState, useEffect } from 'react';
import api from "../services/api.js";

const AuthContext = React.createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        checkAuth();
    }, []);

    const checkAuth = async () => {
        try {
            const response = await api.auth.getCurrentUser();
            if (response.success) {
                setUser(response.data.user);
            }
        } catch (error) {
            setUser(null);
        } finally {
            setLoading(false);
        }
    };

    const login = (userData) => setUser(userData);
    const logout = async () => {
        await api.auth.logout();
        setUser(null);
    };

    const hasRole = (roleName) => user?.roles?.some(r => r.name === roleName) || false;
    const isAdmin = hasRole('ADMIN');
    const isManager = hasRole('MANAGER');
    const isUser = hasRole('USER');

    return (
        <AuthContext.Provider value={{ user, loading, login, logout, checkAuth, hasRole, isAdmin, isManager, isUser }}>
            {children}
        </AuthContext.Provider>
    );
}

 export const useAuth = () => {
    const context = React.useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
};
