import type { ThemeOptions } from '@mui/material/styles';

export interface ThemeConfig extends ThemeOptions {
  applicationName: string;
  companyName: string;
  logoUrl: string;
  logoHeight: number;
  //add your own custom additions here.
}

declare module '@mui/material/styles' {
  interface Theme {
    applicationName: string;
    companyName: string;
    logoUrl: string;
    logoHeight: number;
    // add more custom fields here
  }
  interface ThemeOptions {
    applicationName?: string;
    companyName?: string;
    logoUrl?: string;
    logoHeight?: number;
    // add more custom fields here
  }
}
