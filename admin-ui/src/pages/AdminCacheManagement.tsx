import React, { useState, useEffect } from 'react';
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
      setError('Failed to load cache keys');
    } finally {
      setLoading(false);
    }
  };

  const handleClearVehicleCache = async () => {
    setActionLoading('vehicle');
    try {
      const count = await adminApi.clearVehicleCache();
      setSuccess(`Successfully cleared ${count} vehicle cache entries`);
      loadCacheKeys();
    } catch (err: any) {
      setError('Failed to clear vehicle cache');
    } finally {
      setActionLoading('');
      setConfirmAction(null);
    }
  };

  const handleClearOldCache = async () => {
    setActionLoading('old');
    try {
      const deletedKeys = await adminApi.clearOldCache();
      setSuccess(`Successfully cleared ${deletedKeys.length} old cache entries`);
      loadCacheKeys();
    } catch (err: any) {
      setError('Failed to clear old cache');
    } finally {
      setActionLoading('');
      setConfirmAction(null);
    }
  };

  const handleClearDatabase = async () => {
    setActionLoading('database');
    try {
      await adminApi.clearDatabase();
      setSuccess('Successfully cleared the entire database');
      loadCacheKeys();
    } catch (err: any) {
      setError('Failed to clear database');
    } finally {
      setActionLoading('');
      setConfirmAction(null);
    }
  };

  const clearAlerts = () => {
    setError('');
    setSuccess('');
  };

  const groupedKeys = cacheKeys.reduce((acc, key) => {
    const type = key.includes('_') ? key.split('_')[0] : 'Other';
    if (!acc[type]) {
      acc[type] = [];
    }
    acc[type].push(key);
    return acc;
  }, {} as Record<string, string[]>);

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
          title="Cache Management"
          action={
            <Button
              variant="outlined"
              startIcon={loading ? <CircularProgress size={20} /> : <RefreshIcon />}
              onClick={loadCacheKeys}
              disabled={loading}
            >
              Refresh
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
            <CardHeader title="Cache Actions" />
            <CardContent>
              <ButtonGroup variant="outlined" sx={{ flexWrap: 'wrap', gap: 1 }}>
                <Button
                  color="warning"
                  startIcon={actionLoading === 'vehicle' ? <CircularProgress size={20} /> : <ClearIcon />}
                  onClick={() => openConfirmDialog(
                    'vehicle',
                    'Clear Vehicle Cache',
                    'Are you sure you want to clear the vehicle cache?',
                    handleClearVehicleCache
                  )}
                  disabled={!!actionLoading}
                >
                  Clear Vehicle Cache
                </Button>
                <Button
                  color="warning"
                  startIcon={actionLoading === 'old' ? <CircularProgress size={20} /> : <ClearIcon />}
                  onClick={() => openConfirmDialog(
                    'old',
                    'Clear Old Cache',
                    'Are you sure you want to clear old cache entries?',
                    handleClearOldCache
                  )}
                  disabled={!!actionLoading}
                >
                  Clear Old Cache
                </Button>
                <Button
                  color="error"
                  startIcon={actionLoading === 'database' ? <CircularProgress size={20} /> : <WarningIcon />}
                  onClick={() => openConfirmDialog(
                    'database',
                    'Clear Entire Database',
                    'WARNING: This will clear ALL data in Redis. This action cannot be undone!',
                    handleClearDatabase
                  )}
                  disabled={!!actionLoading}
                >
                  Clear Entire Database
                </Button>
              </ButtonGroup>
            </CardContent>
          </Card>

          <Card>
            <CardHeader title="Cache Keys" />
            <CardContent>
              {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', p: 3 }}>
                  <CircularProgress />
                  <Typography sx={{ ml: 2 }}>Loading...</Typography>
                </Box>
              ) : cacheKeys.length === 0 ? (
                <Typography variant="body1" sx={{ textAlign: 'center', p: 3 }}>
                  No cache keys found.
                </Typography>
              ) : (
                <Box>
                  <Typography variant="body2" sx={{ mb: 2 }}>
                    Total keys: {cacheKeys.length}
                  </Typography>

                  {Object.entries(groupedKeys).map(([type, keys]) => (
                    <Accordion key={type}>
                      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography variant="h6">
                          {type} ({keys.length} keys)
                        </Typography>
                      </AccordionSummary>
                      <AccordionDetails>
                        <List dense>
                          {keys.map((key) => (
                            <ListItem key={key}>
                              <ListItemText
                                primary={key}
                                primaryTypographyProps={{
                                  variant: 'body2',
                                  sx: { fontFamily: 'monospace' }
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
      <Dialog
        open={!!confirmAction}
        onClose={() => setConfirmAction(null)}
      >
        <DialogTitle>{confirmAction?.title}</DialogTitle>
        <DialogContent>
          <Typography>
            {confirmAction?.message}
          </Typography>
          {confirmAction?.action === 'database' && (
            <Alert severity="error" sx={{ mt: 2 }}>
              This action cannot be undone!
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmAction(null)}>
            Cancel
          </Button>
          <Button
            color={confirmAction?.action === 'database' ? 'error' : 'warning'}
            variant="contained"
            onClick={confirmAction?.handler}
            disabled={!!actionLoading}
            startIcon={actionLoading === confirmAction?.action ? <CircularProgress size={20} /> : null}
          >
            {confirmAction?.action === 'database' ? 'Clear Database' : 'Clear Cache'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}