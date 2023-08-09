FROM eclipse-temurin:17.0.8_7-jdk-alpine

RUN apk update && apk upgrade && apk add --no-cache \
    tini

RUN addgroup appuser && adduser --disabled-password appuser --ingroup appuser
USER appuser

WORKDIR /home/appuser

RUN chown -R appuser:appuser /home/appuser
USER appuser

COPY target/lamassu-0.0.1-SNAPSHOT.jar lamassu.jar

CMD [ "/sbin/tini", "--", "java", "-jar", "lamassu.jar" ]