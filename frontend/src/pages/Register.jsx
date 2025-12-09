import { useState } from 'react';
import api from '../services/api';
import {Link, useNavigate} from 'react-router-dom';
import styles from "../styles.js";

export default function RegisterPage() {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [formData, setFormData] = useState({ username: '', email: '', password: '' });
    const [code, setCode] = useState('');
    const [message, setMessage] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleRegister = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);

        try {
            const response = await api.auth.register(formData);
            if (response.success) {
                setMessage({ type: 'success', text: response.message });
                setStep(2);
            } else {
                setMessage({ type: 'error', text: response.message });
            }
        } catch (error) {
            setMessage({ type: 'error', text: 'Registration failed' });
        } finally {
            setLoading(false);
        }
    };

    const handleVerify = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);

        try {
            const response = await api.auth.verifyEmail({ email: formData.email, code });
            if (response.success) {
                setMessage({ type: 'success', text: 'Email verified! Redirecting...' });
                setTimeout(() => navigate('/login'), 2000);
            } else {
                setMessage({ type: 'error', text: response.message });
            }
        } catch (error) {
            setMessage({ type: 'error', text: 'Verification failed' });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ ...styles.container, ...styles.authContainer }}>
            <div style={styles.authCard}>
                <div style={{ textAlign: 'center', marginBottom: '32px' }}>
                    <div style={{ fontSize: '48px', marginBottom: '16px' }}>✨</div>
                    <h1 style={{ margin: 0, fontSize: '28px', color: '#1e293b' }}>
                        {step === 1 ? 'Create Account' : 'Verify Email'}
                    </h1>
                    <p style={{ color: '#64748b', marginTop: '8px' }}>
                        {step === 1 ? 'Sign up for a new account' : 'Check your email for verification code'}
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

                {step === 1 ? (
                    <form onSubmit={handleRegister}>
                        <div style={styles.formGroup}>
                            <label style={styles.label}>Username</label>
                            <input
                                type="text"
                                value={formData.username}
                                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                                style={styles.input}
                                minLength={3}
                                required
                            />
                        </div>
                        <div style={styles.formGroup}>
                            <label style={styles.label}>Email</label>
                            <input
                                type="email"
                                value={formData.email}
                                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
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
                                minLength={6}
                                required
                            />
                        </div>
                        <button
                            type="submit"
                            disabled={loading}
                            style={{ ...styles.btn, ...styles.btnPrimary, width: '100%', justifyContent: 'center' }}
                        >
                            {loading ? 'Creating Account...' : 'Create Account'}
                        </button>
                    </form>
                ) : (
                    <form onSubmit={handleVerify}>
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
                        <button
                            type="submit"
                            disabled={loading}
                            style={{ ...styles.btn, ...styles.btnPrimary, width: '100%', justifyContent: 'center' }}
                        >
                            {loading ? 'Verifying...' : 'Verify Email'}
                        </button>
                    </form>
                )}

                <div style={{ textAlign: 'center', marginTop: '24px', fontSize: '14px', color: '#64748b' }}>
                    Already have an account? <Link to="/login" style={{ color: '#667eea', fontWeight: '600' }}>Sign in</Link>
                </div>
            </div>
        </div>
    );
}