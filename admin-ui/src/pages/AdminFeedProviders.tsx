import { useState, useEffect } from 'react';
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
  Checkbox,
  Toolbar,
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
  Error as ErrorIcon,
  Check,
} from '@mui/icons-material';
import { FeedProviderForm } from '../components/admin/FeedProviderForm';
import { adminApi } from '../services/adminApi';
import type { FeedProvider, SubscriptionStatus } from '../types/admin';
import { validationApi } from '../services/validationApi.ts';
import type { ShortValidationReport } from '../types/validation.ts';
import ValidationReport, {
  type ValidationResult,
} from '../components/validation/ValidationReport.tsx';

const mapValidationReport = (source: ShortValidationReport): ValidationResult => {
  return {
    summary: {
      validatorVersion: source.summary.version,
      files: Object.entries(source.files).map(([file, result]) => {
        return {
          name: file,
          url: `${file}.json`,
          version: result.version,
          language: 'no',
          errors: result.errors.map(err => {
            return {
              keyword: 'N/A',
              instancePath: err.violationPath,
              schemaPath: err.schemaPath,
              message: err.message,
            };
          }),
          schema: {},
        };
      }),
    },
  };
};

export default function AdminFeedProviders() {
  const [providers, setProviders] = useState<FeedProvider[]>([]);
  const [subscriptionStatuses, setSubscriptionStatuses] = useState<
    Record<string, SubscriptionStatus>
  >({});
  const [validationReports, setValidationReports] = useState<Record<string, ShortValidationReport>>(
    {}
  );
  const [showValidationReportForSystem, setShowValidationReportForSystem] = useState<string | null>(
    null
  );
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');
  const [modalOpen, setModalOpen] = useState(false);
  const [currentProvider, setCurrentProvider] = useState<FeedProvider | null>(null);
  const [isCopyMode, setIsCopyMode] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState<Record<string, string>>({});
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [bulkActionLoading, setBulkActionLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setError('');
    try {
      const [providersData, statusesData, validationData] = await Promise.all([
        adminApi.getAllProviders(),
        adminApi.getSubscriptionStatuses(),
        validationApi.getValidationReports(),
      ]);
      setProviders(providersData);
      setSubscriptionStatuses(statusesData || {});
      setValidationReports(validationData);
    } catch (err) {
      setError('Failed to load data. Please try again.');
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
    setActionLoading(prev => ({ ...prev, [systemId]: 'delete' }));
    setError('');
    try {
      await adminApi.deleteProvider(systemId);
      setConfirmDelete(null);
      setSuccess('Feed provider deleted successfully!');
      loadData();
    } catch (err: any) {
      setError(`Failed to delete feed provider: ${err.response?.data?.message || err.message}`);
    } finally {
      setActionLoading(prev => ({ ...prev, [systemId]: '' }));
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
      setError(
        `Failed to ${newEnabled ? 'enable' : 'disable'} feed provider: ${err.response?.data?.message || err.message}`
      );
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

  // Selection handlers
  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      setSelectedIds(new Set(providers.map(p => p.systemId)));
    } else {
      setSelectedIds(new Set());
    }
  };

  const handleSelectOne = (systemId: string) => {
    const newSelected = new Set(selectedIds);
    if (newSelected.has(systemId)) {
      newSelected.delete(systemId);
    } else {
      newSelected.add(systemId);
    }
    setSelectedIds(newSelected);
  };

  const clearSelection = () => {
    setSelectedIds(new Set());
  };

  // Bulk operation handlers
  const handleBulkStart = async () => {
    setBulkActionLoading(true);
    setError('');
    try {
      const results = await adminApi.bulkStartSubscriptions(Array.from(selectedIds));
      const failed = Object.entries(results).filter(([_, status]) => status !== 'SUCCESS');
      if (failed.length > 0) {
        setError(
          `Started ${Object.keys(results).length - failed.length} subscriptions. Failed: ${failed.map(([id]) => id).join(', ')}`
        );
      } else {
        setSuccess(`Successfully started ${Object.keys(results).length} subscriptions!`);
      }
      clearSelection();
      loadData();
    } catch (err: any) {
      setError(`Failed to start subscriptions: ${err.response?.data?.message || err.message}`);
    } finally {
      setBulkActionLoading(false);
    }
  };

  const handleBulkStop = async () => {
    setBulkActionLoading(true);
    setError('');
    try {
      const results = await adminApi.bulkStopSubscriptions(Array.from(selectedIds));
      const failed = Object.entries(results).filter(([_, status]) => status !== 'SUCCESS');
      if (failed.length > 0) {
        setError(
          `Stopped ${Object.keys(results).length - failed.length} subscriptions. Failed: ${failed.map(([id]) => id).join(', ')}`
        );
      } else {
        setSuccess(`Successfully stopped ${Object.keys(results).length} subscriptions!`);
      }
      clearSelection();
      loadData();
    } catch (err: any) {
      setError(`Failed to stop subscriptions: ${err.response?.data?.message || err.message}`);
    } finally {
      setBulkActionLoading(false);
    }
  };

  const handleBulkRestart = async () => {
    setBulkActionLoading(true);
    setError('');
    try {
      const results = await adminApi.bulkRestartSubscriptions(Array.from(selectedIds));
      const failed = Object.entries(results).filter(([_, status]) => status !== 'SUCCESS');
      if (failed.length > 0) {
        setError(
          `Restarted ${Object.keys(results).length - failed.length} subscriptions. Failed: ${failed.map(([id]) => id).join(', ')}`
        );
      } else {
        setSuccess(`Successfully restarted ${Object.keys(results).length} subscriptions!`);
      }
      clearSelection();
      loadData();
    } catch (err: any) {
      setError(`Failed to restart subscriptions: ${err.response?.data?.message || err.message}`);
    } finally {
      setBulkActionLoading(false);
    }
  };

  const handleBulkEnable = async () => {
    setBulkActionLoading(true);
    setError('');
    try {
      const results = await adminApi.bulkSetEnabled(Array.from(selectedIds), true);
      const failed = Object.entries(results).filter(([_, status]) => status !== 'SUCCESS');
      if (failed.length > 0) {
        setError(
          `Enabled ${Object.keys(results).length - failed.length} providers. Failed: ${failed.map(([id]) => id).join(', ')}`
        );
      } else {
        setSuccess(`Successfully enabled ${Object.keys(results).length} providers!`);
      }
      clearSelection();
      loadData();
    } catch (err: any) {
      setError(`Failed to enable providers: ${err.response?.data?.message || err.message}`);
    } finally {
      setBulkActionLoading(false);
    }
  };

  const handleBulkDisable = async () => {
    setBulkActionLoading(true);
    setError('');
    try {
      const results = await adminApi.bulkSetEnabled(Array.from(selectedIds), false);
      const failed = Object.entries(results).filter(([_, status]) => status !== 'SUCCESS');
      if (failed.length > 0) {
        setError(
          `Disabled ${Object.keys(results).length - failed.length} providers. Failed: ${failed.map(([id]) => id).join(', ')}`
        );
      } else {
        setSuccess(`Successfully disabled ${Object.keys(results).length} providers!`);
      }
      clearSelection();
      loadData();
    } catch (err: any) {
      setError(`Failed to disable providers: ${err.response?.data?.message || err.message}`);
    } finally {
      setBulkActionLoading(false);
    }
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

  const selectedCount = selectedIds.size;
  const isAllSelected = providers.length > 0 && selectedCount === providers.length;
  const isIndeterminate = selectedCount > 0 && selectedCount < providers.length;

  return (
    <Box sx={{ p: 3 }}>
      <Card>
        <CardHeader
          title="Feed Providers"
          action={
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button variant="contained" startIcon={<AddIcon />} onClick={openCreateModal}>
                Add New Feed Provider
              </Button>
              <Button variant="outlined" startIcon={<RestartIcon />} onClick={loadData}>
                Refresh
              </Button>
            </Box>
          }
        />
        <CardContent>
          {/* Bulk Action Toolbar */}
          {selectedCount > 0 && (
            <Toolbar
              sx={{
                pl: { sm: 2 },
                pr: { xs: 1, sm: 1 },
                mb: 2,
                bgcolor: theme => theme.palette.action.selected,
                borderRadius: 1,
              }}
            >
              <Typography sx={{ flex: '1 1 100%' }} color="inherit" variant="subtitle1">
                {selectedCount} selected
              </Typography>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <ButtonGroup size="small" disabled={bulkActionLoading}>
                  <Button
                    onClick={handleBulkEnable}
                    startIcon={bulkActionLoading ? <CircularProgress size={16} /> : <PowerIcon />}
                    color="success"
                  >
                    Enable
                  </Button>
                  <Button
                    onClick={handleBulkDisable}
                    startIcon={bulkActionLoading ? <CircularProgress size={16} /> : <PowerIcon />}
                  >
                    Disable
                  </Button>
                </ButtonGroup>
                <ButtonGroup size="small" disabled={bulkActionLoading}>
                  <Button
                    onClick={handleBulkStart}
                    startIcon={bulkActionLoading ? <CircularProgress size={16} /> : <StartIcon />}
                    color="success"
                  >
                    Start
                  </Button>
                  <Button
                    onClick={handleBulkStop}
                    startIcon={bulkActionLoading ? <CircularProgress size={16} /> : <StopIcon />}
                    color="warning"
                  >
                    Stop
                  </Button>
                  <Button
                    onClick={handleBulkRestart}
                    startIcon={bulkActionLoading ? <CircularProgress size={16} /> : <RestartIcon />}
                    color="info"
                  >
                    Restart
                  </Button>
                </ButtonGroup>
                <Button size="small" onClick={clearSelection}>
                  Clear
                </Button>
              </Box>
            </Toolbar>
          )}
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

          {providers.length === 0 ? (
            <Typography variant="body1" sx={{ textAlign: 'center', p: 3 }}>
              No feed providers found. Click "Add New Feed Provider" to create one.
            </Typography>
          ) : (
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox
                        indeterminate={isIndeterminate}
                        checked={isAllSelected}
                        onChange={handleSelectAll}
                      />
                    </TableCell>
                    <TableCell>System ID</TableCell>
                    <TableCell>Operator</TableCell>
                    <TableCell>Codespace</TableCell>
                    <TableCell>Version</TableCell>
                    <TableCell>Config</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Actions</TableCell>
                    <TableCell>Validation</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {providers.map(provider => (
                    <TableRow key={provider.systemId}>
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selectedIds.has(provider.systemId)}
                          onChange={() => handleSelectOne(provider.systemId)}
                        />
                      </TableCell>
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
                        <Chip label={provider.version || 'N/A'} size="small" />
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
                      <TableCell>
                        <Box sx={{ display: 'flex', gap: 0.5 }}>
                          {(validationReports[provider.systemId]?.summary.errorsCount > 0 && (
                            <IconButton
                              size="small"
                              color="error"
                              onClick={() => setShowValidationReportForSystem(provider.systemId)}
                            >
                              <ErrorIcon />
                            </IconButton>
                          )) || (
                            <IconButton
                              size="small"
                              color="success"
                              onClick={() => setShowValidationReportForSystem(provider.systemId)}
                            >
                              <Check />
                            </IconButton>
                          )}
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
      <Dialog open={!!confirmDelete} onClose={() => setConfirmDelete(null)}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete the feed provider with System ID{' '}
            <strong>{confirmDelete}</strong>?
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
            disabled={confirmDelete ? !!actionLoading[confirmDelete] : false}
          >
            {confirmDelete && actionLoading[confirmDelete] ? (
              <CircularProgress size={20} />
            ) : (
              'Delete'
            )}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Feed Provider Form Dialog */}
      <Dialog open={modalOpen} onClose={closeModal} maxWidth="md" fullWidth>
        <DialogTitle>
          {currentProvider
            ? isCopyMode
              ? 'Copy Feed Provider'
              : 'Edit Feed Provider'
            : 'Add New Feed Provider'}
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

      <Dialog
        maxWidth="md"
        fullWidth
        open={!!showValidationReportForSystem}
        onClose={() => setShowValidationReportForSystem(null)}
      >
        {!!showValidationReportForSystem && (
          <ValidationReport
            validationResult={mapValidationReport(validationReports[showValidationReportForSystem])}
          />
        )}
      </Dialog>
    </Box>
  );
}
