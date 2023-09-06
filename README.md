# Lamassu [![CircleCI](https://circleci.com/gh/entur/lamassu.svg?style=svg)](https://circleci.com/gh/entur/lamassu) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=entur_lamassu&metric=alert_status)](https://sonarcloud.io/dashboard?id=entur_lamassu)

Mobility aggregation service based on the [General Bikeshare Feed Specification (GBFS) v2.2](https://github.com/NABSA/gbfs/blob/v2.2/gbfs.md).

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
                scheme: oauth2ClientCredentialsGrant
                properties:
                    tokenUrl: "https://mytokenurl.example/"
                    clientId: my-client-id
                    clientPassword: my-client-password
            ...
           - systemId: my_bearer_secured_system
            authentication:
                scheme: bearerToken
                properties:
                    accessToken: my-access-token
            ...
          - systemId: my_http_headers_secured_system
            authentication:
                scheme: httpHeaders
                properties:
                    x-client-id: my-client-id 
                    x-api-key: ${my-systemproperty-provided-secret-api-key}
            ...

Note: instead of providing credentials in the feedproviders.yml, you could also pass them via ENV variables/system properties.

### End-points

#### `/gbfs`

List all GBFS feeds available via this API

#### `/gbfs/{system}/{feed}`

GBFS feeds for a specific feed system:

E.g.

    /gbfs/boltoslo/free_bike_status

will return the free_bike_status feed for Bolt's scooter service in Oslo.

### `/graphql`

GraphQL endpoint targeted at end-user clients. Documentation can be explored at `/graphiql`.

### Development

Requires a locally running redis instance. E.g.:

    docker run -p 127.0.0.1:6379:6379 -d redis redis-server

## Codestyle
Lamassu uses [Prettier Java](https://github.com/jhipster/prettier-java). Use `mvn prettier:write` to reformat code before
pushing changes. You can also configure your IDE to reformat code when you save a file.
