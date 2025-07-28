# Inanna

A Structured Starting Point for Open-Source Frontend Applications.

---

## What is Inanna?

**Inanna** is an open-source starter template designed to streamline the creation of structured, maintainable, themeable and scalable frontend applications. Leveraging modern best practices, Inanna provides a robust foundational framework that developers can easily customize to kickstart their projects.

## Core Technologies

* **React** with **TypeScript**
* **Vite** for fast and efficient builds
* **Material UI (MUI)** for consistent and configurable UI components
* **MapLibre** for interactive map functionalities

---

## Features

* **Configurable Theming:**

  Customize your application's look and feel dynamically via a simple configuration file—perfect for branding, logos, and color schemes.

* **Responsive Layout:**

  Ensures your application maintains aesthetic appeal across desktop and mobile devices.

* **State Management:**

  Integrates modern state management techniques (React Context, optional Redux) for efficient application state handling.

* **Interactive Maps:**

  Pre-configured interactive mapping components powered by MapLibre, excellent for building location-based applications.

---

## How to Use Inanna in Your Project

## Getting Started

* **Clone the repository:**

  ```bash
  git clone https://github.com/entur/inanna.git
  ```

* **Install dependencies:**

  ```bash
  npm install
  ```

* **Run the development server:**

  ```bash
  npm run dev
  ```

## Customizing the Project

* **Update Theme Configuration:**

  Modify `public/custom-theme-config.json` or `public/default-theme-config.json` to adjust colors, logos, typography, and other MUI theme options.

* **Add New Pages and Components:**

  Follow provided examples (e.g., `Home.tsx`, `MapView.tsx`) to create pages. Components are organized in the `src/components/` directory.

* **Customize the Map:**

  Adjust the map style via `src/mapStyle.ts`, or add layers and interactivity directly.

* **Bring Your Own Icons:**

  Add custom icons to `public/static/customIcons/` (SVG or PNG). Override default icons by matching filenames.

---

## 1. Setting Up a Custom Theme

Your application can switch between a default theme and a custom theme. This behavior is controlled by the **Enable Custom Theme & Icons** switch in the settings dialog, which toggles a value in `localStorage`.

## How it works

* **`CustomizationContext.tsx`:** Manages the `useCustomFeatures` state. When `true`, the app attempts to load the custom theme.

* **`App.tsx`:**

    * Uses the `useCustomFeatures` hook.
    * If enabled, fetches `/custom-theme-config.json`.
    * Otherwise or on failure, fetches `/default-theme-config.json`.

* **`public/default-theme-config.json`:** Default Inanna theme settings.

* **`public/custom-theme-config.json`:** Define or override any MUI theme options here.

* **`src/utils/createThemeFromConfig.ts`:** Converts the JSON configuration into an MUI theme object.

## Steps to customize your theme

* **Edit `public/custom-theme-config.json`:**

  ```json
  {
    "applicationName": "My Custom App",
    "companyName": "My Company",
    "palette": {
      "primary": { "main": "#A020F0" },
      "secondary": { "main": "#00BFFF" },
      "background": { "default": "#F5F5F5" }
    },
    "typography": {
      "fontFamily": "\"Open Sans\", \"Helvetica\", \"Arial\", sans-serif",
      "h1": { "fontSize": "2.8rem" }
    },
    "shape": { "borderRadius": 8 },
    "components": {
      "MuiButton": {
        "styleOverrides": {
          "root": { "textTransform": "capitalize" }
        }
      }
    },
    "logoUrl": "/assets/my-custom-logo.png",
    "logoHeight": 32
  }
  ```

* **Enable Custom Features:**

    * Start the app.
    * Open settings (gear icon).
    * Toggle **Enable Custom Theme & Icons**.
    * The app will apply `custom-theme-config.json` on reload.

---

## 2. Adding Custom Icons

The application’s icon loader resolves custom and default icons based on the **Enable Custom Theme & Icons** setting.

## How it works

* **`src/data/iconLoaderUtils.ts`** – `getIconUrl(name: string)` checks:

    1. If custom features enabled:

        * `public/static/customIcons/[name].svg` or `.png`
    2. Otherwise or not found:

        * `public/static/defaultIcons/[name].svg` or `.png`
    3. Fallback to `default.svg` / `default.png` in `defaultIcons`.

## Steps to add/override icons

* **Prepare icons** in SVG or PNG.

* **Place in `public/static/customIcons/`:**

    * Override: same filename as default.
    * Add new: unique filename (e.g., `analytics.svg`).

* **Use in components:**

  ```tsx
  import { getIconUrl } from '../data/iconLoader';
  import { Box } from '@mui/material';

  const analyticsIconUrl = getIconUrl('analytics');

  return (
    <Box
      component="img"
      src={analyticsIconUrl}
      alt="Analytics"
      sx={{ width: 24, height: 24 }}
    />
  );
  ```

* **Enable Custom Features** in settings to view your icons.

---

## 3. Expanding Theming with TypeScript Definitions

Extend MUI’s theme object in `src/types/theme-config.d.ts` for additional custom properties.

## How it works

* **`ThemeConfig` Interface:** Extends MUI’s `ThemeOptions` with custom fields.

* **Module Augmentation:** Adds these fields to `Theme` and `ThemeOptions` via `declare module '@mui/material/styles'`.

## Steps to extend the theme

* **Define new properties** in `src/types/theme-config.d.ts`:

  ```ts
  import type { ThemeOptions } from '@mui/material/styles';

  export interface ThemeConfig extends ThemeOptions {
    applicationName: string;
    companyName: string;
    logoUrl: string;
    logoHeight: number;
    customSpacing: { small: number; medium: number; large: string };
    brandColors: { accentFocus: string };
  }

  declare module '@mui/material/styles' {
    interface Theme {
      applicationName: string;
      companyName: string;
      logoUrl: string;
      logoHeight: number;
      customSpacing: { small: number; medium: number; large: string };
      brandColors: { accentFocus: string };
    }
    interface ThemeOptions {
      applicationName?: string;
      companyName?: string;
      logoUrl?: string;
      logoHeight?: number;
      customSpacing?: { small?: number; medium?: number; large?: string };
      brandColors?: { accentFocus?: string };
    }
  }
  ```

* **Add to JSON configs:**

    * In **`public/default-theme-config.json`**:

      ```json
      {
        "applicationName": "INANNA",
        "companyName": "ROR",
        "logoUrl": "/assets/inanna-logo.png",
        "logoHeight": 48,
        "customSpacing": { "small": 8, "medium": 16, "large": "32px" },
        "brandColors": { "accentFocus": "#FFD700" }
      }
      ```

    * In **`public/custom-theme-config.json`**:

      ```json
      {
        "applicationName": "My Custom App",
        "companyName": "My Company",
        "logoUrl": "/assets/my-custom-logo.png",
        "logoHeight": 32,
        "customSpacing": { "small": 4, "medium": 12, "large": "24px" },
        "brandColors": { "accentFocus": "#10A37F" }
      }
      ```

* **Use custom properties** in components:

  ```tsx
  import { Box, Typography, useTheme } from '@mui/material';

  function MyCustomComponent() {
    const theme = useTheme();
    return (
      <Box sx={{
        padding: theme.customSpacing.medium,
        border: `2px solid ${theme.brandColors.accentFocus}`,
      }}>
        <Typography sx={{ marginBottom: theme.customSpacing.small }}>
          This component uses custom theme properties!
        </Typography>
        <Typography sx={{ fontSize: theme.customSpacing.large }}>
          Large Text!
        </Typography>
      </Box>
    );
  }

  export default MyCustomComponent;
  ```

The `createThemeFromConfig` utility in `src/utils/createThemeFromConfig.ts` will automatically include all custom properties defined in your theme configs.

---

By following these instructions, you can fully customize the Inanna application’s theme, icons, and extend its theming system in a type-safe manner.
