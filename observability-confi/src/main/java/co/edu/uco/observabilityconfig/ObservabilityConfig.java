package co.edu.uco.observabilityconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * Starter de observabilidad.
 * Con las dependencias del POM, Spring Boot activa tracing (OTel) + Actuator.
 */
@AutoConfiguration
public class ObservabilityConfig {
    public ObservabilityConfig() {
        System.out.println("✅ Observability starter cargado (OTel + Actuator listos).");
    }
}
