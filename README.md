# Lamassu [![CircleCI](https://circleci.com/gh/entur/lamassu.svg?style=svg)](https://circleci.com/gh/entur/lamassu) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=entur_lamassu&metric=alert_status)](https://sonarcloud.io/dashboard?id=entur_lamassu)

Mobility aggregation service based on the [General Bikeshare Feed Specification (GBFS) v2.2](https://github.com/NABSA/gbfs/blob/v2.2/gbfs.md).

### Configuration

`resources/feedproviders.yml` lists GBFS feeds which will be polled by this application:

        lamassu:
            providers:
              
                systemName: My System
                operatorId: MFM:Operator:myoperator
                operatorName: My operator
                codespace: MFM
                url: https://myfavoritegbfsfeeed.com/gbfs.json
                language: en

This will use the GBFS auto-discovery at `url` and poll `en` language feeds.

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

### Deployment

Deployed with helm+harness+terraform.

Accessible via Entur API (e.g. `https://api(-env).entur.io/mobility/v2/`).
