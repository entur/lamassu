import React, { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  CardHeader,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Alert,
  CircularProgress,
  ButtonGroup,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  FileCopy as CopyIcon,
  PlayArrow as StartIcon,
  Stop as StopIcon,
  Refresh as RestartIcon,
  PowerSettingsNew as PowerIcon,
} from '@mui/icons-material';
import { FeedProviderForm } from '../components/admin/FeedProviderForm';
import { adminApi } from '../services/adminApi';
import type { FeedProvider, SubscriptionStatus } from '../types/admin';

export default function AdminFeedProviders() {
  const [providers, setProviders] = useState<FeedProvider[]>([]);
  const [subscriptionStatuses, setSubscriptionStatuses] = useState<Record<string, SubscriptionStatus>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');
  const [modalOpen, setModalOpen] = useState(false);
  const [currentProvider, setCurrentProvider] = useState<FeedProvider | null>(null);
  const [isCopyMode, setIsCopyMode] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState<Record<string, string>>({});

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [providersData, statusesData] = await Promise.all([
        adminApi.getAllProviders(),
        adminApi.getSubscriptionStatuses()
      ]);
      setProviders(providersData);
      setSubscriptionStatuses(statusesData || {});
    } catch (err) {
      setError('Failed to load data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (provider: Omit<FeedProvider, 'systemId'> & { systemId?: string }) => {
    try {
      await adminApi.createProvider(provider as FeedProvider);
      setModalOpen(false);
      setSuccess('Feed provider created successfully!');
      loadData();
    } catch (err: any) {
      throw new Error(err.response?.data?.message || 'Failed to create feed provider');
    }
  };

  const handleUpdate = async (provider: FeedProvider) => {
    try {
      await adminApi.updateProvider(provider);
      setModalOpen(false);
      setSuccess('Feed provider updated successfully!');
      loadData();
    } catch (err: any) {
      throw new Error(err.response?.data?.message || 'Failed to update feed provider');
    }
  };

  const handleDelete = async (systemId: string) => {
    setLoading(true);
    setError('');
    try {
      await adminApi.deleteProvider(systemId);
      setConfirmDelete(null);
      setSuccess('Feed provider deleted successfully!');
      loadData();
    } catch (err: any) {
      setError(`Failed to delete feed provider: ${err.response?.data?.message || err.message}`);
      setLoading(false);
    }
  };

  const handleStartSubscription = async (systemId: string) => {
    setActionLoading(prev => ({ ...prev, [systemId]: 'start' }));
    setError('');
    try {
      await adminApi.startSubscription(systemId);
      setSuccess(`Successfully started subscription for ${systemId}!`);
      loadData();
    } catch (err: any) {
      setError(`Failed to start subscription: ${err.response?.data?.message || err.message}`);
    } finally {
      setActionLoading(prev => ({ ...prev, [systemId]: '' }));
    }
  };

  const handleStopSubscription = async (systemId: string) => {
    setActionLoading(prev => ({ ...prev, [systemId]: 'stop' }));
    setError('');
    try {
      await adminApi.stopSubscription(systemId);
      setSuccess(`Successfully stopped subscription for ${systemId}!`);
      loadData();
    } catch (err: any) {
      setError(`Failed to stop subscription: ${err.response?.data?.message || err.message}`);
    } finally {
      setActionLoading(prev => ({ ...prev, [systemId]: '' }));
    }
  };

  const handleRestartSubscription = async (systemId: string) => {
    setActionLoading(prev => ({ ...prev, [systemId]: 'restart' }));
    setError('');
    try {
      await adminApi.restartSubscription(systemId);
      setSuccess(`Successfully restarted subscription for ${systemId}!`);
      loadData();
    } catch (err: any) {
      setError(`Failed to restart subscription: ${err.response?.data?.message || err.message}`);
    } finally {
      setActionLoading(prev => ({ ...prev, [systemId]: '' }));
    }
  };

  const handleToggleEnabled = async (systemId: string, currentEnabled: boolean) => {
    const newEnabled = !currentEnabled;
    setActionLoading(prev => ({ ...prev, [systemId]: 'toggle' }));
    setError('');
    try {
      await adminApi.setFeedProviderEnabled(systemId, newEnabled);
      setSuccess(`Successfully ${newEnabled ? 'enabled' : 'disabled'} feed provider ${systemId}!`);
      loadData();
    } catch (err: any) {
      setError(`Failed to ${newEnabled ? 'enable' : 'disable'} feed provider: ${err.response?.data?.message || err.message}`);
    } finally {
      setActionLoading(prev => ({ ...prev, [systemId]: '' }));
    }
  };

  const openCreateModal = () => {
    setCurrentProvider(null);
    setIsCopyMode(false);
    setModalOpen(true);
  };

  const openCopyModal = (provider: FeedProvider) => {
    const providerCopy = { ...provider };
    delete (providerCopy as any).systemId;
    setCurrentProvider(providerCopy);
    setIsCopyMode(true);
    setModalOpen(true);
  };

  const openEditModal = (provider: FeedProvider) => {
    setCurrentProvider(provider);
    setIsCopyMode(false);
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setCurrentProvider(null);
    setIsCopyMode(false);
  };

  const clearAlerts = () => {
    setError('');
    setSuccess('');
  };

  const getSubscriptionStatus = (systemId: string): SubscriptionStatus => {
    return subscriptionStatuses[systemId] || 'STOPPED';
  };

  const renderEnabledStatus = (provider: FeedProvider) => {
    const isLoading = actionLoading[provider.systemId] === 'toggle';
    const isEnabled = provider.enabled;

    return (
      <Button
        variant={isEnabled ? 'contained' : 'outlined'}
        color="success"
        size="small"
        onClick={() => handleToggleEnabled(provider.systemId, provider.enabled)}
        disabled={isLoading}
        startIcon={isLoading ? <CircularProgress size={16} /> : <PowerIcon />}
      >
        {isEnabled ? 'Enabled' : 'Enable'}
      </Button>
    );
  };

  const renderSubscriptionStatus = (provider: FeedProvider) => {
    const systemId = provider.systemId;
    const status = getSubscriptionStatus(systemId);
    const isStartLoading = actionLoading[systemId] === 'start';
    const isStopLoading = actionLoading[systemId] === 'stop';
    const isRestartLoading = actionLoading[systemId] === 'restart';

    if (status === 'STARTED') {
      return (
        <ButtonGroup size="small">
          <Button variant="contained" color="success" disabled>
            Running
          </Button>
          <Button
            variant="outlined"
            color="warning"
            onClick={() => handleStopSubscription(systemId)}
            disabled={isStopLoading}
            startIcon={isStopLoading ? <CircularProgress size={16} /> : <StopIcon />}
          >
            Stop
          </Button>
          <Button
            variant="outlined"
            color="info"
            onClick={() => handleRestartSubscription(systemId)}
            disabled={isRestartLoading}
            startIcon={isRestartLoading ? <CircularProgress size={16} /> : <RestartIcon />}
          >
            Restart
          </Button>
        </ButtonGroup>
      );
    } else if (status === 'STARTING') {
      return (
        <ButtonGroup size="small">
          <Button variant="contained" color="info" disabled>
            Starting...
          </Button>
          <Button
            variant="outlined"
            color="warning"
            onClick={() => handleStopSubscription(systemId)}
            disabled={isStopLoading}
            startIcon={isStopLoading ? <CircularProgress size={16} /> : <StopIcon />}
          >
            Stop
          </Button>
        </ButtonGroup>
      );
    } else if (status === 'STOPPING') {
      return (
        <Button variant="contained" color="warning" disabled size="small">
          Stopping...
        </Button>
      );
    } else {
      return (
        <Button
          variant="outlined"
          color="success"
          size="small"
          onClick={() => handleStartSubscription(systemId)}
          disabled={isStartLoading}
          startIcon={isStartLoading ? <CircularProgress size={16} /> : <StartIcon />}
        >
          Start
        </Button>
      );
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Card>
        <CardHeader
          title="Feed Providers"
          action={
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={openCreateModal}
              >
                Add New Feed Provider
              </Button>
              <Button
                variant="outlined"
                startIcon={<RestartIcon />}
                onClick={loadData}
              >
                Refresh
              </Button>
            </Box>
          }
        />
        <CardContent>
          {error && (
            <Alert severity="error" onClose={clearAlerts} sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          {success && (
            <Alert severity="success" onClose={clearAlerts} sx={{ mb: 2 }}>
              {success}
            </Alert>
          )}

          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
              <CircularProgress />
            </Box>
          ) : providers.length === 0 ? (
            <Typography variant="body1" sx={{ textAlign: 'center', p: 3 }}>
              No feed providers found. Click "Add New Feed Provider" to create one.
            </Typography>
          ) : (
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>System ID</TableCell>
                    <TableCell>Operator</TableCell>
                    <TableCell>Codespace</TableCell>
                    <TableCell>Version</TableCell>
                    <TableCell>Config</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {providers.map((provider) => (
                    <TableRow key={provider.systemId}>
                      <TableCell>{provider.systemId}</TableCell>
                      <TableCell>
                        <Box>
                          <Typography variant="body2">{provider.operatorName}</Typography>
                          <Typography variant="caption" color="text.secondary">
                            ID: {provider.operatorId}
                          </Typography>
                        </Box>
                      </TableCell>
                      <TableCell>{provider.codespace}</TableCell>
                      <TableCell>
                        <Chip label={provider.version} size="small" />
                      </TableCell>
                      <TableCell>{renderEnabledStatus(provider)}</TableCell>
                      <TableCell>{renderSubscriptionStatus(provider)}</TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', gap: 0.5 }}>
                          <IconButton
                            size="small"
                            color="primary"
                            onClick={() => openEditModal(provider)}
                          >
                            <EditIcon />
                          </IconButton>
                          <IconButton
                            size="small"
                            color="primary"
                            onClick={() => openCopyModal(provider)}
                          >
                            <CopyIcon />
                          </IconButton>
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => setConfirmDelete(provider.systemId)}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>

      {/* Confirm Delete Dialog */}
      <Dialog
        open={!!confirmDelete}
        onClose={() => setConfirmDelete(null)}
      >
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete the feed provider with System ID <strong>{confirmDelete}</strong>?
          </Typography>
          <Typography color="error" sx={{ mt: 1 }}>
            This action cannot be undone!
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDelete(null)}>Cancel</Button>
          <Button
            color="error"
            variant="contained"
            onClick={() => confirmDelete && handleDelete(confirmDelete)}
            disabled={loading}
          >
            {loading ? <CircularProgress size={20} /> : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Feed Provider Form Dialog */}
      <Dialog
        open={modalOpen}
        onClose={closeModal}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          {currentProvider ? (isCopyMode ? 'Copy Feed Provider' : 'Edit Feed Provider') : 'Add New Feed Provider'}
        </DialogTitle>
        <DialogContent>
          <FeedProviderForm
            provider={currentProvider}
            onSubmit={currentProvider && !isCopyMode ? handleUpdate : handleCreate}
            onCancel={closeModal}
            isCopyMode={isCopyMode}
          />
        </DialogContent>
      </Dialog>
    </Box>
  );
}