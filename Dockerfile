# The builder runs on the build host's platform: the extracted layers are the same for every
# architecture, so the multi-arch build needs no emulation.
FROM --platform=$BUILDPLATFORM gcr.io/distroless/java25-debian13:nonroot@sha256:59bbb349c9752fc57b75744b41b1614c9dabe8bb4869523aabd5a4288f6c09a0 AS builder
WORKDIR /home/nonroot
COPY target/lamassu-*-SNAPSHOT.jar application.jar
RUN [ "/usr/bin/java", "-Djarmode=tools", "-jar", "application.jar", "extract", "--layers", "--destination", "extracted" ]

FROM gcr.io/distroless/java25-debian13:nonroot@sha256:59bbb349c9752fc57b75744b41b1614c9dabe8bb4869523aabd5a4288f6c09a0
WORKDIR /deployments
COPY --from=builder /home/nonroot/extracted/dependencies/ ./
COPY --from=builder /home/nonroot/extracted/spring-boot-loader/ ./
COPY --from=builder /home/nonroot/extracted/snapshot-dependencies/ ./
COPY --from=builder /home/nonroot/extracted/application/ ./
EXPOSE 8080
# No shell, so JVM flags come from JDK_JAVA_OPTIONS, which the launcher reads itself.
# Kryo uses sun.misc.Unsafe, which JDK 25 warns about on stderr without the flag.
ENTRYPOINT [ "/usr/bin/java", "--sun-misc-unsafe-memory-access=allow", "-jar", "application.jar" ]
