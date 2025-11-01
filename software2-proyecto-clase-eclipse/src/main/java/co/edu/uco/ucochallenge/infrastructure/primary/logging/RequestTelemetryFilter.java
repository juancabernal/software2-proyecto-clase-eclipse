package co.edu.uco.ucochallenge.infrastructure.primary.logging;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import co.edu.uco.ucochallenge.crosscuting.security.SanitizationHelper;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

@Component
public class RequestTelemetryFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestTelemetryFilter.class);
    private final ObservationRegistry observationRegistry;

    public RequestTelemetryFilter(final ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
        final long start = System.currentTimeMillis();
        final String sanitizedPath = SanitizationHelper.sanitize(request.getRequestURI());
        final Observation observation = Observation.start("http.server.request.telemetry", observationRegistry)
                .contextualName(request.getMethod() + " " + sanitizedPath)
                .lowCardinalityKeyValue("http.method", request.getMethod())
                .lowCardinalityKeyValue("http.path", sanitizedPath);

        try (Observation.Scope scope = observation.openScope()) {
            filterChain.doFilter(request, response);
            observation.lowCardinalityKeyValue("http.status", String.valueOf(response.getStatus()));
        } catch (Exception exception) {
            observation.error(exception);
            throw exception;
        } finally {
            final long duration = System.currentTimeMillis() - start;
            observation.lowCardinalityKeyValue("http.duration", String.valueOf(duration));
            observation.stop();
            log.info("HTTP {} {} -> {} ({} ms)", request.getMethod(), sanitizedPath, response.getStatus(), duration);
        }
    }
}
