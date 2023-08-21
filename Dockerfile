FROM eclipse-temurin:17.0.8_7-jdk-alpine as builder

# Cache-mount /root/.m2 to speed up subsequent builds.
# Bind-mount pom.xml, .mvn & mvnw to avoid having to copy it.
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=bind,source=.mvn,target=.mvn \
    --mount=type=bind,source=mvnw,target=mvnw \
    --mount=type=cache,target=/root/.m2/ \
    ./mvnw dependency:copy-dependencies

COPY . ./

# Cache-mount /root/.m2 to speed up subsequent builds.
# Bind-mount pom.xml, .mvn & mvnw to avoid having to copy it.
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=bind,source=.mvn,target=.mvn \
    --mount=type=bind,source=mvnw,target=mvnw \
    --mount=type=cache,target=/root/.m2/ \
    env CI=true \
    ./mvnw install -DskipTests -P prettierSkip

FROM eclipse-temurin:17.0.8_7-jre-alpine

RUN apk update && apk upgrade && apk add --no-cache \
    tini

RUN addgroup appuser && adduser --disabled-password appuser --ingroup appuser
USER appuser

WORKDIR /home/appuser

RUN chown -R appuser:appuser /home/appuser
USER appuser

EXPOSE 8080
EXPOSE 9001

# todo: don't hard-code lamassu's version here
COPY --from=builder target/lamassu-0.0.1-SNAPSHOT.jar lamassu.jar

CMD [ "/sbin/tini", "--", "java", "-jar", "lamassu.jar" ]