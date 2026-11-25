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

## The image

The image runs Java 25 on distroless as user 65532, with no shell or package manager. Pass JVM
flags and system properties in `JDK_JAVA_OPTIONS`, as the compose files do. Mounted config files
must be readable by that user. For a shell, build on the `debug-nonroot` tag of the same base
image (`gcr.io/distroless/java25-debian13:debug-nonroot`).
