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
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
} from '@mui/material';
import { Refresh as RefreshIcon, DeleteSweep as ClearIcon } from '@mui/icons-material';
import { adminApi } from '../services/adminApi';

export default function AdminSpatialIndex() {
  const { t } = useTranslation();
  const [orphans, setOrphans] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');
  const [confirmClear, setConfirmClear] = useState(false);

  useEffect(() => {
    loadOrphans();
  }, []);

  const loadOrphans = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await adminApi.getVehicleOrphans();
      setOrphans(data);
    } catch (err: any) {
      setError(t('admin.spatialIndex.errors.load'));
    } finally {
      setLoading(false);
    }
  };

  const handleClearOrphans = async () => {
    setActionLoading(true);
    try {
      const cleared = await adminApi.clearVehicleOrphans();
      setSuccess(t('admin.spatialIndex.success.clear', { count: cleared.length }));
      setOrphans([]);
      setConfirmClear(false);
    } catch (err: any) {
      setError(t('admin.spatialIndex.errors.clear'));
    } finally {
      setActionLoading(false);
    }
  };

  const clearAlerts = () => {
    setError('');
    setSuccess('');
  };

  return (
    <Box sx={{ p: 3 }}>
      <Card>
        <CardHeader
          title={t('admin.spatialIndex.title')}
          action={
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button
                variant="outlined"
                startIcon={loading ? <CircularProgress size={20} /> : <RefreshIcon />}
                onClick={loadOrphans}
                disabled={loading}
              >
                {t('admin.spatialIndex.refresh')}
              </Button>
              <Button
                variant="outlined"
                color="warning"
                startIcon={<ClearIcon />}
                onClick={() => setConfirmClear(true)}
                disabled={loading || orphans.length === 0}
              >
                {t('admin.spatialIndex.clearOrphans')}
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

          <Card>
            <CardHeader title={t('admin.spatialIndex.orphanedEntries')} />
            <CardContent>
              {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', p: 3 }}>
                  <CircularProgress />
                  <Typography sx={{ ml: 2 }}>{t('admin.spatialIndex.loading')}</Typography>
                </Box>
              ) : orphans.length === 0 ? (
                <Typography variant="body1" sx={{ textAlign: 'center', p: 3 }}>
                  {t('admin.spatialIndex.noOrphans')}
                </Typography>
              ) : (
                <Box>
                  <Typography variant="body2" sx={{ mb: 2 }}>
                    {t('admin.spatialIndex.foundOrphans', { count: orphans.length })}
                  </Typography>

                  <TableContainer component={Paper} sx={{ maxHeight: 400 }}>
                    <Table stickyHeader>
                      <TableHead>
                        <TableRow>
                          <TableCell>{t('admin.spatialIndex.vehicleId')}</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {orphans.map(id => (
                          <TableRow key={id}>
                            <TableCell sx={{ fontFamily: 'monospace' }}>{id}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </Box>
              )}
            </CardContent>
          </Card>
        </CardContent>
      </Card>

      {/* Confirmation Dialog */}
      <Dialog open={confirmClear} onClose={() => setConfirmClear(false)}>
        <DialogTitle>{t('admin.spatialIndex.confirm.title')}</DialogTitle>
        <DialogContent>
          <Typography>
            {t('admin.spatialIndex.confirm.message', { count: orphans.length })}
          </Typography>
          <Typography sx={{ mt: 1, color: 'text.secondary' }}>
            {t('admin.spatialIndex.confirm.description')}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmClear(false)}>{t('cancel')}</Button>
          <Button
            color="warning"
            variant="contained"
            onClick={handleClearOrphans}
            disabled={actionLoading}
            startIcon={actionLoading ? <CircularProgress size={20} /> : <ClearIcon />}
          >
            {t('admin.spatialIndex.confirm.button')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
