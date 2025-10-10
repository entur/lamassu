import { useState, useEffect } from 'react';
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
import {
  Refresh as RefreshIcon,
  DeleteSweep as ClearIcon,
} from '@mui/icons-material';
import { adminApi } from '../services/adminApi';

export default function AdminSpatialIndex() {
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
      setError('Failed to load vehicle orphans');
    } finally {
      setLoading(false);
    }
  };

  const handleClearOrphans = async () => {
    setActionLoading(true);
    try {
      const cleared = await adminApi.clearVehicleOrphans();
      setSuccess(`Successfully cleared ${cleared.length} orphaned vehicle entries`);
      setOrphans([]);
      setConfirmClear(false);
    } catch (err: any) {
      setError('Failed to clear orphaned entries');
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
          title="Spatial Index Management"
          action={
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button
                variant="outlined"
                startIcon={loading ? <CircularProgress size={20} /> : <RefreshIcon />}
                onClick={loadOrphans}
                disabled={loading}
              >
                Refresh
              </Button>
              <Button
                variant="outlined"
                color="warning"
                startIcon={<ClearIcon />}
                onClick={() => setConfirmClear(true)}
                disabled={loading || orphans.length === 0}
              >
                Clear All Orphans
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
            <CardHeader title="Orphaned Vehicle Entries" />
            <CardContent>
              {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', p: 3 }}>
                  <CircularProgress />
                  <Typography sx={{ ml: 2 }}>Loading...</Typography>
                </Box>
              ) : orphans.length === 0 ? (
                <Typography variant="body1" sx={{ textAlign: 'center', p: 3 }}>
                  No orphaned vehicle entries found.
                </Typography>
              ) : (
                <Box>
                  <Typography variant="body2" sx={{ mb: 2 }}>
                    Found {orphans.length} orphaned vehicle entries in the spatial index.
                  </Typography>

                  <TableContainer component={Paper} sx={{ maxHeight: 400 }}>
                    <Table stickyHeader>
                      <TableHead>
                        <TableRow>
                          <TableCell>Vehicle ID</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {orphans.map((id) => (
                          <TableRow key={id}>
                            <TableCell sx={{ fontFamily: 'monospace' }}>
                              {id}
                            </TableCell>
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
      <Dialog
        open={confirmClear}
        onClose={() => setConfirmClear(false)}
      >
        <DialogTitle>Clear Orphaned Vehicle Entries</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to clear all {orphans.length} orphaned vehicle entries?
          </Typography>
          <Typography sx={{ mt: 1, color: 'text.secondary' }}>
            This will remove orphaned entries from the spatial index that no longer have corresponding vehicle data.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmClear(false)}>
            Cancel
          </Button>
          <Button
            color="warning"
            variant="contained"
            onClick={handleClearOrphans}
            disabled={actionLoading}
            startIcon={actionLoading ? <CircularProgress size={20} /> : <ClearIcon />}
          >
            Clear Orphans
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
