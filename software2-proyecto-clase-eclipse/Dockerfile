# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /build

# Maven wrapper del backend
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/
COPY pom.xml pom.xml

# === Copiar e instalar dependencias locales USANDO additional_contexts ===
# observability-confi
COPY --from=obs / ./observability-confi/
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q -f observability-confi/pom.xml -DskipTests install

# message-service
COPY --from=msg / ./message-service/
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q -f message-service/pom.xml -DskipTests install

# Resolver deps del backend
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -U dependency:go-offline -DskipTests

# Compilar backend
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -DskipTests package && \
    mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

# Extraer capas (layertools)
FROM eclipse-temurin:21-jdk-jammy AS extract
WORKDIR /build
COPY --from=build /build/target/app.jar target/app.jar
RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

# Imagen final
FROM eclipse-temurin:21-jre-jammy AS final
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

WORKDIR /app
COPY --from=extract /build/target/extracted/dependencies/ ./
COPY --from=extract /build/target/extracted/spring-boot-loader/ ./
COPY --from=extract /build/target/extracted/snapshot-dependencies/ ./
COPY --from=extract /build/target/extracted/application/ ./

EXPOSE 8081
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
