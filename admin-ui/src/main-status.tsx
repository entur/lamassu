import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import AppStatus from './AppStatus.tsx';
import './index.css';
import './i18n.ts';
import { CustomizationProvider } from './contexts/CustomizationContext.tsx';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <CustomizationProvider>
      <AppStatus />
    </CustomizationProvider>
  </StrictMode>
);
