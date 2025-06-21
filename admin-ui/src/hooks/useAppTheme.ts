import { useEffect, useMemo, useState } from 'react';
import { createTheme, type Theme } from '@mui/material/styles';
import { createThemeFromConfig } from '../theme/createThemeFromConfig';
import type { ThemeConfig } from '../theme/theme-config';

export function useAppTheme(useCustomFeatures: boolean): {
  theme: Theme;
  themeError: string | null;
} {
  const [cfg, setCfg] = useState<ThemeConfig | null>(null);
  const [themeError, setThemeError] = useState<string | null>(null);

  useEffect(() => {
    async function loadConfig() {
      setThemeError(null);
      let configToSet: ThemeConfig | null = null;

      try {
        if (useCustomFeatures) {
          const customRes = await fetch('/custom-theme-config.json');
          if (customRes.ok) {
            configToSet = await customRes.json();
          } else {
            console.warn(
              `Custom theme config '/custom-theme-config.json' not found or failed (status: ${customRes.status}). Falling back to default theme.`
            );
            const defaultRes = await fetch('/default-theme-config.json');
            if (defaultRes.ok) {
              configToSet = await defaultRes.json();
            } else {
              throw new Error(
                `Default theme config '/default-theme-config.json' also not found (status: ${defaultRes.status}) after custom theme failed.`
              );
            }
          }
        } else {
          const defaultRes = await fetch('/default-theme-config.json');
          if (defaultRes.ok) {
            configToSet = await defaultRes.json();
          } else {
            throw new Error(
              `Failed to load default theme config '/default-theme-config.json' (status: ${defaultRes.status}).`
            );
          }
        }
        setCfg(configToSet);
      } catch (error) {
        const errorMessage =
          error instanceof Error
            ? error.message
            : 'An unknown error occurred while loading theme configuration.';
        console.error('Theme loading error:', errorMessage);
        setThemeError(errorMessage);
        if (cfg !== null) setCfg(null);
      }
    }
    loadConfig();
  }, [useCustomFeatures]);

  const theme = useMemo(() => {
    return cfg ? createThemeFromConfig(cfg) : createTheme();
  }, [cfg]);

  return { theme, themeError };
}
