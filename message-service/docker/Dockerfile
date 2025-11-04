# Dockerfile multi-stage para message-service
# - Builder: usa Maven + Eclipse Temurin 21 para compilar sin tests (cache de dependencias)
# - Runtime: imagen ligera basada en eclipse-temurin:21-jre-alpine
# - Incluye el agente OpenTelemetry (OTel Java Agent) en /otel/otel-javaagent.jar
# Ajusta variables vía env / docker-compose (no modifiques application.yml)

# ---------------------------
# Stage: builder
# ---------------------------
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copiar sólo pom.xml primero para aprovechar cache de dependencias
COPY pom.xml ./
# Si el proyecto tiene wrapper, copiarlo también acelera builds locales en algunos casos
# (se copia después de la fase de dependencias para no invalidar cache inicial)

# Resolver dependencias en modo offline para cachear en layer
RUN mvn -B -DskipTests dependency:go-offline

# Copiar código fuente y otros ficheros necesarios para compilar
COPY src ./src
# Copiar wrapper si existe (no obligatorio)
COPY mvnw ./mvnw
COPY .mvn .mvn

# Compilar el jar sin ejecutar tests
RUN mvn -B -DskipTests -Dmaven.test.skip=true package

# Preparar artefacto final en un lugar conocido
RUN mkdir -p /workspace/app && cp target/*.jar /workspace/app/app.jar

# ---------------------------
# Stage: runtime
# ---------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Herramientas necesarias para healthchecks / descargas
RUN apk add --no-cache curl \
    && addgroup -S app && adduser -S app -G app \
    && mkdir -p /otel \
    && curl -sSL -o /otel/otel-javaagent.jar "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.32.0/opentelemetry-javaagent.jar" \
    && chown -R app:app /app /otel

# Copiar el jar compilado desde el builder
COPY --from=builder /workspace/app/app.jar /app/app.jar

# Ejecutar como usuario no-root
USER app

# Puerto por defecto (modificable por env)
EXPOSE 8082

# Healthcheck básico (usa actuator/health)
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

# Variables soportadas en runtime (valores por defecto; pueden ser sobrescritas por docker-compose/env)
ENV SERVER_PORT=8082
ENV EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
ENV EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
ENV SPRING_APPLICATION_NAME=message-service
ENV MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,prometheus
ENV MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED=true
ENV MANAGEMENT_SERVER_PORT=8082
ENV OTEL_SERVICE_NAME=message-service
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317
ENV OTEL_EXPORTER_OTLP_PROTOCOL=grpc
ENV OTEL_METRICS_EXPORTER=none
ENV OTEL_TRACES_SAMPLER=parentbased_always_on

# Nota: si necesita inyectar opciones de JVM, usar la variable JAVA_OPTS al ejecutar
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -javaagent:/otel/otel-javaagent.jar -jar /app/app.jar"]
