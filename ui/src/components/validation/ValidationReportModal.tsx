import {
  Box,
  Paper,
  Typography,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
} from '@mui/material';
import { CheckCircle, Error as ErrorIcon } from '@mui/icons-material';
import type { ShortValidationReport } from '../../types/validation';

interface ValidationReportModalProps {
  open: boolean;
  onClose: () => void;
  systemId: string | null;
  report: ShortValidationReport | null;
}

export default function ValidationReportModal({
  open,
  onClose,
  systemId,
  report,
}: ValidationReportModalProps) {
  if (!systemId || !report) {
    return null;
  }

  const hasErrors = report.summary.errorsCount > 0;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Validation Report: {systemId}</DialogTitle>
      <DialogContent>
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
      </DialogContent>
    </Dialog>
  );
}