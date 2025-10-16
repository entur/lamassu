import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
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

export function FeedProviderForm({
  provider,
  onSubmit,
  onCancel,
  isCopyMode,
}: FeedProviderFormProps) {
  const { t } = useTranslation();
  const [formData, setFormData] = useState<FeedProvider>(
    provider || {
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
    }
  );

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [newHeaderKey, setNewHeaderKey] = useState('');
  const [newHeaderValue, setNewHeaderValue] = useState('');

  const handleChange =
    (field: keyof FeedProvider) =>
    (
      event:
        | React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
        | { target: { value: unknown } }
    ) => {
      const value = event.target.value;
      setFormData(prev => ({
        ...prev,
        [field]: field === 'aggregate' || field === 'enabled' ? Boolean(value) : value,
      }));
    };

  const handleCheckboxChange =
    (field: keyof FeedProvider) => (event: React.ChangeEvent<HTMLInputElement>) => {
      setFormData(prev => ({
        ...prev,
        [field]: event.target.checked,
      }));
    };

  const handleAuthTypeChange = (value: string) => {
    let newAuth: Authentication | null = null;

    if (value) {
      newAuth = {
        scheme: value as Authentication['scheme'],
        properties: {},
      };

      if (value === 'OAUTH2_CLIENT_CREDENTIALS_GRANT') {
        newAuth.properties = {
          tokenUrl: '',
          clientId: '',
          clientPassword: '',
          scope: '',
        };
      } else if (value === 'BEARER_TOKEN') {
        newAuth.properties = {
          accessToken: '',
        };
      } else if (value === 'HTTP_HEADERS') {
        newAuth.properties = {};
      }
    }

    setFormData(prev => ({
      ...prev,
      authentication: newAuth,
    }));
  };

  const handleAuthPropChange =
    (propName: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
      setFormData(prev => ({
        ...prev,
        authentication: prev.authentication
          ? {
              ...prev.authentication,
              properties: {
                ...prev.authentication.properties,
                [propName]: event.target.value,
              },
            }
          : prev.authentication,
      }));
    };

  const addHeader = () => {
    if (newHeaderKey && newHeaderValue && formData.authentication) {
      setFormData(prev => ({
        ...prev,
        authentication: prev.authentication
          ? {
              ...prev.authentication,
              properties: {
                ...prev.authentication.properties,
                [newHeaderKey]: newHeaderValue,
              },
            }
          : prev.authentication,
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
        authentication: prev.authentication
          ? {
              ...prev.authentication,
              properties: newProps,
            }
          : prev.authentication,
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
      setError(err.message || t('admin.feedProviderForm.errorSaving'));
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
              label={t('admin.feedProviderForm.tokenUrl')}
              value={properties.tokenUrl || ''}
              onChange={handleAuthPropChange('tokenUrl')}
              required
              sx={{ mb: 2 }}
            />
            <TextField
              fullWidth
              label={t('admin.feedProviderForm.clientId')}
              value={properties.clientId || ''}
              onChange={handleAuthPropChange('clientId')}
              required
              sx={{ mb: 2 }}
            />
            <TextField
              fullWidth
              label={t('admin.feedProviderForm.clientSecret')}
              type="password"
              value={properties.clientPassword || ''}
              onChange={handleAuthPropChange('clientPassword')}
              required
              sx={{ mb: 2 }}
            />
            <TextField
              fullWidth
              label={t('admin.feedProviderForm.scope')}
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
              label={t('admin.feedProviderForm.accessToken')}
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
              <CardHeader title={t('admin.feedProviderForm.headersTitle')} />
              <CardContent>
                {Object.entries(properties).length > 0 ? (
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>{t('admin.feedProviderForm.headerName')}</TableCell>
                        <TableCell>{t('admin.feedProviderForm.headerValue')}</TableCell>
                        <TableCell>{t('admin.feedProviderForm.headerAction')}</TableCell>
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
                    {t('admin.feedProviderForm.noHeaders')}
                  </Typography>
                )}

                <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
                  <TextField
                    label={t('admin.feedProviderForm.headerName')}
                    value={newHeaderKey}
                    onChange={e => setNewHeaderKey(e.target.value)}
                    size="small"
                  />
                  <TextField
                    label={t('admin.feedProviderForm.headerValue')}
                    value={newHeaderValue}
                    onChange={e => setNewHeaderValue(e.target.value)}
                    size="small"
                  />
                  <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={addHeader}
                    disabled={!newHeaderKey || !newHeaderValue}
                  >
                    {t('admin.feedProviderForm.addHeader')}
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
        label={t('admin.feedProviderForm.systemId')}
        value={formData.systemId}
        onChange={handleChange('systemId')}
        required
        disabled={!!provider && !!provider.systemId && !isCopyMode}
        sx={{ mb: 2 }}
        helperText={
          provider && provider.systemId && !isCopyMode
            ? t('admin.feedProviderForm.systemIdHelper')
            : ''
        }
      />

      <TextField
        fullWidth
        label={t('admin.feedProviderForm.operatorId')}
        value={formData.operatorId}
        onChange={handleChange('operatorId')}
        required
        sx={{ mb: 2 }}
      />

      <TextField
        fullWidth
        label={t('admin.feedProviderForm.operatorName')}
        value={formData.operatorName}
        onChange={handleChange('operatorName')}
        required
        sx={{ mb: 2 }}
      />

      <TextField
        fullWidth
        label={t('admin.feedProviderForm.codespace')}
        value={formData.codespace}
        onChange={handleChange('codespace')}
        required
        sx={{ mb: 2 }}
      />

      <TextField
        fullWidth
        label={t('admin.feedProviderForm.url')}
        type="url"
        value={formData.url}
        onChange={handleChange('url')}
        required
        sx={{ mb: 2 }}
      />

      <TextField
        fullWidth
        label={t('admin.feedProviderForm.language')}
        value={formData.language}
        onChange={handleChange('language')}
        sx={{ mb: 2 }}
      />

      <FormControl fullWidth sx={{ mb: 2 }}>
        <InputLabel>{t('admin.feedProviderForm.version')}</InputLabel>
        <Select
          value={formData.version}
          onChange={handleChange('version')}
          label={t('admin.feedProviderForm.version')}
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
          <Checkbox checked={formData.aggregate} onChange={handleCheckboxChange('aggregate')} />
        }
        label={t('admin.feedProviderForm.aggregate')}
        sx={{ mb: 2, display: 'block' }}
      />

      <FormControlLabel
        control={<Checkbox checked={formData.enabled} onChange={handleCheckboxChange('enabled')} />}
        label={t('admin.feedProviderForm.enabled')}
        sx={{ mb: 3, display: 'block' }}
      />

      <Typography variant="h6" gutterBottom>
        {t('admin.feedProviderForm.authTitle')}
      </Typography>

      <FormControl fullWidth sx={{ mb: 2 }}>
        <InputLabel>{t('admin.feedProviderForm.authType')}</InputLabel>
        <Select
          value={formData.authentication?.scheme || ''}
          onChange={e => handleAuthTypeChange(e.target.value as string)}
          label={t('admin.feedProviderForm.authType')}
        >
          <MenuItem value="">{t('admin.feedProviderForm.authNone')}</MenuItem>
          <MenuItem value="OAUTH2_CLIENT_CREDENTIALS_GRANT">
            {t('admin.feedProviderForm.authOAuth2')}
          </MenuItem>
          <MenuItem value="BEARER_TOKEN">{t('admin.feedProviderForm.authBearer')}</MenuItem>
          <MenuItem value="HTTP_HEADERS">{t('admin.feedProviderForm.authHeaders')}</MenuItem>
        </Select>
      </FormControl>

      {renderAuthenticationForm()}

      <DialogActions sx={{ px: 0, pt: 3 }}>
        <Button onClick={onCancel}>{t('cancel')}</Button>
        <Button
          type="submit"
          variant="contained"
          disabled={loading}
          startIcon={loading ? <CircularProgress size={20} /> : null}
        >
          {isCopyMode
            ? t('admin.feedProviderForm.createCopy')
            : provider
              ? t('admin.feedProviderForm.update')
              : t('admin.feedProviderForm.create')}{' '}
          {t('admin.feedProviderForm.feedProvider')}
        </Button>
      </DialogActions>
    </Box>
  );
}
