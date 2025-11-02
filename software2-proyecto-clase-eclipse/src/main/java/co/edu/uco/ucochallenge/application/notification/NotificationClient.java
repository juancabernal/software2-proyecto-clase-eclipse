package co.edu.uco.ucochallenge.application.notification;

import java.net.URI;
import java.time.format.DateTimeFormatter;
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

import co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationRequest.NotificationEvent;
import co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationRequest.Person;
import co.edu.uco.ucochallenge.application.notification.DuplicateRegistrationNotificationRequest.Recipient;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;

@Component
public class NotificationClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationClient.class);

    private final RestTemplate restTemplate;
    private final NotificationApiProperties properties;

    public NotificationClient(final RestTemplate restTemplate, final NotificationApiProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

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

        final Map<String, Object> payload = buildPayload(request, recipients);
        try {
            final URI target = buildTargetUri(baseUrl, properties.getDuplicatePath());
            final HttpHeaders headers = buildHeaders();
            final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            LOGGER.info("Sending notification '{}' to {}", request.type(), target);
            LOGGER.debug("Notification payload: {}", payload);

            final ResponseEntity<String> response = restTemplate.postForEntity(target, entity, String.class);
            LOGGER.info("Notification dispatched successfully. Status: {}", response.getStatusCode());
        } catch (final RestClientException exception) {
            LOGGER.error("Unable to dispatch notification '{}'", request.type(), exception);
        }
    }

    private Map<String, Object> buildPayload(final DuplicateRegistrationNotificationRequest request,
            final List<Map<String, Object>> recipients) {
        final Map<String, Object> payload = new HashMap<>();

        payload.put("type", request.notificationType());
        payload.put("subject", request.subject());
        payload.put("message", request.message());
        payload.put("detectedAt", DateTimeFormatter.ISO_INSTANT.format(request.detectedAt()));

        final NotificationEvent event = request.type();
        payload.put("eventName", event.eventName());
        payload.put("reasonCode", event.reasonCode());

        final Map<String, Object> to = convertPerson(request.attemptedUser());
        payload.put("to", to);

        final Map<String, Object> metadata = new HashMap<>();
        metadata.put("existingUser", convertPerson(request.existingUser()));
        payload.put("metadata", metadata);

        payload.put("recipients", recipients);

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("subject", request.subject());
        parameters.put("message", request.message());
        parameters.put("detectedAt", DateTimeFormatter.ISO_INSTANT.format(request.detectedAt()));
        payload.put("parameters", parameters);

        payload.put("forceChannels", resolveChannels(request));
        return payload;
    }

    private List<String> resolveChannels(final DuplicateRegistrationNotificationRequest request) {
        final List<String> channels = new ArrayList<>();
        final String forceChannel = TextHelper.getDefaultWithTrim(request.forceChannel());
        if (!TextHelper.isEmpty(forceChannel)) {
            channels.add(forceChannel);
            return channels;
        }

        final Person attempted = request.attemptedUser();
        if (!TextHelper.isEmpty(attempted.email())) {
            channels.add("EMAIL");
        }
        if (!TextHelper.isEmpty(attempted.mobileNumber())) {
            channels.add("SMS");
        }
        return channels;
    }

    private Map<String, Object> convertPerson(final Person person) {
        final Map<String, Object> map = new HashMap<>();
        if (person == null) {
            return map;
        }
        if (!TextHelper.isEmpty(person.name())) {
            map.put("name", person.name());
        }
        if (!TextHelper.isEmpty(person.email())) {
            map.put("email", person.email());
        }
        if (!TextHelper.isEmpty(person.mobileNumber())) {
            map.put("number", person.mobileNumber());
        }
        return map;
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
            final String key = (TextHelper.getDefaultWithTrim(recipient.email()) + "|" +
                    TextHelper.getDefaultWithTrim(recipient.mobileNumber())).toLowerCase();
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

    private HttpHeaders buildHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        final String apiKey = TextHelper.getDefaultWithTrim(properties.getApiKey());
        final String apiSecret = TextHelper.getDefaultWithTrim(properties.getApiSecret());
        if (!TextHelper.isEmpty(apiKey) && !TextHelper.isEmpty(apiSecret)) {
            headers.setBasicAuth(apiKey, apiSecret);
        } else {
            LOGGER.warn("NotificationAPI credentials are not fully configured. Request will be sent without authentication.");
        }
        return headers;
    }

    private URI buildTargetUri(final String baseUrl, final String path) {
        final String sanitizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        final String sanitizedPath = path.startsWith("/") ? path : "/" + path;
        final String endpoint = sanitizedBase + sanitizedPath;
        LOGGER.debug("Resolved NotificationAPI endpoint: {}", endpoint);
        return URI.create(endpoint);
    }
}