import { Container, Box, Typography, Paper, useTheme, Grid } from '@mui/material';
import { useTranslation } from 'react-i18next';
import ViewQuiltIcon from '@mui/icons-material/ViewQuilt';
import MapIcon from '@mui/icons-material/Map';
import StorageIcon from '@mui/icons-material/Storage';
import LockOpenIcon from '@mui/icons-material/LockOpen';
import PaletteIcon from '@mui/icons-material/Palette';
import TranslateIcon from '@mui/icons-material/Translate';

export default function HomePage() {
  const { t } = useTranslation();
  const theme = useTheme();

  const features = [
    {
      icon: <ViewQuiltIcon fontSize="large" color="primary" />,
      headlineKey: 'home.features.layout.headline',
      descriptionKey: 'home.features.layout.description',
    },
    {
      icon: <PaletteIcon fontSize="large" color="primary" />,
      headlineKey: 'home.features.ui.headline',
      descriptionKey: 'home.features.ui.description',
    },
    {
      icon: <TranslateIcon fontSize="large" color="primary" />,
      headlineKey: 'home.features.localization.headline',
      descriptionKey: 'home.features.localization.description',
    },
    {
      icon: <MapIcon fontSize="large" color="primary" />,
      headlineKey: 'home.features.map.headline',
      descriptionKey: 'home.features.map.description',
    },
    {
      icon: <StorageIcon fontSize="large" color="primary" />,
      headlineKey: 'home.features.data.headline',
      descriptionKey: 'home.features.data.description',
    },
    {
      icon: <LockOpenIcon fontSize="large" color="primary" />,
      headlineKey: 'home.features.auth.headline',
      descriptionKey: '',
      isAuthItem: true,
    },
  ];

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Paper
        elevation={3}
        sx={{
          p: { xs: 2, sm: 2.5, md: 3 },
          textAlign: 'center',
          mb: 5,
          backgroundColor: theme.palette.background.paper,
        }}
      >
        <Typography
          variant="h3"
          component="h1"
          gutterBottom
          sx={{ fontWeight: 'bold', color: theme.palette.primary.main }}
        >
          {t('home.welcomeMessage')}
        </Typography>
        <Typography
          variant="subtitle1"
          color="text.secondary"
          sx={{ mb: 2, maxWidth: '70ch', mx: 'auto' }}
        >
          {t('home.description')}
        </Typography>
      </Paper>

      <Box sx={{ mb: 5 }}>
        <Typography
          variant="h4"
          component="h2"
          gutterBottom
          align="center"
          sx={{ fontWeight: 'medium', mb: 4 }}
        >
          {t('home.features.title')}
        </Typography>
        <Grid container spacing={4} justifyContent="center">
          {features.map(feature => {
            return (
              <Grid key={feature.headlineKey} size={{ xs: 12, sm: 6, md: 4, lg: 3 }}>
                <Paper
                  elevation={2}
                  sx={{
                    p: 3,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    height: '100%',
                    transition: 'transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out',
                    '&:hover': {
                      transform: 'translateY(-5px)',
                      boxShadow: theme.shadows[6],
                    },
                  }}
                >
                  <Box sx={{ mb: 2, color: theme.palette.primary.main }}>{feature.icon}</Box>
                  <Typography
                    variant="h6"
                    component="h3"
                    gutterBottom
                    align="center"
                    sx={{ fontWeight: 'medium' }}
                  >
                    {t(feature.headlineKey)}
                  </Typography>
                  <Typography variant="body1" color="text.secondary" align="center">
                    {feature.isAuthItem ? (
                      <>
                        {t('home.features.auth.description_prefix')}
                        <code>useAuth</code>
                        {t('home.features.auth.description_suffix')}
                      </>
                    ) : (
                      t(feature.descriptionKey)
                    )}
                  </Typography>
                </Paper>
              </Grid>
            );
          })}
        </Grid>
      </Box>
    </Container>
  );
}
