# Docker examples for lamassu

This directory contains docker-compose examples for lamassu.

## Usage

Edit `application-config/feedproviders.yml` so that it contains at least one source feed.

The run docker-compose:

    docker-compose -f docker-compose-simple.yml up

or

    docker-compose -f docker-compose-advanced.yml up

The difference between simple and advanced is that simple runs a single instance of lamassu, whereas
advanced runs two instances, one which is responsible for fetching data from upstream sources and updating
caches (leader), and one which responds to API requests. The second instance may be scaled horizontally, but
the leader can only have one instance, and must be scaled vertically if needed.

