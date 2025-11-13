import { BrowserRouter, Routes, Route } from 'react-router-dom';
import PublicFeedProviders from './pages/PublicFeedProviders';
import { CssBaseline, Box, ThemeProvider } from '@mui/material';
import { useCustomization } from './contexts/CustomizationContext.tsx';
import { useAppTheme } from './hooks/useAppTheme';

function StatusAppContent() {
  return (
    <Box className="app-root">
      <Box className="app-content">
        <Routes>
          <Route path="/" element={<PublicFeedProviders />} />
        </Routes>
      </Box>
    </Box>
  );
}

export default function StatusApp() {
  const { useCustomFeatures } = useCustomization();

  const { theme } = useAppTheme(useCustomFeatures);

  return (
    <BrowserRouter basename="/status/ui">
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <StatusAppContent />
      </ThemeProvider>
    </BrowserRouter>
  );
}
