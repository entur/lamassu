# Changelog

The changelog lists most feature changes between each release. The list is automatically created
based on merged pull requests. Search GitHub issues and pull requests for smaller issues.

## 1.2.0 (under development)
- Improve: introduce configurable stationEntityCacheMinimumTtl and stationEntityCacheMaximumTtl [#542](https://github.com/entur/lamassu/pull/542)
- Update subscription immediately after registration [#592](https://github.com/entur/lamassu/pull/592)
- Replace graphql kickstart project with spring graphql [#595](https://github.com/entur/lamassu/pull/595)
- Normalize entity data [#596](https://github.com/entur/lamassu/pull/596)
- Add null checks to avoid NPE [#612](https://github.com/entur/lamassu/pull/612)
- Remove redundant entity cache interfaces [#613](https://github.com/entur/lamassu/pull/613)
- Add request-scope cache for reading entities [#619](https://github.com/entur/lamassu/pull/619)
- Only filter out vehicles that are assigned to non-virtual stations [#626](https://github.com/entur/lamassu/pull/626)
- Dedupe codespaces list [#630](https://github.com/entur/lamassu/pull/630)
- Compute delta of GBFS files and refactor vehicles and stations updaters [#543](https://github.com/entur/lamassu/pull/543)
  [](AUTOMATIC_CHANGELOG_PLACEHOLDER_DO_NOT_REMOVE)

## 1.1.0

- fix: do not skip vehicles without pricing plan id (fixes #396) [#397](https://github.com/entur/lamassu/pull/397)
- Support consuming GBFS v3 [#389](https://github.com/entur/lamassu/pull/389)
- Add polylineEncodedMultiPolygon to GeofencingZoneProperties [#403](https://github.com/entur/lamassu/pull/403)
- fix: metrics were not registered correctly, this simplifies the setup [#439](https://github.com/entur/lamassu/pull/439)
- Respond with status code 502 if configured feed not yet available [#432](https://github.com/entur/lamassu/pull/432)
- Do not lowercase feed urls [#455](https://github.com/entur/lamassu/pull/455)
- Switch to v3 as base model for entity mapping [#471](https://github.com/entur/lamassu/pull/471)
- Add v3 fields [#475](https://github.com/entur/lamassu/pull/475)
- fix: incorrect urls generated for system discovery (v2) [#481](https://github.com/entur/lamassu/pull/481)
- feature: add v2 and v3 paths [#486](https://github.com/entur/lamassu/pull/486)
- fix: remove hard-coded minimum ttl for feed caches [#485](https://github.com/entur/lamassu/pull/485)
- Follow up v3 paths [#488](https://github.com/entur/lamassu/pull/488)
- feature: support authenticated redis connection [#487](https://github.com/entur/lamassu/pull/487)
- CI: build & publish linux/arm64 images [#253](https://github.com/entur/lamassu/pull/253)
- Map current_range_meters as provided instead of setting a missing value to 0.0 [#512](https://github.com/entur/lamassu/pull/512)
- Improve/id validation [#516](https://github.com/entur/lamassu/pull/516)
- Encoding stations.stationArea with polyline [#526](https://github.com/entur/lamassu/pull/526)
- Improve: allow configurable minimum max-age for cache-control directive [#530](https://github.com/entur/lamassu/pull/530)
- Add subscription update interceptor to add systemId to update logs [#518](https://github.com/entur/lamassu/pull/518)
- Add bounding box search functionality to vehicles and stations queries [#527](https://github.com/entur/lamassu/pull/527)

## 1.0.0

First major release
