import { Alert, Box } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useStopPlaces } from '../../data/useStopPlaces.ts';

export default function ErrorPage() {
  const { t } = useTranslation();
  const { error } = useStopPlaces();
  return (
    <Box sx={{ p: 2 }}>
      <Alert severity="error">
        {t('data.errorPrefix')}: {error}
      </Alert>
    </Box>
  );
}
