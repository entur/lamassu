import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Header from './components/header/Header.tsx';
import Home from './pages/Home';
import AdminFeedProviders from './pages/AdminFeedProviders';
import AdminCacheManagement from './pages/AdminCacheManagement';
import AdminSpatialIndex from './pages/AdminSpatialIndex';
import { CssBaseline, Toolbar, Box, ThemeProvider } from '@mui/material';
import { useCustomization } from './contexts/CustomizationContext.tsx';
import { useAppTheme } from './hooks/useAppTheme';

export default function App() {
  const { useCustomFeatures } = useCustomization();

  const { theme } = useAppTheme(useCustomFeatures);

  return (
    <BrowserRouter future={{ v7_relativeSplatPath: true }} basename={import.meta.env.BASE_URL}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
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
      </ThemeProvider>
    </BrowserRouter>
  );
}
