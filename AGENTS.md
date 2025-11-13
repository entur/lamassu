# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.

## Project Overview

Lamassu is a mobility aggregation service based on the General Bikeshare Feed Specification (GBFS). It aggregates GBFS feeds from multiple mobility providers (bikes, scooters, mopeds, etc.) and exposes them via:
- GBFS v2.3 and v3.0 REST endpoints
- GraphQL API for querying vehicles and stations
- GraphQL subscriptions for real-time updates (experimental)
- Admin UI for managing feed providers (authenticated)
- **Public Status UI for viewing provider status (no authentication)**

The application polls configured GBFS feeds, validates them, caches the data in Redis, and serves aggregated views. It supports both GBFS v2.x and v3.x feeds as input and output.

**Technology Stack**:
- Backend: Spring Boot 3.x, Java 21, Redisson (Redis client with Kryo serialization)
- Frontend: React 19, TypeScript, Material-UI (MUI), Vite, MapLibre
- Build: Maven 3.x with Prettier Java for code formatting
- Testing: JUnit, WireMock, embedded Redis
- Containerization: Docker (via Jib Maven plugin, multi-arch: amd64/arm64)

## Common Development Commands

### Building and Testing

```bash
# Run tests
mvn test

# Run tests with code coverage
mvn jacoco:prepare-agent test jacoco:report

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName

# Build the project (skips tests)
mvn clean install -DskipTests

# Build Docker image locally
mvn jib:dockerBuild -Dmaven.test.skip -P prettierSkip
```

### Code Formatting

Lamassu uses Prettier Java for code formatting. Always format code before committing:

```bash
# Format all Java code
mvn prettier:write

# Check formatting without modifying files
mvn prettier:check

# Skip prettier during build (useful for quick builds)
mvn install -P prettierSkip
```

**Important**: The CI pipeline validates formatting. Code that is not formatted will fail CI checks.

### Running Locally

Requires Redis:

```bash
# Start Redis with Docker
docker run -p 127.0.0.1:6379:6379 -d redis redis-server

# Run the application
mvn spring-boot:run

# Or use the Maven wrapper
./mvnw spring-boot:run
```

The application will start on port 8081 (configurable in `application.properties`).

### Admin UI Development

The admin UI is a React + TypeScript application in the `admin-ui/` directory, based on the [Inanna](https://github.com/entur/inanna) template (React + MUI + MapLibre). It builds into `src/main/resources/static/admin` and is served by Spring Boot.

```bash
cd admin-ui

# Install dependencies
npm install

# Start development server (proxies /admin/ and /validation/ to localhost:8080)
npm run dev

# Build for production (outputs to ../src/main/resources/static/admin)
npm run build

# Lint code
npm run lint

# Check code formatting
npm run check

# Format code
npm run format
```

The admin UI is served by the Spring Boot application when `org.entur.lamassu.enable-admin-ui=true`. During development, the Vite dev server proxies backend API calls to `localhost:8080`. The production build is integrated into the Docker image via Maven.

## High-Level Architecture

### Multi-Instance Architecture with Leader Election

Lamassu supports running multiple instances for high availability, but only one instance (the "leader") actively polls GBFS feeds at a time:

- **Leader Profile**: The instance with `spring.profiles.active=leader` performs feed polling via `FeedUpdater`
- **Follower Instances**: Other instances serve cached data from Redis but do not poll feeds
- All instances can serve GBFS and GraphQL requests from the shared Redis cache

### Feed Polling and Update Flow

1. **FeedUpdater** (leader only): Manages GBFS feed subscriptions using `GbfsSubscriptionManager`
   - Creates subscriptions for each enabled feed provider in `feedproviders.yml` or Redis
   - Handles both v2 and v3 GBFS feeds
   - Validates feeds if `org.entur.lamassu.enableValidation=true`
   - Retries failed subscriptions after 60 seconds

2. **Feed Cache Updates**: When a feed update is received:
   - V2/V3 feeds are mapped to internal models via `GbfsV2DeliveryMapper` / `GbfsV3DeliveryMapper`
   - Feed-level caches are updated via `V2FeedCachesUpdater` / `V3FeedCachesUpdater`
   - These caches store the raw GBFS feed responses per system

3. **Entity Cache Updates**: For aggregated systems (`aggregate: true` in feed provider config):
   - `EntityCachesUpdater` extracts individual entities (vehicles, stations, etc.)
   - Delta calculation identifies CREATE/UPDATE/DELETE changes via `GBFSFileDeltaCalculator`
   - Individual entities are stored in Redis with codespaced IDs
   - Spatial indexes are updated for geo-queries
   - GraphQL subscriptions are notified of changes

### Data Storage in Redis

Lamassu uses Redis (via Redisson) for all data storage:

- **Feed Caches**: Complete GBFS feed responses per system (e.g., `system_information`, `station_status`)
- **Entity Caches**: Individual vehicles, stations, systems, pricing plans, etc.
- **Spatial Indexes**: Geohash-based indexes for efficient lat/lon range queries
- **Feed Provider Config**: Can be stored in Redis instead of `feedproviders.yml`
- **Validation Results**: GBFS validation errors/warnings (up to 10 per system)

All cached data uses Kryo serialization for efficiency.

### ID Namespacing with Codespaces

To avoid ID conflicts when aggregating multiple feeds, Lamassu prefixes all IDs (except systemId) with a codespace:

- Configured per feed provider: `codespace: TST`
- Applied to operators: `TST:Operator:myoperator`
- Applied to all entity IDs: vehicles, stations, pricing plans, etc.
- The `IdBuilder` class handles codespace prefixing
- Queries can filter by codespace using `IdPredicate` classes

### GraphQL API

The GraphQL API (`/graphql`, schema at `src/main/resources/graphql/schema.graphqls`) provides:

- **Queries**: `vehicles`, `stations`, `geofencingZones` with geo-filtering and attribute filtering
- **Subscriptions**: Real-time updates for vehicles and stations (experimental, marked deprecated)
- **Spatial Queries**: Range-based (lat/lon/range) or bounding box queries
- **Filtering**: By codespace, system, operator, form factor, propulsion type, etc.

GraphQL resolvers are in `src/main/java/org/entur/lamassu/graphql/`.

### GBFS REST API

Lamassu exposes GBFS feeds at:

- `/gbfs` - Discovery endpoint listing all systems
- `/gbfs/{systemId}/{feedName}` - Individual GBFS feeds (e.g., `/gbfs/boltoslo/station_status`)
- `/gbfs/v3/manifest.json` - v3.0 discovery manifest

The application produces both GBFS v2.3 and v3.0 feeds regardless of input version.

### Feed Provider Configuration

Feed providers can be configured via:

1. **File**: `src/main/resources/feedproviders.yml` (default)
2. **Redis**: Enabled via `org.entur.lamassu.feedproviders.source=redis`

Migration from file to Redis is supported via:
```properties
org.entur.lamassu.feedprovider.migrate-from-file-to-redis=true
org.entur.lamassu.feedprovider.migrate-from-file-to-redis.strategy=REPLACE
```

Strategies: `REPLACE_ALL`, `SKIP`, or `REPLACE`.

### Authentication Support

GBFS feeds can require authentication. Supported schemes (configured per provider):

- `OAUTH2_CLIENT_CREDENTIALS_GRANT`: OAuth2 with client credentials
- `BEARER_TOKEN`: Static bearer token
- `HTTP_HEADERS`: Custom HTTP headers

See `AuthenticationScheme` enum and README.md for configuration examples.

### Admin API

The Admin API (`/admin/*`, requires authentication) allows:

- Managing feed providers (add, update, delete, enable/disable)
- Starting/stopping/restarting individual feed subscriptions
- Viewing validation reports
- Viewing system health and metrics

Controllers: `AdminController`, `ValidationController`.

## Key Package Structure

- `cache/`: Redis cache interfaces and implementations
  - `impl/`: Cache implementations for vehicles, stations, systems, etc.
  - Spatial indexes for geo-queries
- `config/`: Spring configuration classes
  - `feedprovider/`: Feed provider configuration loading
  - Security, metrics, scheduling setup
- `controller/`: REST controllers for GBFS feeds, admin API, **status API**, health checks
- `delta/`: Delta calculation for detecting entity changes (CREATE/UPDATE/DELETE)
- `graphql/`: GraphQL resolvers and subscription handling
- `leader/`: Feed polling and update orchestration (leader instance only)
  - `FeedUpdater`: Manages GBFS subscriptions
  - `entityupdater/`: Updates entity caches from feed data
  - `feedcachesupdater/`: Updates feed-level caches
- `mapper/`: Mapping between GBFS models and internal models
  - `entitymapper/`: Entity-level mappers
  - `feedmapper/`: Feed-level mappers (v2 and v3)
- `model/`: Domain models
  - `entities/`: Internal entity models (Vehicle, Station, System, etc.)
  - `provider/`: Feed provider configuration models
  - `id/`: ID handling and namespacing
  - **`dto/`: Data transfer objects for public APIs**
- `service/`: Business logic services
  - `GeoSearchService`: Spatial queries using indexes
  - `FeedProviderService`: Feed provider CRUD operations
- `util/`: Utility classes

## Testing

- Tests are in `src/test/java/org/entur/lamassu/`
- Embedded Redis is used for integration tests
- WireMock is used for mocking HTTP GBFS feeds
- Test naming convention: `*Test.java`

## Important Configuration Properties

Key properties in `application.properties`:

- `org.entur.lamassu.feedproviders`: Path to feedproviders.yml (classpath or file)
- `org.entur.lamassu.feedproviders.source`: `file` or `redis`
- `org.entur.lamassu.enableValidation`: Enable GBFS validation (default: false)
- `org.entur.lamassu.redis.master.host/port`: Redis connection settings
- `org.entur.lamassu.enable-admin-ui`: Enable admin UI (default: false)
- `org.entur.lamassu.enable-admin-endpoints`: Enable admin REST endpoints (default: false)
- **`org.entur.lamassu.enable-status-ui`: Enable public status UI (default: false)**
- **`org.entur.lamassu.enable-status-endpoints`: Enable public status REST endpoints (default: false)**
- `spring.profiles.active`: `leader` for feed polling instance

## Multi-Version GBFS Support

Lamassu handles both GBFS v2.x and v3.x:

- **Input**: Consumes v2.x or v3.x (specified in feed provider config: `version: 3.0`)
- **Output**: Always produces both v2.3 and v3.0 feeds
- **Mapping**: `GbfsFeedVersionMappers` converts between versions
- **Default**: If no version specified, assumes v2.3

The dual-version approach ensures clients can consume either version regardless of what upstream providers publish.

## Metrics and Monitoring

- Prometheus metrics exposed at `/actuator/prometheus` (port 9001 by default)
- `MetricsService` tracks subscription health, validation results, entity counts
- Health checks at `/actuator/health` (liveness and readiness probes)
- GraphiQL playground available at `/graphiql` when enabled

## Code Style and Conventions

- **Formatting**: Prettier Java with 90 char line width, 2-space indentation
- **License**: All Java files must have EUPL license header
- **Logging**: Use SLF4J logger via `LoggerFactory.getLogger(this.getClass())`
- **Dependency Injection**: Use constructor injection with `@Autowired`
- **Profiles**: Use `@Profile("leader")` for leader-only components
