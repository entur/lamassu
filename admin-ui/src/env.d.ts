declare module 'virtual:theme-config' {
  export interface ThemeConfig {
    palette: {
      primary: { main: string };
      secondary: { main: string };
      [key: string]: unknown;
    };
    typography: {
      fontFamily: string;
      [key: string]: unknown;
    };
    branding: {
      logoUrl: string;
      [key: string]: unknown;
    };
    [key: string]: unknown;
  }
  const value: ThemeConfig;
  export default value;
}
