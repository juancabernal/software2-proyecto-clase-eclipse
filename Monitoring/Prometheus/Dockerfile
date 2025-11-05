# prometheus/Dockerfile
FROM prom/prometheus:latest

# Copiamos el archivo de configuración
COPY prometheus.yml /etc/prometheus/prometheus.yml

# Exponemos el puerto por el que escucha Prometheus
EXPOSE 9090

# Comando de arranque por defecto
CMD ["--config.file=/etc/prometheus/prometheus.yml", "--web.enable-lifecycle"]
