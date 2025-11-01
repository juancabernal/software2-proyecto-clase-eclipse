package co.edu.uco.ucochallenge.infrastructure.secondary.ai;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import co.edu.uco.ucochallenge.crosscuting.security.SanitizationHelper;
import co.edu.uco.ucochallenge.domain.ai.port.out.UserIntelligencePort;
import co.edu.uco.ucochallenge.domain.user.model.User;

final class OpenAiUserIntelligenceAdapter implements UserIntelligencePort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiUserIntelligenceAdapter.class);
    private final RestClient client;
    private final AiProperties.OpenAi properties;

    OpenAiUserIntelligenceAdapter(final AiProperties properties, final RestClient.Builder restClientBuilder) {
        this.properties = properties.getOpenai();
        this.client = restClientBuilder.clone()
                .baseUrl(this.properties.getEndpoint())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.properties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public void publishUserRegistrationInsight(final User user) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            log.warn("OpenAI integration enabled but API key is missing. Skipping insight generation.");
            return;
        }

        final String prompt = buildPrompt(user);
        try {
            client.post()
                    .body(buildRequestBody(prompt))
                    .retrieve()
                    .toBodilessEntity();
            log.info("AI insight requested for user {} using model {}", user.id(), properties.getModel());
        } catch (Exception exception) {
            log.warn("Failed to generate AI insight: {}", exception.getMessage());
        }
    }

    private Map<String, Object> buildRequestBody(final String prompt) {
        return Map.of(
                "model", properties.getModel(),
                "input", prompt,
                "temperature", properties.getTemperature(),
                "max_output_tokens", 200);
    }

    private String buildPrompt(final User user) {
        final String sanitizedName = SanitizationHelper.sanitize(user.firstName() + " " + user.firstSurname());
        return "Genera una recomendación breve (máximo 50 palabras) para el equipo de éxito del cliente "
                + "respecto al onboarding del usuario llamado '" + sanitizedName
                + "'. Usa un tono proactivo y sin datos sensibles.";
    }
}
