import { Box, CircularProgress, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';

export default function LoadingPage() {
  const { t } = useTranslation();
  return (
    <Box
      display="flex"
      justifyContent="center"
      alignItems="center"
      flexDirection="column"
      mt={4}
      sx={{ p: 2 }}
    >
      <CircularProgress />
      <Typography sx={{ mt: 1 }}>{t('data.loading')}</Typography>
    </Box>
  );
}
