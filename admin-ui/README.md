# Lamassu Admin UI

Web-based administration interface for managing Lamassu's GBFS feed aggregation service.

## Overview

The Admin UI is a React + TypeScript single-page application built on the [Inanna](https://github.com/entur/inanna) template. It provides a graphical interface for managing feed providers, monitoring subscriptions, controlling caches, and viewing validation reports.

The application is served by the Lamassu Spring Boot backend at `/admin` when `org.entur.lamassu.enable-admin-ui=true` is configured.

## Features

### Feed Provider Management

**CRUD Operations:**
- Create, read, update, and delete GBFS feed provider configurations
- Copy existing providers to quickly create similar configurations
- Configure authentication (OAuth2, Bearer Token, HTTP Headers)
- Set GBFS version (2.x or 3.x), codespace, operator details, and language preferences

**Subscription Control:**
- Start, stop, and restart individual feed subscriptions in real-time
- Monitor subscription status (STARTED, STOPPED, STARTING, STOPPING)
- Enable/disable providers to control whether they are polled

**Bulk Operations:**
- Select multiple feed providers for batch operations
- Bulk start, stop, restart subscriptions
- Bulk enable/disable providers
- Useful for managing many feeds efficiently

**Validation Reports:**
- View GBFS validation results for each feed provider
- Identify errors and warnings in feed data
- Quick access to validation status with visual indicators

### Cache Management

- View all Redis cache keys
- Clear vehicle cache
- Clear old/stale cache entries
- Full database wipe (use with caution)

### Vehicle Orphan Management

- Identify "orphaned" vehicles (vehicles in cache without an active feed provider)
- Remove orphaned vehicles to keep the cache clean

### Spatial Index Management

- Monitor and manage geospatial indexes used for location-based queries

## Tech Stack

- **React 19** with **TypeScript**
- **Vite** - Fast development server and build tool
- **Material UI (MUI)** - Component library for UI
- **React Router** - Client-side routing
- **Axios** - HTTP client for API calls
- **i18next** - Internationalization framework
- **MapLibre** - Interactive maps (via Inanna template)

## Development

### Prerequisites

- Node.js 22.x
- Running Lamassu backend on `localhost:8080` (see main README)

### Getting Started

```bash
# Install dependencies
npm install

# Start development server (runs on port 5000 by default)
npm run dev
```

The dev server automatically proxies API requests to the backend:
- `/admin/*` → `http://localhost:8080/admin/*`
- `/validation/*` → `http://localhost:8080/validation/*`

Open [http://localhost:5000/admin](http://localhost:5000/admin) in your browser.

### Available Scripts

```bash
# Development server with hot reload
npm run dev

# Build for production (outputs to ../src/main/resources/static/admin)
npm run build

# Lint code
npm run lint

# Check code formatting
npm run check

# Format code with Prettier
npm run format
```

### Build Integration

The production build is integrated into the Spring Boot application:

1. **Output Directory**: `npm run build` outputs to `../src/main/resources/static/admin`
2. **Base Path**: The build uses `--base /admin` to ensure assets load correctly
3. **Docker Build**: The CI pipeline runs `npm install && npm run build` before building the Docker image
4. **Static Serving**: Spring Boot serves the built assets when the admin UI is enabled

## Project Structure

```
admin-ui/
├── src/
│   ├── components/       # Reusable UI components
│   │   ├── admin/        # Admin-specific components (FeedProviderForm, etc.)
│   │   ├── common/       # Shared components
│   │   ├── dialogs/      # Dialog components
│   │   ├── header/       # Header and navigation
│   │   └── validation/   # Validation report components
│   ├── pages/            # Page components (route targets)
│   │   ├── Home.tsx                    # Landing page
│   │   ├── AdminFeedProviders.tsx      # Feed provider management
│   │   ├── AdminCacheManagement.tsx    # Cache operations
│   │   └── AdminSpatialIndex.tsx       # Spatial index management
│   ├── services/         # API client services
│   │   ├── adminApi.ts       # Admin API endpoints
│   │   └── validationApi.ts  # Validation API endpoints
│   ├── types/            # TypeScript type definitions
│   ├── contexts/         # React contexts (theme, customization)
│   ├── hooks/            # Custom React hooks
│   ├── locales/          # i18n translation files
│   ├── theme/            # MUI theme configuration
│   ├── utils/            # Utility functions
│   └── App.tsx           # Root application component
├── public/               # Static assets
├── vite.config.ts        # Vite configuration
├── package.json          # Dependencies and scripts
└── tsconfig.json         # TypeScript configuration
```

## Configuration

### Environment Variables

The Vite dev server port can be configured:

```bash
PORT=3000 npm run dev  # Start on port 3000 instead of default 5000
```

### Theme Customization

The UI supports custom theming via the Inanna template's theme system:

- **Default theme**: `public/default-theme-config.json`
- **Custom theme**: `public/custom-theme-config.json`

Enable custom features in the settings dialog to apply custom themes and icons.

## Backend Integration

The Admin UI communicates with Lamassu's Admin API endpoints:

### Admin API (`/admin/*`)

- `GET /admin/feed-providers` - List all feed providers
- `POST /admin/feed-providers` - Create new feed provider
- `PUT /admin/feed-providers/{systemId}` - Update feed provider
- `DELETE /admin/feed-providers/{systemId}` - Delete feed provider
- `POST /admin/feed-providers/{systemId}/start` - Start subscription
- `POST /admin/feed-providers/{systemId}/stop` - Stop subscription
- `POST /admin/feed-providers/{systemId}/restart` - Restart subscription
- `POST /admin/feed-providers/{systemId}/set-enabled` - Enable/disable provider
- `POST /admin/feed-providers/bulk/*` - Bulk operations
- `GET /admin/cache_keys` - Get cache keys
- `POST /admin/clear_vehicle_cache` - Clear vehicle cache
- `POST /admin/clear_old_cache` - Clear old cache
- `POST /admin/clear_db` - Clear database
- `GET /admin/vehicle_orphans` - Get orphaned vehicles
- `DELETE /admin/vehicle_orphans` - Clear orphaned vehicles

### Validation API (`/validation/*`)

- `GET /validation/reports` - Get validation reports for all systems

## Authentication

The Admin API requires authentication when `org.entur.lamassu.enable-admin-endpoints=true` is configured with security enabled. Ensure your Lamassu backend is properly configured for authentication.

## Production Deployment

The admin UI is automatically included in the Lamassu Docker image when built via CI:

1. CI runs `npm install --prefix admin-ui && npm run build --prefix admin-ui -- --base /admin`
2. Built files are placed in `src/main/resources/static/admin/`
3. Maven packages these static files into the JAR
4. Spring Boot serves the UI at `/admin` when enabled

### Enabling in Production

Add to `application.properties`:

```properties
org.entur.lamassu.enable-admin-ui=true
org.entur.lamassu.enable-admin-endpoints=true
```

Access the UI at `http://your-lamassu-host:8080/admin`

## Development Tips

- **Hot Reload**: Vite provides instant hot module replacement during development
- **Type Safety**: Use TypeScript types from `src/types/` for API contracts
- **Code Formatting**: Run `npm run format` before committing (enforced by lint-staged)
- **Component Library**: Leverage MUI's extensive component library for UI consistency
- **API Errors**: The UI displays error messages from backend API responses

## Troubleshooting

**Port 5000 already in use:**
```bash
PORT=3001 npm run dev  # Use a different port
```

**Proxy errors:**
- Ensure Lamassu backend is running on `localhost:8080`
- Check that admin endpoints are enabled in backend configuration

**Build fails:**
- Ensure TypeScript compilation succeeds: `tsc -b`
- Check for missing dependencies: `npm install`

## Contributing

When adding new features to the admin UI:

1. Add TypeScript types to `src/types/`
2. Create API client methods in `src/services/`
3. Build UI components in `src/components/`
4. Add routes in `App.tsx` and pages in `src/pages/`
5. Follow existing patterns for error handling and loading states
6. Format code with Prettier before committing

## License

See [LICENSE.txt](../LICENSE.txt) in the root of the Lamassu repository.