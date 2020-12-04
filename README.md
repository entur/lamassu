# Lamassu [![CircleCI](https://circleci.com/gh/entur/lamassu.svg?style=svg)](https://circleci.com/gh/entur/lamassu) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=entur_lamassu&metric=alert_status)](https://sonarcloud.io/dashboard?id=entur_lamassu)

Mobility aggregation service based on the [General Bikeshare Feed Specification (GBFS) v2.1-RC2](https://github.com/NABSA/gbfs/blob/v2.1-RC2/gbfs.md).

### End-points

#### `/gbfs`

List all GBFS feeds available via this API

#### `/gbfs/{codespace}/{city?}/{vehicleType?}/{feed}`

GBFS feeds for a specific feed provider, minimally defined via codespace, and optionally with
city and vehicle type.

E.g.

    /gbfs/ybo/oslo/scooter/free_bike_status

will return the free_bike_status feed for Bolt's scooter service in Oslo.

#### `/vehicles/nearby?lon={longitude}&lat={latitude}&range={range}&count={count}`

List vehicles nearby, from any feed provider, given a position (longitude, latitude), within a given range in meters and a maximum count.

### Development

Requires a locally running redis instance. E.g.:

    docker run -p 127.0.0.1:6379:6379 -d redis redis-server

### Deployment

Deployed with helm+harness+terraform.

Accessible via Entur API (e.g. `https://api(-env).entur.io/mobility/v2/`).
