import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { authAPI } from '../services/api';
import './Auth.css';

export default function VerifyEmail() {
    const navigate = useNavigate();
    const location = useLocation();

    // Step 1: Request Code (Enter Email), Step 2: Verify Code
    const [step, setStep] = useState(1);
    const [email, setEmail] = useState(location.state?.email || '');
    const [code, setCode] = useState('');
    const [message, setMessage] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleSendCode = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);

        try {
            const response = await authAPI.resendCode(email);
            if (response.data.success) {
                setMessage({ type: 'success', text: 'Verification code sent! Please check your email.' });
                setStep(2);
            }
        } catch (error) {
            setMessage({ type: 'error', text: error.response?.data?.message || 'Failed to send code' });
        } finally {
            setLoading(false);
        }
    };

    const handleVerify = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);

        try {
            const response = await authAPI.verifyEmail({ email, code });
            if (response.data.success) {
                setMessage({ type: 'success', text: 'Email verified! Redirecting to login...' });
                setTimeout(() => navigate('/login'), 2000);
            }
        } catch (error) {
            setMessage({ type: 'error', text: error.response?.data?.message || 'Verification failed' });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h1>Verify Email</h1>
                <div className="steps-indicator">
                    <div className={`step ${step === 1 ? 'active' : 'completed'}`}>1. Request</div>
                    <div className="line"></div>
                    <div className={`step ${step === 2 ? 'active' : ''}`}>2. Verify</div>
                </div>

                {step === 1 ? (
                    <form onSubmit={handleSendCode}>
                        <p className="instruction-text">
                            Enter your email address to receive a verification code.
                        </p>
                        <div className="form-group">
                            <label>Email Address</label>
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="name@example.com"
                                required
                            />
                        </div>
                        {message && <div className={`message ${message.type}`}>{message.text}</div>}
                        <button type="submit" disabled={loading}>
                            {loading ? 'Sending Code...' : 'Send Verification Code'}
                        </button>
                    </form>
                ) : (
                    <form onSubmit={handleVerify}>
                        <p className="instruction-text">
                            Enter the code sent to <strong>{email}</strong>.
                            <br />
                            <button type="button" className="link-btn" onClick={() => setStep(1)}>Change email</button>
                        </p>
                        <div className="form-group">
                            <label>Verification Code</label>
                            <input
                                type="text"
                                value={code}
                                onChange={(e) => setCode(e.target.value)}
                                placeholder="Enter 6-digit code"
                                required
                                maxLength={6}
                            />
                        </div>
                        {message && <div className={`message ${message.type}`}>{message.text}</div>}
                        <button type="submit" disabled={loading}>
                            {loading ? 'Verifying...' : 'Verify Email'}
                        </button>
                        <button
                            type="button"
                            className="secondary-btn"
                            onClick={handleSendCode}
                            disabled={loading}
                        >
                            Resend Code
                        </button>
                    </form>
                )}

                <p className="auth-link">
                    Back to <a href="/login">Login</a>
                </p>
            </div>
        </div>
    );
}
