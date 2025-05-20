// Main React application for Feed Provider Admin UI

// Utility functions for API calls
const api = {
  // Get all feed providers
  getAllProviders: async () => {
    try {
      const response = await axios.get('/admin/feed-providers');
      return response.data;
    } catch (error) {
      console.error('Error fetching feed providers:', error);
      throw error;
    }
  },

  // Get a specific feed provider by systemId
  getProvider: async (systemId) => {
    try {
      const response = await axios.get(`/admin/feed-providers/${systemId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching feed provider ${systemId}:`, error);
      throw error;
    }
  },

  // Create a new feed provider
  createProvider: async (provider) => {
    try {
      const response = await axios.post('/admin/feed-providers', provider);
      return response.data;
    } catch (error) {
      console.error('Error creating feed provider:', error);
      throw error;
    }
  },

  // Update an existing feed provider
  updateProvider: async (provider) => {
    try {
      const response = await axios.put(`/admin/feed-providers/${provider.systemId}`, provider);
      return response.data;
    } catch (error) {
      console.error(`Error updating feed provider ${provider.systemId}:`, error);
      throw error;
    }
  },

  // Delete a feed provider
  deleteProvider: async (systemId) => {
    try {
      await axios.delete(`/admin/feed-providers/${systemId}`);
      return true;
    } catch (error) {
      console.error(`Error deleting feed provider ${systemId}:`, error);
      throw error;
    }
  },

  // Start a feed provider subscription
  startSubscription: async (systemId) => {
    try {
      await axios.post(`/admin/feed-providers/${systemId}/start`);
      return true;
    } catch (error) {
      console.error(`Error starting subscription for ${systemId}:`, error);
      throw error;
    }
  },

  // Stop a feed provider subscription
  stopSubscription: async (systemId) => {
    try {
      await axios.post(`/admin/feed-providers/${systemId}/stop`);
      return true;
    } catch (error) {
      console.error(`Error stopping subscription for ${systemId}:`, error);
      throw error;
    }
  },

  // Restart a feed provider subscription
  restartSubscription: async (systemId) => {
    try {
      await axios.post(`/admin/feed-providers/${systemId}/restart`);
      return true;
    } catch (error) {
      console.error(`Error restarting subscription for ${systemId}:`, error);
      throw error;
    }
  },

  // Get all subscription statuses
  getSubscriptionStatuses: async () => {
    try {
      const response = await axios.get('/admin/feed-providers/subscription-statuses');
      return response.data;
    } catch (error) {
      console.error('Error fetching subscription statuses:', error);
      throw error;
    }
  },

  // Get subscription status for a specific feed provider
  getSubscriptionStatus: async (systemId) => {
    try {
      const response = await axios.get(`/admin/feed-providers/${systemId}/subscription-status`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching subscription status for ${systemId}:`, error);
      throw error;
    }
  },

  // Enable or disable a feed provider
  setFeedProviderEnabled: async (systemId, enabled) => {
    try {
      await axios.post(`/admin/feed-providers/${systemId}/set-enabled?enabled=${enabled}`);
      return true;
    } catch (error) {
      console.error(`Error ${enabled ? 'enabling' : 'disabling'} feed provider ${systemId}:`, error);
      throw error;
    }
  },

  // Get all cache keys
  getCacheKeys: async () => {
    try {
      const response = await axios.get('/admin/cache_keys');
      return response.data;
    } catch (error) {
      console.error('Error fetching cache keys:', error);
      throw error;
    }
  },

  // Clear vehicle cache
  clearVehicleCache: async () => {
    try {
      const response = await axios.post('/admin/clear_vehicle_cache');
      return response.data; // Number of entries cleared
    } catch (error) {
      console.error('Error clearing vehicle cache:', error);
      throw error;
    }
  },

  // Clear old cache
  clearOldCache: async () => {
    try {
      const response = await axios.post('/admin/clear_old_cache');
      return response.data; // List of deleted keys
    } catch (error) {
      console.error('Error clearing old cache:', error);
      throw error;
    }
  },

  // Clear entire database
  clearDatabase: async () => {
    try {
      await axios.post('/admin/clear_db');
      return true;
    } catch (error) {
      console.error('Error clearing database:', error);
      throw error;
    }
  },

  // Get vehicle orphans
  getVehicleOrphans: async () => {
    try {
      const response = await axios.get('/admin/vehicle_orphans');
      return response.data;
    } catch (error) {
      console.error('Error fetching vehicle orphans:', error);
      throw error;
    }
  },

  // Clear vehicle orphans
  clearVehicleOrphans: async () => {
    try {
      const response = await axios.delete('/admin/vehicle_orphans');
      return response.data;
    } catch (error) {
      console.error('Error clearing vehicle orphans:', error);
      throw error;
    }
  },
};

// Alert component for displaying messages
const Alert = ({ message, type, onClose }) => {
  if (!message) return null;

  return (
    <div className={`alert alert-${type}`}>
      {message}
      {onClose && (
        <button
          onClick={onClose}
          style={{ background: 'none', border: 'none', float: 'right', cursor: 'pointer' }}
        >
          &times;
        </button>
      )}
    </div>
  );
};

// Loading spinner component
const Spinner = () => (
  <div className="spinner"></div>
);

// Modal component for forms
const Modal = ({ isOpen, title, onClose, children }) => {
  if (!isOpen) return null;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3 className="modal-title">{title}</h3>
          <button className="modal-close" onClick={onClose}>&times;</button>
        </div>
        <div className="modal-body">
          {children}
        </div>
      </div>
    </div>
  );
};

// Form for creating/editing feed providers
const FeedProviderForm = ({ provider, onSubmit, onCancel }) => {
  const [formData, setFormData] = React.useState(provider || {
    systemId: '',
    operatorId: '',
    operatorName: '',
    codespace: '',
    url: '',
    language: 'en',
    authentication: null,
    excludeFeeds: null,
    aggregate: true,
    vehicleTypes: null,
    pricingPlans: null,
    version: '2.3'
  });

  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState('');

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleAuthChange = (e) => {
    const { name, value } = e.target;

    // If changing the authentication type
    if (name === 'type') {
      let newAuth = null;

      if (value) {
        newAuth = {
          scheme: value,
          properties: {}
        };

        // Initialize properties based on the authentication scheme
        if (value === 'OAUTH2_CLIENT_CREDENTIALS_GRANT') {
          newAuth.properties = {
            tokenUrl: '',
            clientId: '',
            clientPassword: '',
            scope: ''
          };
        } else if (value === 'BEARER_TOKEN') {
          newAuth.properties = {
            accessToken: ''
          };
        } else if (value === 'HTTP_HEADERS') {
          newAuth.properties = {
            // Will be populated dynamically
          };
        }
      }

      setFormData(prev => ({
        ...prev,
        authentication: newAuth
      }));
    }
    // If changing an authentication property
    else if (name.startsWith('auth-prop-')) {
      const propName = name.replace('auth-prop-', '');

      setFormData(prev => ({
        ...prev,
        authentication: {
          ...prev.authentication,
          properties: {
            ...prev.authentication?.properties,
            [propName]: value
          }
        }
      }));
    }
    // If adding a new HTTP header
    else if (name === 'new-header-key' || name === 'new-header-value') {
      // This is handled by the addHeader function
    }
  };

  const addHeader = () => {
    const headerKey = document.getElementById('new-header-key').value;
    const headerValue = document.getElementById('new-header-value').value;

    if (headerKey && headerValue) {
      setFormData(prev => ({
        ...prev,
        authentication: {
          ...prev.authentication,
          properties: {
            ...prev.authentication?.properties,
            [headerKey]: headerValue
          }
        }
      }));

      // Clear the input fields
      document.getElementById('new-header-key').value = '';
      document.getElementById('new-header-value').value = '';
    }
  };

  const removeHeader = (key) => {
    setFormData(prev => {
      const newProps = { ...prev.authentication?.properties };
      delete newProps[key];

      return {
        ...prev,
        authentication: {
          ...prev.authentication,
          properties: newProps
        }
      };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await onSubmit(formData);
      setLoading(false);
    } catch (err) {
      setError(err.response?.data?.message || 'An error occurred while saving the feed provider');
      setLoading(false);
    }
  };

  // Render authentication form based on the selected scheme
  const renderAuthenticationForm = () => {
    if (!formData.authentication || !formData.authentication.scheme) {
      return null;
    }

    const scheme = formData.authentication.scheme;
    const properties = formData.authentication.properties || {};

    switch (scheme) {
      case 'OAUTH2_CLIENT_CREDENTIALS_GRANT':
        return (
          <>
            <div className="form-group">
              <label className="form-label" htmlFor="auth-prop-tokenUrl">Token URL</label>
              <input
                type="url"
                id="auth-prop-tokenUrl"
                name="auth-prop-tokenUrl"
                className="form-control"
                value={properties.tokenUrl || ''}
                onChange={handleAuthChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="auth-prop-clientId">Client ID</label>
              <input
                type="text"
                id="auth-prop-clientId"
                name="auth-prop-clientId"
                className="form-control"
                value={properties.clientId || ''}
                onChange={handleAuthChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="auth-prop-clientPassword">Client Secret</label>
              <input
                type="password"
                id="auth-prop-clientPassword"
                name="auth-prop-clientPassword"
                className="form-control"
                value={properties.clientPassword || ''}
                onChange={handleAuthChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="auth-prop-scope">Scope</label>
              <input
                type="text"
                id="auth-prop-scope"
                name="auth-prop-scope"
                className="form-control"
                value={properties.scope || ''}
                onChange={handleAuthChange}
              />
            </div>
          </>
        );

      case 'BEARER_TOKEN':
        return (
          <div className="form-group">
            <label className="form-label" htmlFor="auth-prop-accessToken">Access Token</label>
            <input
              type="text"
              id="auth-prop-accessToken"
              name="auth-prop-accessToken"
              className="form-control"
              value={properties.accessToken || ''}
              onChange={handleAuthChange}
              required
            />
          </div>
        );

      case 'HTTP_HEADERS':
        return (
          <>
            <div className="card mb-3">
              <div className="card-header">
                <h5>HTTP Headers</h5>
              </div>
              <div className="card-body">
                {Object.entries(properties).length > 0 ? (
                  <table className="table">
                    <thead>
                      <tr>
                        <th>Header Name</th>
                        <th>Value</th>
                        <th>Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {Object.entries(properties).map(([key, value]) => (
                        <tr key={key}>
                          <td>{key}</td>
                          <td>{value}</td>
                          <td>
                            <button
                              type="button"
                              className="btn btn-danger btn-sm"
                              onClick={() => removeHeader(key)}
                            >
                              Remove
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                ) : (
                  <p>No headers added yet. Add headers below.</p>
                )}

                <div className="row mt-3">
                  <div className="col">
                    <input
                      type="text"
                      id="new-header-key"
                      name="new-header-key"
                      className="form-control"
                      placeholder="Header Name"
                    />
                  </div>
                  <div className="col">
                    <input
                      type="text"
                      id="new-header-value"
                      name="new-header-value"
                      className="form-control"
                      placeholder="Header Value"
                    />
                  </div>
                  <div className="col-auto">
                    <button
                      type="button"
                      className="btn btn-primary"
                      onClick={addHeader}
                    >
                      Add Header
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </>
        );

      default:
        return null;
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {error && <Alert message={error} type="danger" onClose={() => setError('')} />}

      <div className="form-group">
        <label className="form-label" htmlFor="systemId">System ID*</label>
        <input
          type="text"
          id="systemId"
          name="systemId"
          className="form-control"
          value={formData.systemId}
          onChange={handleChange}
          required
          disabled={provider && provider.systemId} // Disable editing systemId for existing providers
        />
      </div>

      <div className="form-group">
        <label className="form-label" htmlFor="operatorId">Operator ID*</label>
        <input
          type="text"
          id="operatorId"
          name="operatorId"
          className="form-control"
          value={formData.operatorId}
          onChange={handleChange}
          required
        />
      </div>

      <div className="form-group">
        <label className="form-label" htmlFor="operatorName">Operator Name*</label>
        <input
          type="text"
          id="operatorName"
          name="operatorName"
          className="form-control"
          value={formData.operatorName}
          onChange={handleChange}
          required
        />
      </div>

      <div className="form-group">
        <label className="form-label" htmlFor="codespace">Codespace*</label>
        <input
          type="text"
          id="codespace"
          name="codespace"
          className="form-control"
          value={formData.codespace}
          onChange={handleChange}
          required
        />
      </div>

      <div className="form-group">
        <label className="form-label" htmlFor="url">URL*</label>
        <input
          type="url"
          id="url"
          name="url"
          className="form-control"
          value={formData.url}
          onChange={handleChange}
          required
        />
      </div>

      <div className="form-group">
        <label className="form-label" htmlFor="language">Language</label>
        <input
          type="text"
          id="language"
          name="language"
          className="form-control"
          value={formData.language}
          onChange={handleChange}
        />
      </div>

      <div className="form-group">
        <label className="form-label" htmlFor="version">GBFS Version</label>
        <select
          id="version"
          name="version"
          className="form-control"
          value={formData.version}
          onChange={handleChange}
        >
          <option value="1.0">1.0</option>
          <option value="1.1">1.1</option>
          <option value="2.0">2.0</option>
          <option value="2.1">2.1</option>
          <option value="2.2">2.2</option>
          <option value="2.3">2.3</option>
          <option value="3.0">3.0</option>
        </select>
      </div>

      <div className="form-group">
        <label className="form-label">
          <input
            type="checkbox"
            name="aggregate"
            checked={formData.aggregate}
            onChange={handleChange}
          />
          {' '}Aggregate
        </label>
      </div>

      <h4 className="mt-3 mb-3">Authentication (Optional)</h4>

      <div className="form-group">
        <label className="form-label" htmlFor="auth-type">Authentication Type</label>
        <select
          id="auth-type"
          name="type"
          className="form-control"
          value={formData.authentication?.scheme || ''}
          onChange={handleAuthChange}
        >
          <option value="">None</option>
          <option value="OAUTH2_CLIENT_CREDENTIALS_GRANT">OAuth2 Client Credentials</option>
          <option value="BEARER_TOKEN">Bearer Token</option>
          <option value="HTTP_HEADERS">HTTP Headers</option>
        </select>
      </div>

      {renderAuthenticationForm()}

      <div className="modal-footer">
        <button type="button" className="btn btn-secondary" onClick={onCancel}>Cancel</button>
        <button type="submit" className="btn btn-success" disabled={loading}>
          {loading ? <Spinner /> : null}
          {provider ? 'Update' : 'Create'} Feed Provider
        </button>
      </div>
    </form>
  );
};

// Cache Management component
const CacheManagement = () => {
  const [cacheKeys, setCacheKeys] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState('');
  const [success, setSuccess] = React.useState('');

  // Load cache keys on component mount
  React.useEffect(() => {
    loadCacheKeys();
  }, []);

  const loadCacheKeys = async () => {
    setLoading(true);
    setError('');
    try {
      const keys = await api.getCacheKeys();
      setCacheKeys(keys);
      setLoading(false);
    } catch (err) {
      setError('Failed to load cache keys');
      setLoading(false);
    }
  };

  const handleClearVehicleCache = async () => {
    if (!window.confirm('Are you sure you want to clear the vehicle cache?')) {
      return;
    }

    setLoading(true);
    try {
      const count = await api.clearVehicleCache();
      setSuccess(`Successfully cleared ${count} vehicle cache entries`);
      loadCacheKeys(); // Refresh the list
    } catch (err) {
      setError('Failed to clear vehicle cache');
      setLoading(false);
    }
  };

  const handleClearOldCache = async () => {
    if (!window.confirm('Are you sure you want to clear old cache entries?')) {
      return;
    }

    setLoading(true);
    try {
      const deletedKeys = await api.clearOldCache();
      setSuccess(`Successfully cleared ${deletedKeys.length} old cache entries`);
      loadCacheKeys(); // Refresh the list
    } catch (err) {
      setError('Failed to clear old cache');
      setLoading(false);
    }
  };

  const handleClearDatabase = async () => {
    if (!window.confirm('WARNING: This will clear ALL data in Redis. Are you absolutely sure?')) {
      return;
    }
    if (!window.confirm('This action cannot be undone. Confirm again to proceed.')) {
      return;
    }

    setLoading(true);
    try {
      await api.clearDatabase();
      setSuccess('Successfully cleared the entire database');
      loadCacheKeys(); // Refresh the list
    } catch (err) {
      setError('Failed to clear database');
      setLoading(false);
    }
  };

  // Group cache keys by type
  const groupedKeys = cacheKeys.reduce((acc, key) => {
    const type = key.includes('_') ? key.split('_')[0] : 'Other';
    if (!acc[type]) {
      acc[type] = [];
    }
    acc[type].push(key);
    return acc;
  }, {});

  // Render the component
  return (
    <div className="container">
      <h2>Cache Management</h2>

      {error && <Alert message={error} type="danger" onClose={() => setError('')} />}
      {success && <Alert message={success} type="success" onClose={() => setSuccess('')} />}

      <div className="card mb-4">
        <div className="card-header">
          <h3>Cache Actions</h3>
        </div>
        <div className="card-body">
          <div className="d-flex gap-2">
            <button className="btn btn-warning" onClick={handleClearVehicleCache}>
              Clear Vehicle Cache
            </button>
            <button className="btn btn-warning" onClick={handleClearOldCache}>
              Clear Old Cache
            </button>
            <button className="btn btn-danger" onClick={handleClearDatabase}>
              Clear Entire Database
            </button>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-header d-flex justify-content-between align-items-center">
          <h3>Cache Keys</h3>
          <button className="btn btn-sm btn-primary" onClick={loadCacheKeys} disabled={loading}>
            {loading ? <Spinner /> : 'Refresh'}
          </button>
        </div>
        <div className="card-body">
          {loading && <div className="text-center"><Spinner /> Loading...</div>}

          {!loading && cacheKeys.length === 0 && (
            <p className="text-center">No cache keys found.</p>
          )}

          {!loading && cacheKeys.length > 0 && (
            <div>
              <p>Total keys: {cacheKeys.length}</p>

              <div className="accordion">
                {Object.entries(groupedKeys).map(([type, keys]) => (
                  <div className="accordion-item" key={type}>
                    <h2 className="accordion-header">
                      <button
                        className="accordion-button collapsed"
                        type="button"
                        data-bs-toggle="collapse"
                        data-bs-target={`#collapse-${type}`}
                      >
                        {type} ({keys.length} keys)
                      </button>
                    </h2>
                    <div id={`collapse-${type}`} className="accordion-collapse collapse">
                      <div className="accordion-body">
                        <ul className="list-group">
                          {keys.map(key => (
                            <li className="list-group-item" key={key}>{key}</li>
                          ))}
                        </ul>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

// Spatial Index Management component
const SpatialIndexManagement = () => {
  const [orphans, setOrphans] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState('');
  const [success, setSuccess] = React.useState('');

  // Load orphans on component mount
  React.useEffect(() => {
    loadOrphans();
  }, []);

  const loadOrphans = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await api.getVehicleOrphans();
      setOrphans(data);
      setLoading(false);
    } catch (err) {
      setError('Failed to load vehicle orphans');
      setLoading(false);
    }
  };

  const handleClearOrphans = async () => {
    if (!window.confirm('Are you sure you want to clear all orphaned vehicle entries?')) {
      return;
    }

    setLoading(true);
    try {
      const cleared = await api.clearVehicleOrphans();
      setSuccess(`Successfully cleared ${cleared.length} orphaned vehicle entries`);
      setOrphans([]);
      setLoading(false);
    } catch (err) {
      setError('Failed to clear orphaned entries');
      setLoading(false);
    }
  };

  // Render the component
  return (
    <div className="container">
      <h2>Spatial Index Management</h2>

      {error && <Alert message={error} type="danger" onClose={() => setError('')} />}
      {success && <Alert message={success} type="success" onClose={() => setSuccess('')} />}

      <div className="card mb-4">
        <div className="card-header d-flex justify-content-between align-items-center">
          <h3>Orphaned Vehicle Entries</h3>
          <div>
            <button className="btn btn-sm btn-primary me-2" onClick={loadOrphans} disabled={loading}>
              {loading ? <Spinner /> : 'Refresh'}
            </button>
            <button
              className="btn btn-sm btn-warning"
              onClick={handleClearOrphans}
              disabled={loading || orphans.length === 0}
            >
              Clear All Orphans
            </button>
          </div>
        </div>
        <div className="card-body">
          {loading && <div className="text-center"><Spinner /> Loading...</div>}

          {!loading && orphans.length === 0 && (
            <p className="text-center">No orphaned vehicle entries found.</p>
          )}

          {!loading && orphans.length > 0 && (
            <div>
              <p>Found {orphans.length} orphaned vehicle entries in the spatial index.</p>
              <div className="table-container">
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>Vehicle ID</th>
                    </tr>
                  </thead>
                  <tbody>
                    {orphans.map(id => (
                      <tr key={id}>
                        <td>{id}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

// Main application component
const App = () => {
  const [providers, setProviders] = React.useState([]);
  const [subscriptionStatuses, setSubscriptionStatuses] = React.useState({});
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState('');
  const [success, setSuccess] = React.useState('');
  const [modalOpen, setModalOpen] = React.useState(false);
  const [currentProvider, setCurrentProvider] = React.useState(null);
  const [confirmDelete, setConfirmDelete] = React.useState(null);
  const [actionLoading, setActionLoading] = React.useState({});
  const [activeTab, setActiveTab] = React.useState('providers');

  // Load feed providers and subscription statuses on component mount
  React.useEffect(() => {
    loadData();
  }, []);

  // Load feed providers and subscription statuses
  const loadData = async () => {
    setLoading(true);
    setError('');

    try {
      const [providersData, statusesData] = await Promise.all([
        api.getAllProviders(),
        api.getSubscriptionStatuses()
      ]);
      setProviders(providersData);
      setSubscriptionStatuses(statusesData || {});
      setLoading(false);
    } catch (err) {
      setError('Failed to load data. Please try again.');
      setLoading(false);
    }
  };

  // Handle creating a new feed provider
  const handleCreate = async (provider) => {
    try {
      await api.createProvider(provider);
      setModalOpen(false);
      setSuccess('Feed provider created successfully!');
      loadData();
    } catch (err) {
      throw err;
    }
  };

  // Handle updating an existing feed provider
  const handleUpdate = async (provider) => {
    try {
      await api.updateProvider(provider);
      setModalOpen(false);
      setSuccess('Feed provider updated successfully!');
      loadData();
    } catch (err) {
      throw err;
    }
  };

  // Handle deleting a feed provider
  const handleDelete = async (systemId) => {
    setLoading(true);
    setError('');

    try {
      await api.deleteProvider(systemId);
      setConfirmDelete(null);
      setSuccess('Feed provider deleted successfully!');
      loadData();
    } catch (err) {
      setError(`Failed to delete feed provider: ${err.response?.data?.message || err.message}`);
      setLoading(false);
    }
  };

// Handle starting a subscription
  const handleStartSubscription = async (systemId) => {
    setActionLoading(prev => ({ ...prev, [systemId]: 'start' }));
    setError('');

    try {
      await api.startSubscription(systemId);
      setSuccess(`Successfully started subscription for ${systemId}!`);
      pollSubscriptionStatus(systemId, 'STARTED');
      loadData();
    } catch (err) {
      setError(`Failed to start subscription: ${err.response?.data?.message || err.message}`);
    } finally {
      setActionLoading(prev => ({ ...prev, [systemId]: null }));
    }
  };

  // Handle stopping a subscription
  const handleStopSubscription = async (systemId) => {
    setActionLoading(prev => ({ ...prev, [systemId]: 'stop' }));
    setError('');

    try {
      await api.stopSubscription(systemId);
      setSuccess(`Successfully stopped subscription for ${systemId}!`);
      pollSubscriptionStatus(systemId, 'STOPPED');
      loadData();
    } catch (err) {
      setError(`Failed to stop subscription: ${err.response?.data?.message || err.message}`);
    } finally {
      setActionLoading(prev => ({ ...prev, [systemId]: null }));
    }
  };

  // Handle restarting a subscription
  const handleRestartSubscription = async (systemId) => {
    setActionLoading(prev => ({ ...prev, [systemId]: 'restart' }));
    setError('');

    try {
      await api.restartSubscription(systemId);
      setSuccess(`Successfully restarted subscription for ${systemId}!`);
      pollSubscriptionStatus(systemId, 'STARTED');
      loadData();
    } catch (err) {
      setError(`Failed to restart subscription: ${err.response?.data?.message || err.message}`);
    } finally {
      setActionLoading(prev => ({ ...prev, [systemId]: null }));
    }
  };

  // Handle enabling/disabling a feed provider
  const handleToggleEnabled = async (systemId, currentEnabled) => {
    const newEnabled = !currentEnabled;
    setActionLoading(prev => ({ ...prev, [systemId]: 'toggle' }));
    setError('');

    try {
      await api.setFeedProviderEnabled(systemId, newEnabled);
      setSuccess(`Successfully ${newEnabled ? 'enabled' : 'disabled'} feed provider ${systemId}!`);
      loadData();
    } catch (err) {
      setError(`Failed to ${newEnabled ? 'enable' : 'disable'} feed provider: ${err.response?.data?.message || err.message}`);
    } finally {
      setActionLoading(prev => ({ ...prev, [systemId]: null }));
    }
  };

  // Poll for subscription status until it reaches the target status
  const pollSubscriptionStatus = async (systemId, targetStatus, maxAttempts = 20, interval = 500) => {
    let attempts = 0;

    const poll = async () => {
      try {
        const status = await api.getSubscriptionStatus(systemId);

        // Update the status in our local state
        setSubscriptionStatuses(prev => ({
          ...prev,
          [systemId]: status
        }));

        // If we've reached the target status or max attempts, stop polling
        if (status === targetStatus || attempts >= maxAttempts) {
          return;
        }

        // Continue polling
        attempts++;
        setTimeout(poll, interval);
      } catch (error) {
        console.error(`Error polling subscription status for ${systemId}:`, error);
      }
    };

    // Start polling
    poll();
  };

  // Open modal for creating a new feed provider
  const openCreateModal = () => {
    setCurrentProvider(null);
    setModalOpen(true);
  };

  // Open modal for editing an existing feed provider
  const openEditModal = (provider) => {
    setCurrentProvider(provider);
    setModalOpen(true);
  };

  // Close the modal
  const closeModal = () => {
    setModalOpen(false);
    setCurrentProvider(null);
  };

  // Clear alert messages
  const clearAlerts = () => {
    setError('');
    setSuccess('');
  };

  // Get subscription status for a provider
  const getSubscriptionStatus = (systemId) => {
    return subscriptionStatuses[systemId] || 'STOPPED';
  };

  // Render enabled status badge with toggle button
  const renderEnabledStatus = (provider) => {
    const isLoading = actionLoading[provider.systemId] === 'toggle';
    const isEnabled = provider.enabled;

    return (
      <button
        className={`btn btn-sm ${isEnabled ? 'btn-success' : 'btn-outline-success'}`}
        onClick={() => handleToggleEnabled(provider.systemId, provider.enabled)}
        disabled={isLoading}
      >
        {isLoading ? <Spinner /> : isEnabled ? 'Enabled' : 'Enable'}
      </button>
    );
  };

  // Render subscription status badge with control buttons
  const renderSubscriptionStatus = (provider) => {
    const systemId = provider.systemId;
    const status = getSubscriptionStatus(systemId);
    const isStartLoading = actionLoading[systemId] === 'start';
    const isStopLoading = actionLoading[systemId] === 'stop';
    const isRestartLoading = actionLoading[systemId] === 'restart';

    // Show appropriate button based on current status
    if (status === 'STARTED') {
      return (
        <div className="btn-group">
          <button
            className="btn btn-sm btn-success active"
            disabled
          >
            Running
          </button>
          <button
            className="btn btn-sm btn-warning"
            onClick={() => handleStopSubscription(systemId)}
            disabled={isStopLoading}
          >
            {isStopLoading ? <Spinner /> : 'Stop'}
          </button>
          <button
            className="btn btn-sm btn-info"
            onClick={() => handleRestartSubscription(systemId)}
            disabled={isRestartLoading}
          >
            {isRestartLoading ? <Spinner /> : 'Restart'}
          </button>
        </div>
      );
    } else if (status === 'STARTING') {
      return (
        <div className="btn-group">
          <button
            className="btn btn-sm btn-info"
            disabled
          >
            Starting...
          </button>
          <button
            className="btn btn-sm btn-warning"
            onClick={() => handleStopSubscription(systemId)}
            disabled={isStopLoading}
          >
            {isStopLoading ? <Spinner /> : 'Stop'}
          </button>
        </div>
      );
    } else if (status === 'STOPPING') {
      return (
        <button
          className="btn btn-sm btn-warning"
          disabled
        >
          Stopping...
        </button>
      );
    } else {
      // STOPPED or unknown status
      return (
        <button
          className="btn btn-sm btn-outline-success"
          onClick={() => handleStartSubscription(systemId)}
          disabled={isStartLoading}
        >
          {isStartLoading ? <Spinner /> : 'Start'}
        </button>
      );
    }
  };

  // Render the feed providers table
  const renderFeedProvidersTab = () => {
    return (
      <div className="card">
        <div className="card-header">
          <h2>Feed Providers</h2>
        </div>
        <div className="card-body">
          {loading && <div className="text-center"><Spinner /> Loading...</div>}

          {!loading && providers.length === 0 && (
            <p className="text-center">No feed providers found. Click "Add New Feed Provider" to create one.</p>
          )}

          {!loading && providers.length > 0 && (
            <div className="table-container">
              <table className="table">
                <thead>
                  <tr>
                    <th>System ID</th>
                    <th>Operator</th>
                    <th>Codespace</th>
                    <th>Version</th>
                    <th>Config</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {providers.map(provider => (
                    <tr key={provider.systemId}>
                      <td>{provider.systemId}</td>
                      <td>
                        <div>{provider.operatorName}</div>
                        <small className="text-muted">ID: {provider.operatorId}</small>
                      </td>
                      <td>{provider.codespace}</td>
                      <td>{provider.version}</td>
                      <td>{renderEnabledStatus(provider)}</td>
                      <td>{renderSubscriptionStatus(provider)}</td>
                      <td>
                        <div className="d-flex gap-1">
                          <button
                            className="btn btn-sm btn-primary"
                            onClick={() => openEditModal(provider)}
                          >
                            Edit
                          </button>
                          <button
                            className="btn btn-sm btn-danger"
                            onClick={() => setConfirmDelete(provider.systemId)}
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <div className="container">
      <style>
        {`
          .config-status, .subscription-status {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            gap: 8px;
          }
          
          .subscription-controls {
            display: flex;
            gap: 5px;
          }
          
          .btn-sm {
            padding: 0.25rem 0.5rem;
            font-size: 0.875rem;
            line-height: 1.5;
          }
          
          .btn-outline-primary {
            color: #007bff;
            border-color: #007bff;
            background-color: transparent;
          }
          
          .btn-outline-primary:hover {
            color: #fff;
            background-color: #007bff;
          }
          
          .nav-tabs {
            margin-bottom: 20px;
            border-bottom: 1px solid #dee2e6;
            display: flex;
            list-style-type: none;
            padding-left: 0;
          }
          
          .nav-item {
            margin-bottom: -1px;
          }
          
          .nav-tabs .nav-link {
            margin-bottom: -1px;
            border: 1px solid transparent;
            border-top-left-radius: 0.25rem;
            border-top-right-radius: 0.25rem;
            padding: 0.5rem 1rem;
            cursor: pointer;
            color: #495057;
            transition: all 0.2s ease-in-out;
            display: block;
            text-decoration: none;
          }
          
          .nav-tabs .nav-link:hover {
            border-color: #e9ecef #e9ecef #dee2e6;
            background-color: #f8f9fa;
          }
          
          .nav-tabs .nav-link.active {
            color: #007bff;
            background-color: #fff;
            border-color: #dee2e6 #dee2e6 #fff;
            font-weight: 500;
          }
          
          .tab-content {
            padding-top: 15px;
          }
        `}
      </style>
      <div className="header">
        <h1>Lamassu Admin</h1>
        <div>
          {activeTab === 'providers' && (
            <>
              <button className="btn" onClick={openCreateModal}>Add New Feed Provider</button>
              <button className="btn" onClick={loadData}>Refresh</button>
            </>
          )}
        </div>
      </div>

      {error && <Alert message={error} type="danger" onClose={clearAlerts} />}
      {success && <Alert message={success} type="success" onClose={clearAlerts} />}

      {/* Navigation Tabs */}
      <ul className="nav nav-tabs">
        <li className="nav-item">
          <a
            className={`nav-link ${activeTab === 'providers' ? 'active' : ''}`}
            onClick={() => setActiveTab('providers')}
          >
            Feed Providers
          </a>
        </li>
        <li className="nav-item">
          <a
            className={`nav-link ${activeTab === 'cache' ? 'active' : ''}`}
            onClick={() => setActiveTab('cache')}
          >
            Cache Management
          </a>
        </li>
        <li className="nav-item">
          <a
            className={`nav-link ${activeTab === 'spatial' ? 'active' : ''}`}
            onClick={() => setActiveTab('spatial')}
          >
            Spatial Index
          </a>
        </li>
      </ul>

      {/* Tab Content */}
      {activeTab === 'providers' && renderFeedProvidersTab()}
      {activeTab === 'cache' && <CacheManagement />}
      {activeTab === 'spatial' && <SpatialIndexManagement />}

      {/* Confirm Delete Modal */}
      {confirmDelete && (
        <div className="modal-backdrop" onClick={() => setConfirmDelete(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3 className="modal-title">Confirm Delete</h3>
              <button className="modal-close" onClick={() => setConfirmDelete(null)}>&times;</button>
            </div>
            <div className="modal-body">
              <p>Are you sure you want to delete the feed provider with System ID <strong>{confirmDelete}</strong>?</p>
              <p className="text-danger">This action cannot be undone!</p>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setConfirmDelete(null)}>Cancel</button>
              <button className="btn btn-danger" onClick={() => handleDelete(confirmDelete)}>
                {loading ? <Spinner /> : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Feed Provider Form Modal */}
      <Modal
        isOpen={modalOpen}
        title={currentProvider ? 'Edit Feed Provider' : 'Add New Feed Provider'}
        onClose={closeModal}
      >
        <FeedProviderForm
          provider={currentProvider}
          onSubmit={currentProvider ? handleUpdate : handleCreate}
          onCancel={closeModal}
        />
      </Modal>
    </div>
  );
};

// Render the application
ReactDOM.createRoot(document.getElementById('root')).render(<App />);
