import { useState } from 'react';
import { authAPI } from '../services/api';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Auth.css';

export default function Login() {
    const navigate = useNavigate();
    const { login } = useAuth();
    const [step, setStep] = useState(1); // 1: password, 2: 2FA
    const [formData, setFormData] = useState({
        username: '',
        password: '',
    });
    const [twoFACode, setTwoFACode] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setMessage('');

        try {
            const response = await authAPI.login(formData);
            if (response.data.success) {
                setMessage(response.data.message);
                setStep(2);
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Login failed');
        } finally {
            setLoading(false);
        }
    };

    const handleVerify2FA = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setMessage('');

        try {
            const response = await authAPI.verify2FA({
                username: formData.username,
                code: twoFACode,
            });
            if (response.data.success) {
                login(response.data.data);
                navigate('/dashboard');
            }
        } catch (err) {
            setError(err.response?.data?.message || '2FA verification failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h1>Login</h1>

                {step === 1 ? (
                    <form onSubmit={handleLogin}>
                        <div className="form-group">
                            <label>Username</label>
                            <input
                                type="text"
                                name="username"
                                value={formData.username}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label>Password</label>
                            <input
                                type="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                required
                            />
                        </div>

                        {error && <div className="error-message">{error}</div>}
                        {message && <div className="success-message">{message}</div>}

                        <button type="submit" disabled={loading}>
                            {loading ? 'Logging in...' : 'Login'}
                        </button>

                        <p className="auth-link">
                            Don't have an account? <a href="/register">Register</a>
                        </p>
                        <p className="auth-link">
                            Have a code? <a href="/verify-email">Verify Email</a>
                        </p>
                    </form>
                ) : (
                    <form onSubmit={handleVerify2FA}>
                        <div className="info-box">
                            <p>A 2FA code has been sent to your email (check console logs).</p>
                        </div>

                        <div className="form-group">
                            <label>2FA Code</label>
                            <input
                                type="text"
                                value={twoFACode}
                                onChange={(e) => setTwoFACode(e.target.value)}
                                required
                                maxLength={6}
                                placeholder="Enter 6-digit code"
                            />
                        </div>

                        {error && <div className="error-message">{error}</div>}
                        {message && <div className="success-message">{message}</div>}

                        <button type="submit" disabled={loading}>
                            {loading ? 'Verifying...' : 'Verify 2FA'}
                        </button>
                    </form>
                )}
            </div>
        </div>
    );
}
