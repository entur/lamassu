import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
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
  const { t } = useTranslation();
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
  const [pollingSystemIds, setPollingSystemIds] = useState<Set<string>>(new Set());
  const [bulkActionLoading, setBulkActionLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  // Polling effect for subscription status updates
  useEffect(() => {
    if (pollingSystemIds.size === 0) return;

    const interval = setInterval(async () => {
      try {
        const statuses: Record<string, SubscriptionStatus> = {};
        await Promise.all(
          Array.from(pollingSystemIds).map(async systemId => {
            const status = await adminApi.getSubscriptionStatus(systemId);
            statuses[systemId] = status;
          })
        );
        setSubscriptionStatuses(prev => ({ ...prev, ...statuses }));

        // Remove systems that have reached a stable state (STARTED or STOPPED)
        setPollingSystemIds(prev => {
          const next = new Set(prev);
          Object.entries(statuses).forEach(([systemId, status]) => {
            if (status === 'STARTED' || status === 'STOPPED') {
              next.delete(systemId);
              setActionLoading(prevLoading => ({ ...prevLoading, [systemId]: '' }));
              setSuccess(
                t(
                  status === 'STARTED'
                    ? 'admin.feedProviders.success.started'
                    : 'admin.feedProviders.success.stopped',
                  { systemId }
                )
              );
            }
          });
          return next;
        });
      } catch (err) {
        console.error('Error polling subscription statuses:', err);
      }
    }, 1000); // Poll every second

    return () => clearInterval(interval);
  }, [pollingSystemIds]);

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
      setError(t('admin.feedProviders.errors.loadFailed'));
    }
  };

  const handleCreate = async (provider: Omit<FeedProvider, 'systemId'> & { systemId?: string }) => {
    try {
      await adminApi.createProvider(provider as FeedProvider);
      setModalOpen(false);
      setSuccess(t('admin.feedProviders.success.created'));
      loadData();
    } catch (err: any) {
      throw new Error(err.response?.data?.message || t('admin.feedProviders.errors.createFailed'));
    }
  };

  const handleUpdate = async (provider: FeedProvider) => {
    try {
      await adminApi.updateProvider(provider);
      setModalOpen(false);
      setSuccess(t('admin.feedProviders.success.updated'));
      loadData();
    } catch (err: any) {
      throw new Error(err.response?.data?.message || t('admin.feedProviders.errors.updateFailed'));
    }
  };

  const handleDelete = async (systemId: string) => {
    setActionLoading(prev => ({ ...prev, [systemId]: 'delete' }));
    setError('');
    try {
      await adminApi.deleteProvider(systemId);
      setConfirmDelete(null);
      setSuccess(t('admin.feedProviders.success.deleted'));
      loadData();
    } catch (err: any) {
      setError(
        t('admin.feedProviders.errors.deleteFailed', {
          message: err.response?.data?.message || err.message,
        })
      );
    } finally {
      setActionLoading(prev => ({ ...prev, [systemId]: '' }));
    }
  };

  const handleStartSubscription = async (systemId: string) => {
    setActionLoading(prev => ({ ...prev, [systemId]: 'start' }));
    setError('');
    try {
      await adminApi.startSubscription(systemId);
      setSuccess(t('admin.feedProviders.success.starting', { systemId }));
      // Start polling for status updates
      setPollingSystemIds(prev => new Set(prev).add(systemId));
    } catch (err: any) {
      setError(
        t('admin.feedProviders.errors.startFailed', {
          message: err.response?.data?.message || err.message,
        })
      );
      setActionLoading(prev => ({ ...prev, [systemId]: '' }));
    }
  };

  const handleStopSubscription = async (systemId: string) => {
    setActionLoading(prev => ({ ...prev, [systemId]: 'stop' }));
    setError('');
    try {
      await adminApi.stopSubscription(systemId);
      setSuccess(t('admin.feedProviders.success.stopping', { systemId }));
      // Start polling for status updates
      setPollingSystemIds(prev => new Set(prev).add(systemId));
    } catch (err: any) {
      setError(
        t('admin.feedProviders.errors.stopFailed', {
          message: err.response?.data?.message || err.message,
        })
      );
      setActionLoading(prev => ({ ...prev, [systemId]: '' }));
    }
  };

  const handleRestartSubscription = async (systemId: string) => {
    setActionLoading(prev => ({ ...prev, [systemId]: 'restart' }));
    setError('');
    try {
      await adminApi.restartSubscription(systemId);
      setSuccess(t('admin.feedProviders.success.restarting', { systemId }));
      // Start polling for status updates
      setPollingSystemIds(prev => new Set(prev).add(systemId));
    } catch (err: any) {
      setError(
        t('admin.feedProviders.errors.restartFailed', {
          message: err.response?.data?.message || err.message,
        })
      );
      setActionLoading(prev => ({ ...prev, [systemId]: '' }));
    }
  };

  const handleToggleEnabled = async (systemId: string, currentEnabled: boolean) => {
    const newEnabled = !currentEnabled;
    setActionLoading(prev => ({ ...prev, [systemId]: 'toggle' }));
    setError('');
    try {
      await adminApi.setFeedProviderEnabled(systemId, newEnabled);
      setSuccess(
        t('admin.feedProviders.success.enabled', {
          action: newEnabled ? 'enabled' : 'disabled',
          systemId,
        })
      );
      loadData();
    } catch (err: any) {
      setError(
        t('admin.feedProviders.errors.enableFailed', {
          action: newEnabled ? 'enable' : 'disable',
          message: err.response?.data?.message || err.message,
        })
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
          t('admin.feedProviders.bulk.startPartial', {
            success: Object.keys(results).length - failed.length,
            failed: failed.map(([id]) => id).join(', '),
          })
        );
      } else {
        setSuccess(t('admin.feedProviders.bulk.startSuccess', { count: Object.keys(results).length }));
      }
      clearSelection();
      loadData();
    } catch (err: any) {
      setError(
        t('admin.feedProviders.bulk.startFailed', {
          message: err.response?.data?.message || err.message,
        })
      );
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
          t('admin.feedProviders.bulk.stopPartial', {
            success: Object.keys(results).length - failed.length,
            failed: failed.map(([id]) => id).join(', '),
          })
        );
      } else {
        setSuccess(t('admin.feedProviders.bulk.stopSuccess', { count: Object.keys(results).length }));
      }
      clearSelection();
      loadData();
    } catch (err: any) {
      setError(
        t('admin.feedProviders.bulk.stopFailed', {
          message: err.response?.data?.message || err.message,
        })
      );
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
          t('admin.feedProviders.bulk.restartPartial', {
            success: Object.keys(results).length - failed.length,
            failed: failed.map(([id]) => id).join(', '),
          })
        );
      } else {
        setSuccess(
          t('admin.feedProviders.bulk.restartSuccess', { count: Object.keys(results).length })
        );
      }
      clearSelection();
      loadData();
    } catch (err: any) {
      setError(
        t('admin.feedProviders.bulk.restartFailed', {
          message: err.response?.data?.message || err.message,
        })
      );
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
          t('admin.feedProviders.bulk.enablePartial', {
            success: Object.keys(results).length - failed.length,
            failed: failed.map(([id]) => id).join(', '),
          })
        );
      } else {
        setSuccess(
          t('admin.feedProviders.bulk.enableSuccess', { count: Object.keys(results).length })
        );
      }
      clearSelection();
      loadData();
    } catch (err: any) {
      setError(
        t('admin.feedProviders.bulk.enableFailed', {
          message: err.response?.data?.message || err.message,
        })
      );
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
          t('admin.feedProviders.bulk.disablePartial', {
            success: Object.keys(results).length - failed.length,
            failed: failed.map(([id]) => id).join(', '),
          })
        );
      } else {
        setSuccess(
          t('admin.feedProviders.bulk.disableSuccess', { count: Object.keys(results).length })
        );
      }
      clearSelection();
      loadData();
    } catch (err: any) {
      setError(
        t('admin.feedProviders.bulk.disableFailed', {
          message: err.response?.data?.message || err.message,
        })
      );
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
        {isEnabled ? t('admin.feedProviders.status.enabled') : t('admin.feedProviders.status.enable')}
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
            {t('admin.feedProviders.status.running')}
          </Button>
          <Button
            variant="outlined"
            color="warning"
            onClick={() => handleStopSubscription(systemId)}
            disabled={isStopLoading}
            startIcon={isStopLoading ? <CircularProgress size={16} /> : <StopIcon />}
          >
            {t('admin.feedProviders.status.stop')}
          </Button>
          <Button
            variant="outlined"
            color="info"
            onClick={() => handleRestartSubscription(systemId)}
            disabled={isRestartLoading}
            startIcon={isRestartLoading ? <CircularProgress size={16} /> : <RestartIcon />}
          >
            {t('admin.feedProviders.status.restart')}
          </Button>
        </ButtonGroup>
      );
    } else if (status === 'STARTING') {
      return (
        <ButtonGroup size="small">
          <Button variant="contained" color="info" disabled>
            {t('admin.feedProviders.status.starting')}
          </Button>
          <Button
            variant="outlined"
            color="warning"
            onClick={() => handleStopSubscription(systemId)}
            disabled={isStopLoading}
            startIcon={isStopLoading ? <CircularProgress size={16} /> : <StopIcon />}
          >
            {t('admin.feedProviders.status.stop')}
          </Button>
        </ButtonGroup>
      );
    } else if (status === 'STOPPING') {
      return (
        <Button variant="contained" color="warning" disabled size="small">
          {t('admin.feedProviders.status.stopping')}
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
          {t('admin.feedProviders.status.start')}
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
          title={t('admin.feedProviders')}
          action={
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button variant="contained" startIcon={<AddIcon />} onClick={openCreateModal}>
                {t('admin.feedProviders.addNew')}
              </Button>
              <Button variant="outlined" startIcon={<RestartIcon />} onClick={loadData}>
                {t('admin.feedProviders.refresh')}
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
                {t('admin.feedProviders.selected', { count: selectedCount })}
              </Typography>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <ButtonGroup size="small" disabled={bulkActionLoading}>
                  <Button
                    onClick={handleBulkEnable}
                    startIcon={bulkActionLoading ? <CircularProgress size={16} /> : <PowerIcon />}
                    color="success"
                  >
                    {t('admin.feedProviders.bulkActions.enable')}
                  </Button>
                  <Button
                    onClick={handleBulkDisable}
                    startIcon={bulkActionLoading ? <CircularProgress size={16} /> : <PowerIcon />}
                  >
                    {t('admin.feedProviders.bulkActions.disable')}
                  </Button>
                </ButtonGroup>
                <ButtonGroup size="small" disabled={bulkActionLoading}>
                  <Button
                    onClick={handleBulkStart}
                    startIcon={bulkActionLoading ? <CircularProgress size={16} /> : <StartIcon />}
                    color="success"
                  >
                    {t('admin.feedProviders.bulkActions.start')}
                  </Button>
                  <Button
                    onClick={handleBulkStop}
                    startIcon={bulkActionLoading ? <CircularProgress size={16} /> : <StopIcon />}
                    color="warning"
                  >
                    {t('admin.feedProviders.bulkActions.stop')}
                  </Button>
                  <Button
                    onClick={handleBulkRestart}
                    startIcon={bulkActionLoading ? <CircularProgress size={16} /> : <RestartIcon />}
                    color="info"
                  >
                    {t('admin.feedProviders.bulkActions.restart')}
                  </Button>
                </ButtonGroup>
                <Button size="small" onClick={clearSelection}>
                  {t('admin.feedProviders.bulkActions.clear')}
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
              {t('admin.feedProviders.noData')}
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
                    <TableCell>{t('admin.feedProviders.table.systemId')}</TableCell>
                    <TableCell>{t('admin.feedProviders.table.operator')}</TableCell>
                    <TableCell>{t('admin.feedProviders.table.codespace')}</TableCell>
                    <TableCell>{t('admin.feedProviders.table.version')}</TableCell>
                    <TableCell>{t('admin.feedProviders.table.config')}</TableCell>
                    <TableCell>{t('admin.feedProviders.table.status')}</TableCell>
                    <TableCell>{t('admin.feedProviders.table.actions')}</TableCell>
                    <TableCell>{t('admin.feedProviders.table.validation')}</TableCell>
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
                            {t('admin.feedProviders.table.operatorId')} {provider.operatorId}
                          </Typography>
                        </Box>
                      </TableCell>
                      <TableCell>{provider.codespace}</TableCell>
                      <TableCell>
                        <Chip
                          label={provider.version || t('admin.feedProviders.table.notAvailable')}
                          size="small"
                        />
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
        <DialogTitle>{t('admin.feedProviders.delete.title')}</DialogTitle>
        <DialogContent>
          <Typography>
            {t('admin.feedProviders.delete.message')} <strong>{confirmDelete}</strong>?
          </Typography>
          <Typography color="error" sx={{ mt: 1 }}>
            {t('admin.feedProviders.delete.warning')}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDelete(null)}>{t('cancel')}</Button>
          <Button
            color="error"
            variant="contained"
            onClick={() => confirmDelete && handleDelete(confirmDelete)}
            disabled={confirmDelete ? !!actionLoading[confirmDelete] : false}
          >
            {confirmDelete && actionLoading[confirmDelete] ? (
              <CircularProgress size={20} />
            ) : (
              t('admin.feedProviders.delete.button')
            )}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Feed Provider Form Dialog */}
      <Dialog open={modalOpen} onClose={closeModal} maxWidth="md" fullWidth>
        <DialogTitle>
          {currentProvider
            ? isCopyMode
              ? t('admin.feedProviders.modal.copy')
              : t('admin.feedProviders.modal.edit')
            : t('admin.feedProviders.modal.create')}
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
