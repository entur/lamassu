import { createTheme, type Theme } from '@mui/material/styles';
import type { ThemeConfig } from './theme-config';

export function createThemeFromConfig(cfg: ThemeConfig): Theme {
  const { logoUrl, ...themeOptions } = cfg;

  return createTheme({ ...themeOptions, logoUrl });
}
