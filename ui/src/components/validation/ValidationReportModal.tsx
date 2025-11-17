import {
  Box,
  Paper,
  Typography,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  Chip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@mui/material';
import { CheckCircle, Error as ErrorIcon, ExpandMore as ExpandMoreIcon } from '@mui/icons-material';
import type { ShortValidationReport } from '../../types/validation';
import { groupValidationErrors } from '../../utils/validationErrorGrouping';

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

              {fileResult.errorsCount > 0 &&
                fileResult.errors.length > 0 &&
                (() => {
                  const groupedErrors = groupValidationErrors(fileResult.errors);
                  const hasMultipleOccurrences = groupedErrors.some(g => g.count > 1);

                  return (
                    <Box sx={{ mt: 2 }}>
                      {hasMultipleOccurrences && (
                        <Alert severity="info" sx={{ mb: 2 }}>
                          Showing {groupedErrors.length} unique error
                          {groupedErrors.length !== 1 ? 's' : ''} ({fileResult.errorsCount} total
                          occurrence{fileResult.errorsCount !== 1 ? 's' : ''})
                        </Alert>
                      )}
                      {groupedErrors.map((groupedError, idx) => (
                        <Accordion key={idx} defaultExpanded={groupedErrors.length <= 3}>
                          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                            <Box
                              sx={{ display: 'flex', alignItems: 'center', gap: 1, width: '100%' }}
                            >
                              <ErrorIcon color="error" fontSize="small" />
                              <Typography variant="body2" sx={{ flex: 1 }}>
                                <strong>{groupedError.message}</strong>
                              </Typography>
                              {groupedError.count > 1 && (
                                <Chip
                                  label={`${groupedError.count} occurrences`}
                                  color="error"
                                  size="small"
                                />
                              )}
                            </Box>
                          </AccordionSummary>
                          <AccordionDetails>
                            <Box>
                              <Typography variant="caption" display="block" sx={{ mb: 1 }}>
                                <strong>Pattern:</strong> {groupedError.normalizedPath}
                              </Typography>
                              <Typography variant="caption" display="block" sx={{ mb: 1 }}>
                                <strong>Schema:</strong> {groupedError.schemaPath}
                              </Typography>
                              {groupedError.count > 1 && (
                                <Box sx={{ mt: 2 }}>
                                  <Typography variant="caption" display="block" sx={{ mb: 0.5 }}>
                                    <strong>Example paths:</strong>
                                  </Typography>
                                  {groupedError.examplePaths.map((path, pathIdx) => (
                                    <Typography
                                      key={pathIdx}
                                      variant="caption"
                                      display="block"
                                      sx={{ ml: 2, fontFamily: 'monospace' }}
                                    >
                                      • {path}
                                    </Typography>
                                  ))}
                                  {groupedError.count > groupedError.examplePaths.length && (
                                    <Typography
                                      variant="caption"
                                      display="block"
                                      sx={{ ml: 2, fontStyle: 'italic', mt: 0.5 }}
                                    >
                                      ... and{' '}
                                      {groupedError.count - groupedError.examplePaths.length} more
                                    </Typography>
                                  )}
                                </Box>
                              )}
                              {groupedError.count === 1 && (
                                <Typography variant="caption" display="block" sx={{ mt: 1 }}>
                                  <strong>Path:</strong> {groupedError.examplePaths[0]}
                                </Typography>
                              )}
                            </Box>
                          </AccordionDetails>
                        </Accordion>
                      ))}
                    </Box>
                  );
                })()}
            </Paper>
          ))}
        </Box>
      </DialogContent>
    </Dialog>
  );
}
