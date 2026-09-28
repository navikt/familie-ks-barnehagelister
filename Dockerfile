# Final image
FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25@sha256:dec8e70f5f2a046453c1d7143972ab579a4ddf9eecbcc8989af405a32fb77f4c
COPY --chown=nonroot:nonroot ./target/familie-ks-barnehagelister.jar /app/app.jar
WORKDIR /app

ENV APP_NAME=familie-ks-barnehagelister
ENV TZ="Europe/Oslo"
# TLS Config works around an issue in OpenJDK... See: https://github.com/kubernetes-client/java/issues/854
ENTRYPOINT [ "java", "-Djdk.tls.client.protocols=TLSv1.2", "-XX:MinRAMPercentage=25.0", "-XX:MaxRAMPercentage=75.0", "-XX:+HeapDumpOnOutOfMemoryError", "-jar", "/app/app.jar" ]

