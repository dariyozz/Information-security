import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedResources from './pages/ProtectedResources';
import './App.css';
import styles from "./styles.js";
import LoginPage from "./pages/Login";
import RegisterPage from "./pages/Register";
import DashboardPage from "./pages/Dashboard";
import JitAccessPage from "./pages/JitAccess";
import AdminRequestsPage from "./pages/AdminPage.jsx";
import UserManagement from "./pages/UserManagement";
import RoleManagement from "./pages/RoleManagement";
import Reports from "./pages/Reports";

function PrivateRoute({ children }) {
    const { user, loading } = useAuth();

    if (loading) {
        return (
            <div style={{ ...styles.container, display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh' }}>
                <div style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '48px', marginBottom: '16px' }}>⏳</div>
                    <div style={{ color: 'white', fontSize: '18px' }}>Loading...</div>
                </div>
            </div>
        );
    }

    return user ? children : <Navigate to="/login" />;
}

export default function App() {
    return (
        <div style={styles.container}>
            <AuthProvider>
                <BrowserRouter>
                    <Routes>
                        <Route path="/login" element={<LoginPage />} />
                        <Route path="/register" element={<RegisterPage />} />
                        <Route path="/dashboard" element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
                        <Route path="/resources" element={<PrivateRoute><ProtectedResources /></PrivateRoute>} />
                        <Route path="/jit-access" element={<PrivateRoute><JitAccessPage /></PrivateRoute>} />
                        <Route path="/admin/requests" element={<PrivateRoute><AdminRequestsPage /></PrivateRoute>} />
                        <Route path="/admin/users" element={<PrivateRoute><UserManagement /></PrivateRoute>} />
                        <Route path="/admin/roles" element={<PrivateRoute><RoleManagement /></PrivateRoute>} />
                        <Route path="/admin/reports" element={<PrivateRoute><Reports /></PrivateRoute>} />
                        <Route path="/" element={<Navigate to="/dashboard" />} />
                    </Routes>
                </BrowserRouter>
            </AuthProvider>
        </div>
    );
}