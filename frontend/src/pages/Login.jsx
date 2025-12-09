import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from "../services/api.js";
import styles from "../styles.js";

export default function LoginPage() {
    const navigate = useNavigate();
    const { login } = useAuth();
    const [step, setStep] = useState(1);
    const [formData, setFormData] = useState({ username: '', password: '', email: '' });
    const [code, setCode] = useState('');
    const [message, setMessage] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);

        try {
            const response = await api.auth.login(formData);
            if (response.success) {
                setMessage({ type: 'success', text: response.message });
                setStep(2);
            } else {
                setMessage({ type: 'error', text: response.message });
            }
        } catch (error) {
            setMessage({ type: 'error', text: 'Login failed' });
        } finally {
            setLoading(false);
        }
    };

    const handleVerifyEmail = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);

        try {
            const response = await api.auth.verifyEmail({ email: formData.email, code });
            if (response.success) {
                setMessage({ type: 'success', text: 'Email verified successfully! You can now login.' });
                setTimeout(() => setStep(1), 2000);
            } else {
                setMessage({ type: 'error', text: response.message });
            }
        } catch (error) {
            setMessage({ type: 'error', text: 'Email verification failed' });
        } finally {
            setLoading(false);
        }
    };

    const handleResendCode = async () => {
        if (!formData.email) {
            setMessage({ type: 'error', text: 'Please enter your email address' });
            return;
        }

        setLoading(true);
        setMessage(null);

        try {
            const response = await api.auth.resendCode(formData.email);
            if (response.success) {
                setMessage({ type: 'success', text: response.message });
            } else {
                setMessage({ type: 'error', text: response.message });
            }
        } catch (error) {
            setMessage({ type: 'error', text: 'Failed to resend code' });
        } finally {
            setLoading(false);
        }
    };
    const handleVerify2FA = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);

        try {
            const response = await api.auth.verify2FA({ username: formData.username, code });
            if (response.success) {
                login(response.data.user);
                navigate('/dashboard');
            } else {
                setMessage({ type: 'error', text: response.message });
            }
        } catch (error) {
            setMessage({ type: 'error', text: '2FA verification failed' });
        } finally {
            setLoading(false);
        }
    };
    return (
        <div style={{ ...styles.container, ...styles.authContainer }}>
            <div style={styles.authCard}>
                <div style={{ textAlign: 'center', marginBottom: '32px' }}>
                    <div style={{ fontSize: '48px', marginBottom: '16px' }}>🔐</div>
                    <h1 style={{ margin: 0, fontSize: '28px', color: '#1e293b' }}>
                        {step === 1 ? 'Welcome Back' : step === 2 ? 'Two-Factor Auth' : 'Verify Email'}
                    </h1>
                    <p style={{ color: '#64748b', marginTop: '8px' }}>
                        {step === 1 ? 'Sign in to your account' : step === 2 ? 'Enter the code sent to your email' : 'Enter your username and verification code'}
                    </p>
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

                {step === 1 && (
                    <form onSubmit={handleLogin}>
                        <div style={styles.formGroup}>
                            <label style={styles.label}>Username</label>
                            <input
                                type="text"
                                value={formData.username}
                                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                                style={styles.input}
                                required
                            />
                        </div>
                        <div style={styles.formGroup}>
                            <label style={styles.label}>Password</label>
                            <input
                                type="password"
                                value={formData.password}
                                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                                style={styles.input}
                                required
                            />
                        </div>
                        <button
                            type="submit"
                            disabled={loading}
                            style={{ ...styles.btn, ...styles.btnPrimary, width: '100%', justifyContent: 'center' }}
                        >
                            {loading ? 'Signing in...' : 'Sign In'}
                        </button>
                    </form>
                )}

                {step === 2 && (
                    <form onSubmit={handleVerify2FA}>
                        <div style={styles.formGroup}>
                            <label style={styles.label}>2FA Code</label>
                            <input
                                type="text"
                                value={code}
                                onChange={(e) => setCode(e.target.value)}
                                style={styles.input}
                                maxLength={6}
                                placeholder="Enter 6-digit code"
                                required
                            />
                        </div>
                        <button
                            type="submit"
                            disabled={loading}
                            style={{ ...styles.btn, ...styles.btnPrimary, width: '100%', justifyContent: 'center' }}
                        >
                            {loading ? 'Verifying...' : 'Verify & Sign In'}
                        </button>
                    </form>
                )}

                {step === 3 && (
                    <form onSubmit={handleVerifyEmail}>
                        <div style={styles.formGroup}>
                            <label style={styles.label}>Email Address</label>
                            <input
                                type="email"
                                value={formData.email}
                                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                style={styles.input}
                                placeholder="Enter your email"
                                required
                            />
                        </div>
                        <div style={styles.formGroup}>
                            <label style={styles.label}>Verification Code</label>
                            <input
                                type="text"
                                value={code}
                                onChange={(e) => setCode(e.target.value)}
                                style={styles.input}
                                maxLength={6}
                                placeholder="Enter 6-digit code"
                                required
                            />
                        </div>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                            <button
                                type="submit"
                                disabled={loading}
                                style={{ ...styles.btn, ...styles.btnPrimary, width: '100%', justifyContent: 'center' }}
                            >
                                {loading ? 'Verifying...' : 'Verify Email'}
                            </button>
                            <button
                                type="button"
                                onClick={handleResendCode}
                                disabled={loading}
                                style={{ ...styles.btn, background: 'none', border: '1px solid #cbd5e1', color: '#64748b', width: '100%', justifyContent: 'center' }}
                            >
                                Resend Code
                            </button>
                        </div>
                    </form>
                )}

                <div style={{ textAlign: 'center', marginTop: '24px', fontSize: '14px', color: '#64748b', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    <div>
                        Don't have an account? <Link to="/register" style={{ color: '#667eea', fontWeight: '600' }}>Sign up</Link>
                    </div>
                    <div>
                        {step === 1 ? (
                            <span
                                onClick={() => { setStep(3); setMessage(null); }}
                                style={{ color: '#64748b', textDecoration: 'underline', cursor: 'pointer' }}
                            >
                                Verify email
                            </span>
                        ) : (
                            <span
                                onClick={() => { setStep(1); setMessage(null); }}
                                style={{ color: '#64748b', textDecoration: 'underline', cursor: 'pointer' }}
                            >
                                Back to login
                            </span>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}