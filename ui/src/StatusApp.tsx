import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
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
          {/* Catch-all route for dev mode (.html files) and unknown paths */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Box>
    </Box>
  );
}

export default function StatusApp() {
  const { useCustomFeatures } = useCustomization();

  const { theme } = useAppTheme(useCustomFeatures);

  // Detect basename from current URL path
  // In dev mode, use '/' as basename
  // In production, supports both direct access (/status/ui) and proxied paths (/mobility/v2/status/ui)
  const basename = import.meta.env.DEV
    ? '/'
    : window.location.pathname.match(/^(.*)\/status\/ui/)?.[1] + '/status/ui' || '/status/ui';

  return (
    <BrowserRouter basename={basename}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <StatusAppContent />
      </ThemeProvider>
    </BrowserRouter>
  );
}
