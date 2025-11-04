# syntax=docker/dockerfile:1

################################################################################
# STAGE: deps  — descarga dependencias con el wrapper (mvnw) y cache de ~/.m2
################################################################################
FROM eclipse-temurin:21-jdk-jammy as deps
WORKDIR /build

# Wrapper y ajustes
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/
# POM para resolver deps (aprovecha cache de Docker + cache mount de Maven)
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -e dependency:go-offline -DskipTests

################################################################################
# STAGE: package — compila/empaca el JAR (SIN compilar ni ejecutar tests)
################################################################################
FROM deps as package
WORKDIR /build

# Código fuente
COPY ./src src/

# Empaquetado sin compilar ni ejecutar tests
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -e -Dmaven.test.skip=true package && \
    # Deja el artefacto con nombre estable para el siguiente stage
    cp target/*.jar target/app.jar

################################################################################
# STAGE: extract — extrae capas del JAR (Spring Boot layertools)
################################################################################
FROM package as extract
WORKDIR /build

RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

################################################################################
# STAGE: final — imagen de runtime mínima (JRE 21) con usuario no-root
################################################################################
FROM eclipse-temurin:21-jre-jammy AS final

# Crea usuario no privilegiado
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

# Copia cada capa desde el stage "extract" (OJO: rutas absolutas correctas)
COPY --from=extract /build/target/extracted/dependencies/ ./
COPY --from=extract /build/target/extracted/spring-boot-loader/ ./
COPY --from=extract /build/target/extracted/snapshot-dependencies/ ./
COPY --from=extract /build/target/extracted/application/ ./

# Cambia si tu servicio usa otro puerto (Eureka: 8761)
EXPOSE 8761

# Puedes pasar flags con JAVA_TOOL_OPTIONS/JAVA_OPTS al correr
ENTRYPOINT [ "java", "org.springframework.boot.loader.launch.JarLauncher" ]
