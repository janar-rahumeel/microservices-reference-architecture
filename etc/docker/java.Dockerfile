FROM eclipse-temurin:25-jdk AS builder

ARG MODULE

WORKDIR /build

COPY target/microservices-reference-architecture-${MODULE}-*.jar application.jar

RUN java -Djarmode=tools -jar application.jar \
    extract --layers --destination extracted

RUN jlink \
    --add-modules java.se,jdk.crypto.ec,jdk.unsupported,jdk.management,jdk.management.agent,jdk.net,jdk.jfr,jdk.localedata \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=zip-6 \
    --output /javaruntime

FROM gcr.io/distroless/base-debian13:nonroot

ARG MODULE
ARG IMAGE_VERSION=unknown
ARG IMAGE_REVISION=unknown

LABEL org.opencontainers.image.title="mra-${MODULE}" \
      org.opencontainers.image.version="${IMAGE_VERSION}" \
      org.opencontainers.image.revision="${IMAGE_REVISION}"

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

WORKDIR /app

COPY --from=builder /javaruntime /opt/java/openjdk/

COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/application/ ./

USER nonroot

ENTRYPOINT ["java", "-jar", "application.jar"]
