package co.edu.uco.ucochallenge.infrastructure.primary.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;

@Configuration
public class ObservabilityConfiguration {

    @Bean
    MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(
            @Value("${spring.application.name:uco-challenge}") final String appName,
            @Value("${uco-challenge.telemetry.region:local}") final String region) {
        return registry -> registry.config()
                .commonTags(List.of(
                        Tag.of("application", appName),
                        Tag.of("region", region)
                ));
    }

    @Bean
    MeterFilter commonTagsMeterFilter(@Value("${spring.application.name:uco-challenge}") final String appName) {
        return MeterFilter.commonTags(List.of(Tag.of("application", appName)));
    }

    @Bean
    ObservationRegistryCustomizer<ObservationRegistry> observationRegistryCustomizer(
            @Value("${spring.application.name:uco-challenge}") final String appName) {
        return registry -> registry.observationConfig()
                .observationPredicate(observationPredicate())
                .observationFilter(context -> {
                    context.addLowCardinalityKeyValue(KeyValue.of("application", appName));
                    return context;
                });
    }

    private ObservationPredicate observationPredicate() {
        return (name, context) -> true;
    }
}
