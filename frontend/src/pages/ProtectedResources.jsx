import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { resourceAPI } from '../services/api';
import './Resources.css';

export default function ProtectedResources() {
    const navigate = useNavigate();
    const [results, setResults] = useState({});
    const [loading, setLoading] = useState({});

    const testResource = async (resourceName, apiCall) => {
        setLoading({ ...loading, [resourceName]: true });
        try {
            const response = await apiCall();
            setResults({
                ...results,
                [resourceName]: {
                    success: true,
                    message: response.data.message,
                    data: response.data.data,
                },
            });
        } catch (error) {
            setResults({
                ...results,
                [resourceName]: {
                    success: false,
                    message: error.response?.data?.message || 'Access denied',
                    status: error.response?.status,
                },
            });
        } finally {
            setLoading({ ...loading, [resourceName]: false });
        }
    };

    const testDocument = async () => {
        const documentId = 'doc-123';
        await testResource('document', () => resourceAPI.getDocument(documentId));
    };

    return (
        <div className="resources-container">
            <div className="resources-header">
                <button onClick={() => navigate('/dashboard')} className="back-btn">
                    ← Back to Dashboard
                </button>
                <h1>Protected Resources Test</h1>
            </div>

            <div className="resources-content">
                <div className="test-section">
                    <h2>Organizational Role Tests</h2>
                    <p className="section-description">
                        Test access based on organizational role hierarchy (ADMIN &gt; MANAGER &gt; USER)
                    </p>

                    <div className="test-card">
                        <h3>Admin Resource</h3>
                        <p>Requires: ADMIN role</p>
                        <button
                            onClick={() => testResource('admin', resourceAPI.getAdminResource)}
                            disabled={loading.admin}
                        >
                            {loading.admin ? 'Testing...' : 'Test Admin Access'}
                        </button>
                        {results.admin && (
                            <div className={`result ${results.admin.success ? 'success' : 'error'}`}>
                                <p><strong>Status:</strong> {results.admin.success ? 'Success' : `Failed (${results.admin.status})`}</p>
                                <p><strong>Message:</strong> {results.admin.message}</p>
                                {results.admin.data && (
                                    <pre>{JSON.stringify(results.admin.data, null, 2)}</pre>
                                )}
                            </div>
                        )}
                    </div>

                    <div className="test-card">
                        <h3>Manager Resource</h3>
                        <p>Requires: MANAGER role or higher</p>
                        <button
                            onClick={() => testResource('manager', resourceAPI.getManagerResource)}
                            disabled={loading.manager}
                        >
                            {loading.manager ? 'Testing...' : 'Test Manager Access'}
                        </button>
                        {results.manager && (
                            <div className={`result ${results.manager.success ? 'success' : 'error'}`}>
                                <p><strong>Status:</strong> {results.manager.success ? 'Success' : `Failed (${results.manager.status})`}</p>
                                <p><strong>Message:</strong> {results.manager.message}</p>
                                {results.manager.data && (
                                    <pre>{JSON.stringify(results.manager.data, null, 2)}</pre>
                                )}
                            </div>
                        )}
                    </div>

                    <div className="test-card">
                        <h3>User Resource</h3>
                        <p>Requires: USER role or higher</p>
                        <button
                            onClick={() => testResource('user', resourceAPI.getUserResource)}
                            disabled={loading.user}
                        >
                            {loading.user ? 'Testing...' : 'Test User Access'}
                        </button>
                        {results.user && (
                            <div className={`result ${results.user.success ? 'success' : 'error'}`}>
                                <p><strong>Status:</strong> {results.user.success ? 'Success' : `Failed (${results.user.status})`}</p>
                                <p><strong>Message:</strong> {results.user.message}</p>
                                {results.user.data && (
                                    <pre>{JSON.stringify(results.user.data, null, 2)}</pre>
                                )}
                            </div>
                        )}
                    </div>
                </div>

                <div className="test-section">
                    <h2>Resource-Specific Access Test</h2>
                    <p className="section-description">
                        Test access to specific documents (requires READ_DOCUMENTS permission or JIT access)
                    </p>

                    <div className="test-card">
                        <h3>Document Access</h3>
                        <p>Requires: DOCUMENT READ permission or temporary JIT access</p>
                        <button
                            onClick={testDocument}
                            disabled={loading.document}
                        >
                            {loading.document ? 'Testing...' : 'Test Document Access'}
                        </button>
                        {results.document && (
                            <div className={`result ${results.document.success ? 'success' : 'error'}`}>
                                <p><strong>Status:</strong> {results.document.success ? 'Success' : `Failed (${results.document.status})`}</p>
                                <p><strong>Message:</strong> {results.document.message}</p>
                                {results.document.data && (
                                    <pre>{JSON.stringify(results.document.data, null, 2)}</pre>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
