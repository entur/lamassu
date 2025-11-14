import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import AdminApp from './AdminApp.tsx';
import { CustomizationProvider } from './contexts/CustomizationContext.tsx';

import './i18n';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <CustomizationProvider>
      <AdminApp />
    </CustomizationProvider>
  </StrictMode>
);
