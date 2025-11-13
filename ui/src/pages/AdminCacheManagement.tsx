import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Box,
  Button,
  Card,
  CardContent,
  CardHeader,
  Typography,
  Alert,
  CircularProgress,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  List,
  ListItem,
  ListItemText,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  ButtonGroup,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  ExpandMore as ExpandMoreIcon,
  DeleteSweep as ClearIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import { adminApi } from '../services/adminApi';

export default function AdminCacheManagement() {
  const { t } = useTranslation();
  const [cacheKeys, setCacheKeys] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<string>('');
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');
  const [confirmAction, setConfirmAction] = useState<{
    action: string;
    title: string;
    message: string;
    handler: () => Promise<void>;
  } | null>(null);

  useEffect(() => {
    loadCacheKeys();
  }, []);

  const loadCacheKeys = async () => {
    setLoading(true);
    setError('');
    try {
      const keys = await adminApi.getCacheKeys();
      setCacheKeys(keys);
    } catch (err: any) {
      setError(t('admin.cacheManagement.errors.loadKeys'));
    } finally {
      setLoading(false);
    }
  };

  const handleClearVehicleCache = async () => {
    setActionLoading('vehicle');
    try {
      const count = await adminApi.clearVehicleCache();
      setSuccess(t('admin.cacheManagement.success.clearVehicle', { count }));
      loadCacheKeys();
    } catch (err: any) {
      setError(t('admin.cacheManagement.errors.clearVehicle'));
    } finally {
      setActionLoading('');
      setConfirmAction(null);
    }
  };

  const handleClearOldCache = async () => {
    setActionLoading('old');
    try {
      const deletedKeys = await adminApi.clearOldCache();
      setSuccess(t('admin.cacheManagement.success.clearOld', { count: deletedKeys.length }));
      loadCacheKeys();
    } catch (err: any) {
      setError(t('admin.cacheManagement.errors.clearOld'));
    } finally {
      setActionLoading('');
      setConfirmAction(null);
    }
  };

  const handleClearDatabase = async () => {
    setActionLoading('database');
    try {
      await adminApi.clearDatabase();
      setSuccess(t('admin.cacheManagement.success.clearDatabase'));
      loadCacheKeys();
    } catch (err: any) {
      setError(t('admin.cacheManagement.errors.clearDatabase'));
    } finally {
      setActionLoading('');
      setConfirmAction(null);
    }
  };

  const clearAlerts = () => {
    setError('');
    setSuccess('');
  };

  const groupedKeys = cacheKeys.reduce(
    (acc, key) => {
      const type = key.includes('_') ? key.split('_')[0] : 'Other';
      if (!acc[type]) {
        acc[type] = [];
      }
      acc[type].push(key);
      return acc;
    },
    {} as Record<string, string[]>
  );

  const openConfirmDialog = (
    action: string,
    title: string,
    message: string,
    handler: () => Promise<void>
  ) => {
    setConfirmAction({ action, title, message, handler });
  };

  return (
    <Box sx={{ p: 3 }}>
      <Card sx={{ mb: 3 }}>
        <CardHeader
          title={t('admin.cacheManagement.title')}
          action={
            <Button
              variant="outlined"
              startIcon={loading ? <CircularProgress size={20} /> : <RefreshIcon />}
              onClick={loadCacheKeys}
              disabled={loading}
            >
              {t('admin.cacheManagement.refresh')}
            </Button>
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

          <Card sx={{ mb: 3 }}>
            <CardHeader title={t('admin.cacheManagement.actions')} />
            <CardContent>
              <ButtonGroup variant="outlined" sx={{ flexWrap: 'wrap', gap: 1 }}>
                <Button
                  color="warning"
                  startIcon={
                    actionLoading === 'vehicle' ? <CircularProgress size={20} /> : <ClearIcon />
                  }
                  onClick={() =>
                    openConfirmDialog(
                      'vehicle',
                      t('admin.cacheManagement.confirmVehicle.title'),
                      t('admin.cacheManagement.confirmVehicle.message'),
                      handleClearVehicleCache
                    )
                  }
                  disabled={!!actionLoading}
                >
                  {t('admin.cacheManagement.clearVehicle')}
                </Button>
                <Button
                  color="warning"
                  startIcon={
                    actionLoading === 'old' ? <CircularProgress size={20} /> : <ClearIcon />
                  }
                  onClick={() =>
                    openConfirmDialog(
                      'old',
                      t('admin.cacheManagement.confirmOld.title'),
                      t('admin.cacheManagement.confirmOld.message'),
                      handleClearOldCache
                    )
                  }
                  disabled={!!actionLoading}
                >
                  {t('admin.cacheManagement.clearOld')}
                </Button>
                <Button
                  color="error"
                  startIcon={
                    actionLoading === 'database' ? <CircularProgress size={20} /> : <WarningIcon />
                  }
                  onClick={() =>
                    openConfirmDialog(
                      'database',
                      t('admin.cacheManagement.confirmDatabase.title'),
                      t('admin.cacheManagement.confirmDatabase.message'),
                      handleClearDatabase
                    )
                  }
                  disabled={!!actionLoading}
                >
                  {t('admin.cacheManagement.clearDatabase')}
                </Button>
              </ButtonGroup>
            </CardContent>
          </Card>

          <Card>
            <CardHeader title={t('admin.cacheManagement.keys')} />
            <CardContent>
              {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', p: 3 }}>
                  <CircularProgress />
                  <Typography sx={{ ml: 2 }}>{t('admin.cacheManagement.loading')}</Typography>
                </Box>
              ) : cacheKeys.length === 0 ? (
                <Typography variant="body1" sx={{ textAlign: 'center', p: 3 }}>
                  {t('admin.cacheManagement.noKeys')}
                </Typography>
              ) : (
                <Box>
                  <Typography variant="body2" sx={{ mb: 2 }}>
                    {t('admin.cacheManagement.totalKeys')} {cacheKeys.length}
                  </Typography>

                  {Object.entries(groupedKeys).map(([type, keys]) => (
                    <Accordion key={type}>
                      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography variant="h6">
                          {type} ({t('admin.cacheManagement.keysCount', { count: keys.length })})
                        </Typography>
                      </AccordionSummary>
                      <AccordionDetails>
                        <List dense>
                          {keys.map(key => (
                            <ListItem key={key}>
                              <ListItemText
                                primary={key}
                                primaryTypographyProps={{
                                  variant: 'body2',
                                  sx: { fontFamily: 'monospace' },
                                }}
                              />
                            </ListItem>
                          ))}
                        </List>
                      </AccordionDetails>
                    </Accordion>
                  ))}
                </Box>
              )}
            </CardContent>
          </Card>
        </CardContent>
      </Card>

      {/* Confirmation Dialog */}
      <Dialog open={!!confirmAction} onClose={() => setConfirmAction(null)}>
        <DialogTitle>{confirmAction?.title}</DialogTitle>
        <DialogContent>
          <Typography>{confirmAction?.message}</Typography>
          {confirmAction?.action === 'database' && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {t('admin.cacheManagement.confirmDatabase.warning')}
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmAction(null)}>{t('cancel')}</Button>
          <Button
            color={confirmAction?.action === 'database' ? 'error' : 'warning'}
            variant="contained"
            onClick={confirmAction?.handler}
            disabled={!!actionLoading}
            startIcon={
              actionLoading === confirmAction?.action ? <CircularProgress size={20} /> : null
            }
          >
            {confirmAction?.action === 'database'
              ? t('admin.cacheManagement.confirmDatabase.button')
              : t('admin.cacheManagement.confirmCache.button')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
