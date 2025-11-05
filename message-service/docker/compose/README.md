# Docker Compose para message-service

Instrucciones para construir y ejecutar el servicio `message-service` localmente.

Pasos rápidos:

```bash
cd message-service/docker/compose
# Construir y levantar en background
docker compose up -d --build
```

Cambiar puerto y variables:
- Copie `../env/.env.example` a `../env/.env.local` y edite variables.

Comprobaciones:

- Health:
```bash
curl http://localhost:8080/actuator/health
```

- Métricas Prometheus:
```
http://localhost:8080/actuator/prometheus
```

- Registro en Eureka:
- Abra la UI de Eureka (por ejemplo `http://eureka-server:8761/`) y verifique que aparece `message-service`.

- Trazas OpenTelemetry:
- Asegúrese que `OTEL_EXPORTER_OTLP_ENDPOINT` apunte al collector/tempo correcto; las trazas deben aparecer en Grafana/Tempo.

Troubleshooting:
- Ver logs:
```
docker logs -f message-service
```
- Ver variables de entorno en el contenedor:
```
docker exec -it message-service env
```
- Problemas de registro en Eureka: compruebe conectividad DNS/puerto hacia el servidor Eureka y las variables `EUREKA_*`.
- Problemas de trazas: confirme que `OTEL_EXPORTER_OTLP_ENDPOINT` es alcanzable desde el contenedor y que el OTel Collector acepta `grpc` en el endpoint configurado.

Notas:
- Este compose no crea servicios de Eureka, Prometheus, Grafana ni OTel Collector. Deben estar desplegados en la misma red (o como redes conectadas).
- El nombre lógico registrado será `message-service` (vía SPRING_APPLICATION_NAME y OTEL_SERVICE_NAME).

