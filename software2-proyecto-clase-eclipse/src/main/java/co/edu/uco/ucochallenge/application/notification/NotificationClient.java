package co.edu.uco.ucochallenge.application.notification;

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

import co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationRequest.Person;
import co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationRequest.Recipient;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.domain.secret.port.SecretProviderPort;

@Component
public class NotificationClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationClient.class);

    private final RestTemplate restTemplate;
    private final NotificationApiProperties properties;
    private final SecretProviderPort secretProvider;

    public NotificationClient(final RestTemplate restTemplate,
                              final NotificationApiProperties properties,
                              final SecretProviderPort secretProvider) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.secretProvider = secretProvider;
    }

    // ============================================================
    // MÉTODO PRINCIPAL (MODIFICADO)
    // ============================================================

    public void sendNotification(final DuplicateRegistrationNotificationRequest request) {
        final String baseUrl = TextHelper.getDefaultWithTrim(properties.getBaseUrl());
        if (TextHelper.isEmpty(baseUrl)) {
            LOGGER.warn("Skipping notification dispatch: NotificationAPI base URL is not configured.");
            return;
        }

        final List<Map<String, Object>> recipients = convertRecipients(request.recipients());
        if (recipients.isEmpty()) {
            LOGGER.warn("Skipping notification dispatch: no recipients available for event {}.", request.type());
            return;
        }

        try {
            final URI target = buildTargetUri(baseUrl, properties.getDuplicatePath());
            final HttpHeaders headers = buildHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // ✅ Construimos el payload
            final Map<String, Object> payload = buildPayload(request);

            // ✅ Serialización explícita con ObjectMapper para evitar diferencias entre entornos
            final ObjectMapper mapper = new ObjectMapper();
            final String jsonBody = mapper.writeValueAsString(payload);

            LOGGER.info("🚀 Sending notification '{}' to {}", request.type(), target);
            LOGGER.info("📤 JSON Body sent to NotificationAPI: {}", jsonBody);

            // ✅ Envío garantizado limpio y sin encoding extra
            final HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            final ResponseEntity<String> response = restTemplate.postForEntity(target, entity, String.class);

            LOGGER.info("✅ Notification dispatched successfully. Status: {}", response.getStatusCode());
            LOGGER.debug("Response body: {}", response.getBody());

        } catch (final RestClientException exception) {
            LOGGER.error("❌ Unable to dispatch notification '{}'", request.type(), exception);
        } catch (final Exception ex) {
            LOGGER.error("❌ Error serializing or sending notification '{}': {}", request.type(), ex.getMessage(), ex);
        }
    }

    // ============================================================
    // HEADERS Y AUTENTICACIÓN
    // ============================================================

    private HttpHeaders buildHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // 🔐 Credenciales desde Key Vault
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

    // ============================================================
    // CONSTRUCCIÓN DE ENDPOINT
    // ============================================================

    private URI buildTargetUri(final String baseUrl, final String path) {
        final String clientId = TextHelper.getDefaultWithTrim(secretProvider.getSecret("cliente-id"));
        final String sanitizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        final String sanitizedPath = path.startsWith("/") ? path : "/" + path;
        final String endpoint = sanitizedBase + "/" + clientId + sanitizedPath;
        LOGGER.debug("Resolved NotificationAPI endpoint: {}", endpoint);
        return URI.create(endpoint);
    }

    // ============================================================
    // CONSTRUCCIÓN DEL PAYLOAD
    // ============================================================

    private Map<String, Object> buildPayload(final DuplicateRegistrationNotificationRequest request) {
        final Map<String, Object> payload = new HashMap<>();

        // 🔹 Identificador de la notificación (como está en tu panel NotificationAPI)
        payload.put("notificationId", request.notificationType()); // Ej: "duplicate_alert"

        // 🔹 Construir objeto 'user' con email o número
        final Map<String, Object> user = new HashMap<>();

        if (request.attemptedUser() != null) {
            if (!TextHelper.isEmpty(request.attemptedUser().email())) {
                user.put("email", request.attemptedUser().email());
                user.put("id", request.attemptedUser().email());
            }
            if (!TextHelper.isEmpty(request.attemptedUser().mobileNumber())) {
                String number = request.attemptedUser().mobileNumber();
                if (!number.startsWith("+")) {
                    number = "+57" + number;
                }
                user.put("number", number);
                // Si no tiene id por email, usa el número como id
                if (!user.containsKey("id")) {
                    user.put("id", number);
                }
            }
        }

        payload.put("user", user);

        // 🔹 Plantilla predeterminada
        payload.put("templateId", "predeterminado");

        // 🔹 Log de payload
        LOGGER.info("📤 Payload NotificationAPI: {}", payload);

        return payload;
    }

    // ============================================================
    // AUXILIARES
    // ============================================================

    private List<String> resolveChannels(final DuplicateRegistrationNotificationRequest request) {
        final List<String> channels = new ArrayList<>();
        final String forceChannel = TextHelper.getDefaultWithTrim(request.forceChannel());
        if (!TextHelper.isEmpty(forceChannel)) {
            channels.add(forceChannel);
            return channels;
        }

        final Person attempted = request.attemptedUser();
        if (attempted != null) {
            if (!TextHelper.isEmpty(attempted.email())) channels.add("EMAIL");
            if (!TextHelper.isEmpty(attempted.mobileNumber())) channels.add("SMS");
        }
        return channels;
    }

    private Map<String, Object> convertPerson(final Person person) {
        final Map<String, Object> map = new HashMap<>();
        if (person == null) return map;
        if (!TextHelper.isEmpty(person.name())) map.put("name", person.name());
        if (!TextHelper.isEmpty(person.email())) map.put("email", person.email());
        if (!TextHelper.isEmpty(person.mobileNumber())) map.put("number", person.mobileNumber());
        return map;
    }

    private List<Map<String, Object>> convertRecipients(final List<Recipient> recipients) {
        final List<Map<String, Object>> result = new ArrayList<>();
        final Set<String> dedupe = new HashSet<>();
        if (recipients == null) return result;
        for (final Recipient recipient : recipients) {
            if (recipient == null || !recipient.hasContactInfo()) continue;
            final String key = (TextHelper.getDefaultWithTrim(recipient.email()) + "|" +
                    TextHelper.getDefaultWithTrim(recipient.mobileNumber())).toLowerCase();
            if (!dedupe.add(key)) continue;
            result.add(convertRecipient(recipient));
        }
        return result;
    }

    private Map<String, Object> convertRecipient(final Recipient recipient) {
        final Map<String, Object> map = new HashMap<>();
        if (!TextHelper.isEmpty(recipient.role())) map.put("role", recipient.role());
        if (!TextHelper.isEmpty(recipient.name())) map.put("name", recipient.name());
        if (!TextHelper.isEmpty(recipient.email())) map.put("email", recipient.email());
        if (!TextHelper.isEmpty(recipient.mobileNumber())) map.put("mobileNumber", recipient.mobileNumber());

        final List<String> channels = new ArrayList<>();
        if (!TextHelper.isEmpty(recipient.email())) channels.add("EMAIL");
        if (!TextHelper.isEmpty(recipient.mobileNumber())) channels.add("SMS");
        map.put("channels", channels);
        return map;
    }
}
