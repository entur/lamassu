# The builder runs on the build host's platform: the extracted layers are the same for every
# architecture, so the multi-arch build needs no emulation.
FROM --platform=$BUILDPLATFORM gcr.io/distroless/java25-debian13:nonroot@sha256:4618e7a1a1f5177222985347e1ffd503076f6be5837b89c8fd02800bd8551bde AS builder
WORKDIR /home/nonroot
COPY target/lamassu-*-SNAPSHOT.jar application.jar
RUN [ "/usr/bin/java", "-Djarmode=tools", "-jar", "application.jar", "extract", "--layers", "--destination", "extracted" ]

FROM gcr.io/distroless/java25-debian13:nonroot@sha256:4618e7a1a1f5177222985347e1ffd503076f6be5837b89c8fd02800bd8551bde
WORKDIR /deployments
COPY --from=builder /home/nonroot/extracted/dependencies/ ./
COPY --from=builder /home/nonroot/extracted/spring-boot-loader/ ./
COPY --from=builder /home/nonroot/extracted/snapshot-dependencies/ ./
COPY --from=builder /home/nonroot/extracted/application/ ./
EXPOSE 8080
# No shell, so JVM flags come from JDK_JAVA_OPTIONS, which the launcher reads itself.
# Kryo uses sun.misc.Unsafe, which JDK 25 warns about on stderr without the flag.
ENTRYPOINT [ "/usr/bin/java", "--sun-misc-unsafe-memory-access=allow", "-jar", "application.jar" ]
