import { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  Alert,
} from '@mui/material';
import { CheckCircle, Error as ErrorIcon, Info as InfoIcon } from '@mui/icons-material';
import { PublicFeedProviderStatus } from '../types/status';
import { statusApi } from '../services/statusApi';
import { validationApi } from '../services/validationApi';
import type { ShortValidationReport } from '../types/validation';

export default function PublicFeedProviders() {
  const [providers, setProviders] = useState<PublicFeedProviderStatus[]>([]);
  const [validationReports, setValidationReports] = useState<Record<string, ShortValidationReport>>(
    {}
  );
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [validationDialogOpen, setValidationDialogOpen] = useState(false);
  const [selectedSystemId, setSelectedSystemId] = useState<string | null>(null);

  // Load data on mount
  useEffect(() => {
    loadData();
  }, []);

  // Poll for subscription status updates every 1 second
  useEffect(() => {
    const interval = setInterval(() => {
      loadProviders();
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [providersData, reportsData] = await Promise.all([
        statusApi.getPublicFeedProviders(),
        validationApi.getValidationReports(),
      ]);
      setProviders(providersData);
      setValidationReports(reportsData);
      setError(null);
    } catch (err) {
      setError('Failed to load feed provider data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadProviders = async () => {
    try {
      const providersData = await statusApi.getPublicFeedProviders();
      setProviders(providersData);
    } catch (err) {
      console.error('Failed to refresh providers:', err);
    }
  };

  const handleShowValidation = (systemId: string) => {
    setSelectedSystemId(systemId);
    setValidationDialogOpen(true);
  };

  const getSubscriptionStatusColor = (status: string) => {
    switch (status) {
      case 'STARTED':
        return 'success';
      case 'STARTING':
        return 'info';
      case 'STOPPED':
        return 'default';
      case 'STOPPING':
        return 'warning';
      default:
        return 'default';
    }
  };

  const getValidationIcon = (systemId: string) => {
    const report = validationReports[systemId];
    if (!report) {
      return <InfoIcon color="disabled" />;
    }
    const hasErrors = report.summary.errorsCount > 0;
    return hasErrors ? <ErrorIcon color="error" /> : <CheckCircle color="success" />;
  };

  if (loading) {
    return <Typography>Loading...</Typography>;
  }

  if (error) {
    return <Typography color="error">{error}</Typography>;
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        GBFS Feed Provider Status
      </Typography>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>System ID</TableCell>
              <TableCell>Operator</TableCell>
              <TableCell>Codespace</TableCell>
              <TableCell>Version</TableCell>
              <TableCell>Enabled</TableCell>
              <TableCell>Subscription Status</TableCell>
              <TableCell>Validation</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {providers.map(provider => (
              <TableRow key={provider.systemId}>
                <TableCell>{provider.systemId}</TableCell>
                <TableCell>
                  {provider.operatorName}
                  {provider.operatorId && (
                    <Typography variant="caption" display="block" color="text.secondary">
                      {provider.operatorId}
                    </Typography>
                  )}
                </TableCell>
                <TableCell>{provider.codespace}</TableCell>
                <TableCell>{provider.version}</TableCell>
                <TableCell>
                  <Chip
                    label={provider.enabled ? 'Enabled' : 'Disabled'}
                    color={provider.enabled ? 'success' : 'default'}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Chip
                    label={provider.subscriptionStatus}
                    color={getSubscriptionStatusColor(provider.subscriptionStatus)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <IconButton
                    size="small"
                    onClick={() => handleShowValidation(provider.systemId)}
                    disabled={!validationReports[provider.systemId]}
                  >
                    {getValidationIcon(provider.systemId)}
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Validation Report Dialog */}
      <Dialog
        open={validationDialogOpen}
        onClose={() => setValidationDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Validation Report: {selectedSystemId}</DialogTitle>
        <DialogContent>
          {selectedSystemId && validationReports[selectedSystemId] && (
            <ValidationReportDisplay
              systemId={selectedSystemId}
              report={validationReports[selectedSystemId]}
            />
          )}
        </DialogContent>
      </Dialog>
    </Box>
  );
}

// Simplified validation report display for public UI
function ValidationReportDisplay({
  systemId,
  report,
}: {
  systemId: string;
  report: ShortValidationReport;
}) {
  const hasErrors = report.summary.errorsCount > 0;

  return (
    <Box>
      <Alert severity={hasErrors ? 'error' : 'success'} sx={{ mb: 2 }}>
        {hasErrors
          ? `Found ${report.summary.errorsCount} validation error${report.summary.errorsCount !== 1 ? 's' : ''}`
          : 'All feeds validated successfully'}
      </Alert>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Version: {report.summary.version} | Last checked:{' '}
        {new Date(report.summary.timestamp).toLocaleString()}
      </Typography>

      {Object.entries(report.files).map(([fileName, fileResult]) => (
        <Paper key={fileName} sx={{ p: 2, mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
            {fileResult.errorsCount > 0 ? (
              <ErrorIcon color="error" sx={{ mr: 1 }} />
            ) : (
              <CheckCircle color="success" sx={{ mr: 1 }} />
            )}
            <Typography variant="h6">{fileName}</Typography>
          </Box>

          <Typography variant="body2" color="text.secondary">
            Required: {fileResult.required ? 'Yes' : 'No'} | Exists:{' '}
            {fileResult.exists ? 'Yes' : 'No'} | Errors: {fileResult.errorsCount}
          </Typography>

          {fileResult.errorsCount > 0 && fileResult.errors.length > 0 && (
            <Box sx={{ mt: 2 }}>
              {fileResult.errors.map((error, idx) => (
                <Alert severity="error" key={idx} sx={{ mb: 1 }}>
                  <Typography variant="body2">
                    <strong>{error.message}</strong>
                  </Typography>
                  <Typography variant="caption" display="block">
                    Path: {error.violationPath}
                  </Typography>
                  <Typography variant="caption" display="block">
                    Schema: {error.schemaPath}
                  </Typography>
                </Alert>
              ))}
            </Box>
          )}
        </Paper>
      ))}
    </Box>
  );
}
