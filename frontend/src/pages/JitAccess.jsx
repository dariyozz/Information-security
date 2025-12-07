import {useState, useEffect} from 'react';
import {jitAPI} from '../services/api';
import {useAuth} from '../context/AuthContext';
import './JitAccess.css';

export default function JitAccess() {
    const {user} = useAuth();
    const [myRequests, setMyRequests] = useState([]);
    const [pendingRequests, setPendingRequests] = useState([]);

    // Form state
    const [resourceId, setResourceId] = useState('');
    const [reason, setReason] = useState('');
    const [duration, setDuration] = useState(30);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState(null);

    useEffect(() => {
        fetchMyRequests();
        if (user?.role === 'admin') {
            fetchPendingRequests();
        }
    }, [user]);

    const fetchMyRequests = async () => {
        try {
            const response = await jitAPI.getMyRequests();
            setMyRequests(response.data);
        } catch (error) {
            console.error('Failed to fetch requests:', error);
        }
    };

    const fetchPendingRequests = async () => {
        try {
            const response = await jitAPI.getPendingRequests();
            setPendingRequests(response.data);
        } catch (error) {
            console.error('Failed to fetch pending requests:', error);
        }
    };

    const handleRequest = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage(null);

        try {
            await jitAPI.requestAccess({
                resourceId,
                reason,
                durationMinutes: parseInt(duration)
            });
            setMessage({type: 'success', text: 'Access request submitted successfully!'});
            setResourceId('');
            setReason('');
            setDuration(30);
            fetchMyRequests();
        } catch (error) {
            setMessage({type: 'error', text: error.response?.data?.error || 'Failed to submit request'});
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <div className="request-form-container">
                <form onSubmit={handleRequest}>
                <div className="form-group">
                    <label>Target Resource ID</label>
                    <input
                        type="text"
                        value={resourceId}
                        onChange={e => setResourceId(e.target.value)}
                        placeholder="e.g. DATABASE_PROD_01"
                        required
                    />
                </div>
                <div className="form-group">
                    <label>Justification</label>
                    <textarea
                        value={reason}
                        onChange={e => setReason(e.target.value)}
                        placeholder="Explain why you need temporary access..."
                        required
                    />
                </div>
                <div className="form-group">
                    <label>Required Duration (Minutes)</label>
                    <input
                        type="number"
                        value={duration}
                        min="1" max="120"
                        onChange={e => setDuration(e.target.value)}
                        required
                    />
                </div>
                <button type="submit" className="request-btn" disabled={loading}>
                    {loading ? 'Submitting...' : 'Submit Request'}
                </button>
                {message && (
                    <div className={`message ${message.type}`}>{message.text}</div>
                )}
            </form>
        </div>

        {/* Shared View: My History */}
        <section className="user-section">
            <div className="section-header">
                <h2>🕒 My Access History</h2>
            </div>
            <div className="section-body">
                {myRequests.length > 0 ? (
                    <div className="access-grid">
                        {myRequests.map(req => (
                            <RequestCard key={req.id} req={req} isAdminView={false}/>
                        ))}
                    </div>
                ) : (
                    <p className="empty-state">You haven't made any access requests yet.</p>
                )}
            </div>
        </section>
        </>
    );
}

function RequestCard({req, isAdminView}) {
    return (
        <div className="request-card">
            <div className="card-header">
                <span className={`status-badge ${req.status}`}>{req.status}</span>
                <span className="resource-id">{req.resourceId}</span>
            </div>
            <div className="card-body">
                <p><strong>Reason:</strong> {req.reason}</p>
                <p><strong>Duration:</strong> {req.durationMinutes} minutes</p>
                <p><strong>Requested:</strong> {new Date(req.createdAt).toLocaleString()}</p>
                {req.approvedAt && <p><strong>Approved:</strong> {new Date(req.approvedAt).toLocaleString()}</p>}
                {req.expiresAt && <p><strong>Expires:</strong> {new Date(req.expiresAt).toLocaleString()}</p>}
            </div>
        </div>
    );
}