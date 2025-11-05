package co.edu.uco.ucochallenge.infrastructure.secondary.notification;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage;
import co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage.Recipient;
import co.edu.uco.ucochallenge.domain.notification.port.out.NotificationSenderPort;
import co.edu.uco.ucochallenge.domain.secret.port.SecretProviderPort;
import co.edu.uco.ucochallenge.infrastructure.secondary.notification.config.NotificationApiProperties;

@Component
public class NotificationApiAdapter implements NotificationSenderPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationApiAdapter.class);

    private final RestTemplate restTemplate;
    private final NotificationApiProperties properties;
    private final SecretProviderPort secretProvider;

    public NotificationApiAdapter(final RestTemplate restTemplate,
            final NotificationApiProperties properties,
            final SecretProviderPort secretProvider) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.secretProvider = secretProvider;
    }

    @Override
    public void send(final NotificationMessage message) {
        final String baseUrl = TextHelper.getDefaultWithTrim(properties.getBaseUrl());
        if (TextHelper.isEmpty(baseUrl)) {
            LOGGER.warn("Skipping notification dispatch: NotificationAPI base URL is not configured.");
            return;
        }

        final List<Map<String, Object>> recipients = convertRecipients(message.recipients());
        if (recipients.isEmpty()) {
            LOGGER.warn("Skipping notification dispatch: no recipients available for event {}.", message.type());
            return;
        }

        try {
            final URI target = buildTargetUri(baseUrl, properties.getDuplicatePath());
            final HttpHeaders headers = buildHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            final Map<String, Object> payload = buildPayload(message);

            final ObjectMapper mapper = new ObjectMapper();
            final String jsonBody = mapper.writeValueAsString(payload);

            LOGGER.info("🚀 Sending notification '{}' to {}", message.type(), target);
            LOGGER.info("📤 JSON Body sent to NotificationAPI: {}", jsonBody);

            final HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            final ResponseEntity<String> response = restTemplate.postForEntity(target, entity, String.class);

            LOGGER.info("✅ Notification dispatched successfully. Status: {}", response.getStatusCode());
            LOGGER.debug("Response body: {}", response.getBody());

        } catch (final RestClientException exception) {
            LOGGER.error("❌ Unable to dispatch notification '{}'", message.type(), exception);
        } catch (final Exception ex) {
            LOGGER.error("❌ Error serializing or sending notification '{}': {}", message.type(), ex.getMessage(), ex);
        }
    }

    private HttpHeaders buildHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        final String clientId = TextHelper.getDefaultWithTrim(secretProvider.getSecret("cliente-id"));
        final String clientSecret = TextHelper.getDefaultWithTrim(secretProvider.getSecret("cliente-secret-id"));

        if (!TextHelper.isEmpty(clientId) && !TextHelper.isEmpty(clientSecret)) {
            headers.setBasicAuth(clientId, clientSecret);
            LOGGER.debug("NotificationAPI Basic Auth configured with client ID '{}'.", clientId);
        } else {
            LOGGER.warn("NotificationAPI credentials are missing. The request may fail with 401 Unauthorized.");
        }

        return headers;
    }

    private URI buildTargetUri(final String baseUrl, final String path) {
        final String clientId = TextHelper.getDefaultWithTrim(secretProvider.getSecret("cliente-id"));
        final String sanitizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        final String sanitizedPath = path.startsWith("/") ? path : "/" + path;
        final String endpoint = sanitizedBase + "/" + clientId + sanitizedPath;
        LOGGER.debug("Resolved NotificationAPI endpoint: {}", endpoint);
        return URI.create(endpoint);
    }

    private Map<String, Object> buildPayload(final NotificationMessage message) {
        final Map<String, Object> payload = new HashMap<>();

        payload.put("notificationId", message.notificationType());
        payload.put("templateId", "predeterminado");

        // Usuario destino
        final Map<String, Object> user = new HashMap<>();
        if (message.attemptedUser() != null) {
            if (!TextHelper.isEmpty(message.attemptedUser().email())) {
                user.put("email", message.attemptedUser().email());
                user.put("id", message.attemptedUser().email());
            }
            if (!TextHelper.isEmpty(message.attemptedUser().mobileNumber())) {
                String number = message.attemptedUser().mobileNumber();
                if (!number.startsWith("+")) {
                    number = "+57" + number;
                }
                user.put("number", number);
                if (!user.containsKey("id")) {
                    user.put("id", number);
                }
            }
        }
        payload.put("user", user);

        // ⚡ Aquí está la clave: usar "parameters" con el nombre exacto del tag del template
        final Map<String, Object> parameters = new HashMap<>();
        if (message.extraData() != null && message.extraData().get("code") != null) {
            parameters.put("verificationCode", message.extraData().get("code")); // nombre exacto del tag en tu plantilla
        }

        payload.put("parameters", parameters);

        LOGGER.info("📤 Payload NotificationAPI (formato final): {}", payload);
        return payload;
    }



    private List<Map<String, Object>> convertRecipients(final List<Recipient> recipients) {
        final List<Map<String, Object>> result = new ArrayList<>();
        final Set<String> dedupe = new HashSet<>();
        if (recipients == null) {
            return result;
        }
        for (final Recipient recipient : recipients) {
            if (recipient == null || !recipient.hasContactInfo()) {
                continue;
            }
            final String key = (TextHelper.getDefaultWithTrim(recipient.email()) + "|"
                    + TextHelper.getDefaultWithTrim(recipient.mobileNumber())).toLowerCase();
            if (!dedupe.add(key)) {
                continue;
            }
            result.add(convertRecipient(recipient));
        }
        return result;
    }

    private Map<String, Object> convertRecipient(final Recipient recipient) {
        final Map<String, Object> map = new HashMap<>();
        if (!TextHelper.isEmpty(recipient.role())) {
            map.put("role", recipient.role());
        }
        if (!TextHelper.isEmpty(recipient.name())) {
            map.put("name", recipient.name());
        }
        if (!TextHelper.isEmpty(recipient.email())) {
            map.put("email", recipient.email());
        }
        if (!TextHelper.isEmpty(recipient.mobileNumber())) {
            map.put("mobileNumber", recipient.mobileNumber());
        }

        final List<String> channels = new ArrayList<>();
        if (!TextHelper.isEmpty(recipient.email())) {
            channels.add("EMAIL");
        }
        if (!TextHelper.isEmpty(recipient.mobileNumber())) {
            channels.add("SMS");
        }
        map.put("channels", channels);
        return map;
    }
}
