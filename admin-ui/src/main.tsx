import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.tsx';
import { CustomizationProvider } from './contexts/CustomizationContext.tsx';

import './i18n';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <CustomizationProvider>
      <App />
    </CustomizationProvider>
  </StrictMode>
);
