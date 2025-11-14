import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Header from './components/header/Header.tsx';
import Home from './pages/Home';
import AdminFeedProviders from './pages/AdminFeedProviders';
import AdminCacheManagement from './pages/AdminCacheManagement';
import AdminSpatialIndex from './pages/AdminSpatialIndex';
import { CssBaseline, Toolbar, Box, ThemeProvider } from '@mui/material';
import { useCustomization } from './contexts/CustomizationContext.tsx';
import { useAppTheme } from './hooks/useAppTheme';

function AdminAppContent() {
  return (
    <>
      <Header />
      <Toolbar />
      <Box className="app-root">
        <Box className="app-content">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/feed-providers" element={<AdminFeedProviders />} />
            <Route path="/cache" element={<AdminCacheManagement />} />
            <Route path="/spatial-index" element={<AdminSpatialIndex />} />
          </Routes>
        </Box>
      </Box>
    </>
  );
}

export default function AdminApp() {
  const { useCustomFeatures } = useCustomization();

  const { theme } = useAppTheme(useCustomFeatures);

  // Detect basename from current URL path
  // This supports both direct access (/admin/ui) and proxied paths (/mobility/v2/admin/ui)
  const basename =
    window.location.pathname.match(/^(.*)\/admin\/ui/)?.[1] + '/admin/ui' || '/admin/ui';

  return (
    <BrowserRouter basename={basename}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <AdminAppContent />
      </ThemeProvider>
    </BrowserRouter>
  );
}
