import React, { useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  CardHeader,
  Checkbox,
  DialogActions,
  FormControl,
  FormControlLabel,
  FormHelperText,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
  Alert,
  CircularProgress,
} from '@mui/material';
import { Delete as DeleteIcon, Add as AddIcon } from '@mui/icons-material';
import * as AdminTypes from '../../types/admin';

type FeedProvider = AdminTypes.FeedProvider;
type Authentication = AdminTypes.Authentication;

interface FeedProviderFormProps {
  provider?: FeedProvider | null;
  onSubmit: (provider: FeedProvider) => Promise<void>;
  onCancel: () => void;
  isCopyMode?: boolean;
}

export function FeedProviderForm({ provider, onSubmit, onCancel, isCopyMode }: FeedProviderFormProps) {
  const [formData, setFormData] = useState<FeedProvider>(provider || {
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
    version: '2.3',
    enabled: true,
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [newHeaderKey, setNewHeaderKey] = useState('');
  const [newHeaderValue, setNewHeaderValue] = useState('');

  const handleChange = (field: keyof FeedProvider) => (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | { target: { value: unknown } }
  ) => {
    const value = event.target.value;
    setFormData(prev => ({
      ...prev,
      [field]: field === 'aggregate' || field === 'enabled' ? Boolean(value) : value
    }));
  };

  const handleCheckboxChange = (field: keyof FeedProvider) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.checked
    }));
  };

  const handleAuthTypeChange = (value: string) => {
    let newAuth: Authentication | null = null;

    if (value) {
      newAuth = {
        scheme: value as Authentication['scheme'],
        properties: {}
      };

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
        newAuth.properties = {};
      }
    }

    setFormData(prev => ({
      ...prev,
      authentication: newAuth
    }));
  };

  const handleAuthPropChange = (propName: string) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setFormData(prev => ({
      ...prev,
      authentication: prev.authentication ? {
        ...prev.authentication,
        properties: {
          ...prev.authentication.properties,
          [propName]: event.target.value
        }
      } : prev.authentication
    }));
  };

  const addHeader = () => {
    if (newHeaderKey && newHeaderValue && formData.authentication) {
      setFormData(prev => ({
        ...prev,
        authentication: prev.authentication ? {
          ...prev.authentication,
          properties: {
            ...prev.authentication.properties,
            [newHeaderKey]: newHeaderValue
          }
        } : prev.authentication
      }));
      setNewHeaderKey('');
      setNewHeaderValue('');
    }
  };

  const removeHeader = (key: string) => {
    if (formData.authentication) {
      const newProps = { ...formData.authentication.properties };
      delete newProps[key];
      setFormData(prev => ({
        ...prev,
        authentication: prev.authentication ? {
          ...prev.authentication,
          properties: newProps
        } : prev.authentication
      }));
    }
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError('');

    try {
      await onSubmit(formData);
    } catch (err: any) {
      setError(err.message || 'An error occurred while saving the feed provider');
    } finally {
      setLoading(false);
    }
  };

  const renderAuthenticationForm = () => {
    if (!formData.authentication?.scheme) {
      return null;
    }

    const scheme = formData.authentication.scheme;
    const properties = formData.authentication.properties || {};

    switch (scheme) {
      case 'OAUTH2_CLIENT_CREDENTIALS_GRANT':
        return (
          <Box sx={{ mt: 2 }}>
            <TextField
              fullWidth
              label="Token URL"
              value={properties.tokenUrl || ''}
              onChange={handleAuthPropChange('tokenUrl')}
              required
              sx={{ mb: 2 }}
            />
            <TextField
              fullWidth
              label="Client ID"
              value={properties.clientId || ''}
              onChange={handleAuthPropChange('clientId')}
              required
              sx={{ mb: 2 }}
            />
            <TextField
              fullWidth
              label="Client Secret"
              type="password"
              value={properties.clientPassword || ''}
              onChange={handleAuthPropChange('clientPassword')}
              required
              sx={{ mb: 2 }}
            />
            <TextField
              fullWidth
              label="Scope"
              value={properties.scope || ''}
              onChange={handleAuthPropChange('scope')}
              sx={{ mb: 2 }}
            />
          </Box>
        );

      case 'BEARER_TOKEN':
        return (
          <Box sx={{ mt: 2 }}>
            <TextField
              fullWidth
              label="Access Token"
              value={properties.accessToken || ''}
              onChange={handleAuthPropChange('accessToken')}
              required
              sx={{ mb: 2 }}
            />
          </Box>
        );

      case 'HTTP_HEADERS':
        return (
          <Box sx={{ mt: 2 }}>
            <Card>
              <CardHeader title="HTTP Headers" />
              <CardContent>
                {Object.entries(properties).length > 0 ? (
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Header Name</TableCell>
                        <TableCell>Value</TableCell>
                        <TableCell>Action</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {Object.entries(properties).map(([key, value]) => (
                        <TableRow key={key}>
                          <TableCell>{key}</TableCell>
                          <TableCell>{value}</TableCell>
                          <TableCell>
                            <IconButton
                              size="small"
                              color="error"
                              onClick={() => removeHeader(key)}
                            >
                              <DeleteIcon />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    No headers added yet. Add headers below.
                  </Typography>
                )}

                <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
                  <TextField
                    label="Header Name"
                    value={newHeaderKey}
                    onChange={(e) => setNewHeaderKey(e.target.value)}
                    size="small"
                  />
                  <TextField
                    label="Header Value"
                    value={newHeaderValue}
                    onChange={(e) => setNewHeaderValue(e.target.value)}
                    size="small"
                  />
                  <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={addHeader}
                    disabled={!newHeaderKey || !newHeaderValue}
                  >
                    Add Header
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Box>
        );

      default:
        return null;
    }
  };

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ mt: 2 }}>
      {error && (
        <Alert severity="error" onClose={() => setError('')} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <TextField
        fullWidth
        label="System ID"
        value={formData.systemId}
        onChange={handleChange('systemId')}
        required
        disabled={provider && provider.systemId && !isCopyMode}
        sx={{ mb: 2 }}
        helperText={provider && provider.systemId && !isCopyMode ? "System ID cannot be changed" : ""}
      />

      <TextField
        fullWidth
        label="Operator ID"
        value={formData.operatorId}
        onChange={handleChange('operatorId')}
        required
        sx={{ mb: 2 }}
      />

      <TextField
        fullWidth
        label="Operator Name"
        value={formData.operatorName}
        onChange={handleChange('operatorName')}
        required
        sx={{ mb: 2 }}
      />

      <TextField
        fullWidth
        label="Codespace"
        value={formData.codespace}
        onChange={handleChange('codespace')}
        required
        sx={{ mb: 2 }}
      />

      <TextField
        fullWidth
        label="URL"
        type="url"
        value={formData.url}
        onChange={handleChange('url')}
        required
        sx={{ mb: 2 }}
      />

      <TextField
        fullWidth
        label="Language"
        value={formData.language}
        onChange={handleChange('language')}
        sx={{ mb: 2 }}
      />

      <FormControl fullWidth sx={{ mb: 2 }}>
        <InputLabel>GBFS Version</InputLabel>
        <Select
          value={formData.version}
          onChange={handleChange('version')}
          label="GBFS Version"
        >
          <MenuItem value="1.0">1.0</MenuItem>
          <MenuItem value="1.1">1.1</MenuItem>
          <MenuItem value="2.0">2.0</MenuItem>
          <MenuItem value="2.1">2.1</MenuItem>
          <MenuItem value="2.2">2.2</MenuItem>
          <MenuItem value="2.3">2.3</MenuItem>
          <MenuItem value="3.0">3.0</MenuItem>
        </Select>
      </FormControl>

      <FormControlLabel
        control={
          <Checkbox
            checked={formData.aggregate}
            onChange={handleCheckboxChange('aggregate')}
          />
        }
        label="Aggregate"
        sx={{ mb: 2, display: 'block' }}
      />

      <FormControlLabel
        control={
          <Checkbox
            checked={formData.enabled}
            onChange={handleCheckboxChange('enabled')}
          />
        }
        label="Enabled"
        sx={{ mb: 3, display: 'block' }}
      />

      <Typography variant="h6" gutterBottom>
        Authentication (Optional)
      </Typography>

      <FormControl fullWidth sx={{ mb: 2 }}>
        <InputLabel>Authentication Type</InputLabel>
        <Select
          value={formData.authentication?.scheme || ''}
          onChange={(e) => handleAuthTypeChange(e.target.value as string)}
          label="Authentication Type"
        >
          <MenuItem value="">None</MenuItem>
          <MenuItem value="OAUTH2_CLIENT_CREDENTIALS_GRANT">OAuth2 Client Credentials</MenuItem>
          <MenuItem value="BEARER_TOKEN">Bearer Token</MenuItem>
          <MenuItem value="HTTP_HEADERS">HTTP Headers</MenuItem>
        </Select>
      </FormControl>

      {renderAuthenticationForm()}

      <DialogActions sx={{ px: 0, pt: 3 }}>
        <Button onClick={onCancel}>Cancel</Button>
        <Button
          type="submit"
          variant="contained"
          disabled={loading}
          startIcon={loading ? <CircularProgress size={20} /> : null}
        >
          {isCopyMode ? 'Create Copy' : provider ? 'Update' : 'Create'} Feed Provider
        </Button>
      </DialogActions>
    </Box>
  );
}