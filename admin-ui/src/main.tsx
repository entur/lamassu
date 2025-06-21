import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.tsx';
import { fetchConfig } from './config/fetchConfig.ts';
import { ConfigContext } from './contexts/ConfigContext.tsx';
import { AuthProvider } from './auth';
import { CustomizationProvider } from './contexts/CustomizationContext.tsx';

import './i18n';

fetchConfig().then(config => {
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <ConfigContext.Provider value={config}>
        <AuthProvider>
          <CustomizationProvider>
            <App />
          </CustomizationProvider>
        </AuthProvider>
      </ConfigContext.Provider>
    </StrictMode>
  );
});
