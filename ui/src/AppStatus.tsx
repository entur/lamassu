import { BrowserRouter, Routes, Route } from 'react-router-dom';
import PublicFeedProviders from './pages/PublicFeedProviders';
import { CssBaseline, Box, ThemeProvider } from '@mui/material';
import { useCustomization } from './contexts/CustomizationContext.tsx';
import { useAppTheme } from './hooks/useAppTheme';

export default function AppStatus() {
  const { useCustomFeatures } = useCustomization();
  const { theme } = useAppTheme(useCustomFeatures);

  return (
    <BrowserRouter basename="/status/ui">
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Box className="app-root">
          <Box className="app-content">
            <Routes>
              <Route path="/" element={<PublicFeedProviders />} />
            </Routes>
          </Box>
        </Box>
      </ThemeProvider>
    </BrowserRouter>
  );
}
