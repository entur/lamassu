# Lamassu

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=entur_lamassu&metric=alert_status)](https://sonarcloud.io/dashboard?id=entur_lamassu)
![Github Workflow](https://github.com/entur/lamassu/actions/workflows/ci.yml/badge.svg)

Mobility aggregation service based on the [General Bikeshare Feed Specification (GBFS)](https://github.com/MobilityData/gbfs).

### Configuration

`resources/feedproviders.yml` lists GBFS feeds which will be polled by this application:

        lamassu:
            providers:
              - systemId: mysystem
                operatorId: MFM:Operator:myoperator
                operatorName: My operator
                codespace: MFM
                url: https://myfavoritegbfsfeeed.com/gbfs.json
                language: en

This will use the GBFS auto-discovery at `url` and poll `en` language feeds.

NOTE: codespace is relevant when aggregating entities across systems, to avoid conflicts between IDs. 
Every ID (except systemId, which (by GBFS convention) should already be globally unique) is prefixed by this codespace.
At least every operator should be assigned its own unique codespace, assuming operators already maintain data / ID separation accross their feeds.
The codespaced operatorId should comply with the following format `<codespace>:Operator:<operator>`.

#### Configuring Authentication

Some providers may require authentication, e.g. via bearer token, OAuth2 or custom http-headers.

    lamassu:
        providers:
          - systemId: my_oauth2_secured_system
            authentication:
                scheme: OAUTH2_CLIENT_CREDENTIALS_GRANT
                properties:
                    tokenUrl: "https://mytokenurl.example/"
                    clientId: my-client-id
                    clientPassword: my-client-password
                    scope: optional-scope
            ...
          - systemId: my_bearer_secured_system
            authentication:
                scheme: BEARER_TOKEN
                properties:
                    accessToken: my-access-token
            ...
          - systemId: my_http_headers_secured_system
            authentication:
                scheme: HTTP_HEADERS
                properties:
                    x-client-id: my-client-id 
                    # you can use a system variable or an environment variable here
                    x-api-key: ${my-systemproperty-provided-secret-api-key}
            ...

Note: Keep in mind you should be cautious about storing your credentials in plain text ! Instead of providing credentials in the feedproviders.yml, it might be more appropriate to provide them via ENV variables/system properties. 

#### Migrating Feed Providers from File to Redis

Lamassu supports migrating feed provider configurations from file-based storage (`feedproviders.yml`) to Redis. This is useful when transitioning to a more scalable and dynamic configuration approach. To enable migration:

```properties
# Enable migration from file to Redis
org.entur.lamassu.feedprovider.migrate-from-file-to-redis=true

# Choose a merge strategy: REPLACE_ALL, SKIP, or REPLACE
org.entur.lamassu.feedprovider.migrate-from-file-to-redis.strategy=REPLACE
```

Available migration strategies:

- `REPLACE_ALL`: Replaces all feed providers in Redis with those from the file
- `SKIP`: Only adds providers from the file that don't already exist in Redis
- `REPLACE`: Adds new providers and updates existing ones in Redis based on the file configuration

The migration runs automatically on application startup when enabled. After successful migration, you can disable the migration property and manage feed providers through the admin interface or Redis directly.

### End-points

#### `/gbfs`

List all GBFS feeds available via this API

#### `/gbfs/{system}/{feed}`

GBFS feeds for a specific feed system:

E.g.

    /gbfs/boltoslo/free_bike_status

will return the free_bike_status feed for Bolt's scooter service in Oslo.

#### `/graphql`

GraphQL endpoint targeted at end-user clients. Documentation can be explored at `/graphiql`.

#### Consuming

Lamassu supports consuming v2.x and v3.x. The version has to be marked in the feedprovider
config, e.g.:

    - systemId: testozon
      operatorId: OZO:Operator:testozon
      operatorName: Ozon
      url: http://test.com/gbfs
      version: 3.0
      codespace: OZO
      language: en

Version defaults to 2.3.

#### Versions

Lamassu produces both v2.3 and v3.0.

v3.0 feeds can be found in the manifest: `/gbfs/v3/manifest.json`.
`manifest.json` replaces the proprietary system discovery endpoint, which will 
is now deprecated.

### Development

Requires a locally running redis instance. E.g.:

    docker run -p 127.0.0.1:6379:6379 -d redis redis-server

## Codestyle
Lamassu uses [Prettier Java](https://github.com/jhipster/prettier-java). Use `mvn prettier:write` to reformat code before
pushing changes. You can also configure your IDE to reformat code when you save a file.
