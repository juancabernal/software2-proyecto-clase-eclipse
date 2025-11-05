package co.edu.uco.ucochallenge.domain.notification.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;

import java.util.Map;

public record NotificationMessage(
        NotificationEvent type,
        String subject,
        String message,
        Instant detectedAt,
        Person existingUser,
        Person attemptedUser,
        List<Recipient> recipients,
        String notificationType,
        String forceChannel,
        Map<String, Object> extraData) {


    public NotificationMessage {
        type = type == null ? NotificationEvent.DUPLICATE_EMAIL : type;
        subject = TextHelper.getDefaultWithTrim(subject);
        message = TextHelper.getDefault(message);
        detectedAt = detectedAt == null ? Instant.now() : detectedAt;
        existingUser = existingUser == null ? Person.empty() : existingUser;
        attemptedUser = attemptedUser == null ? Person.empty() : attemptedUser;
        recipients = recipients == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(recipients));
        notificationType = TextHelper.isEmpty(notificationType) ? "duplicate_alert" : notificationType;
        forceChannel = TextHelper.getDefaultWithTrim(forceChannel);
        extraData = extraData == null ? Map.of() : Map.copyOf(extraData);

    }

    public NotificationMessage(final NotificationEvent type,
            final String subject,
            final String message,
            final Instant detectedAt,
            final Person existingUser,
            final Person attemptedUser,
            final List<Recipient> recipients) {
        this(type, subject, message, detectedAt, existingUser, attemptedUser, recipients, "duplicate_alert", "");
    }
    
    public NotificationMessage(final NotificationEvent type,
            final String subject,
            final String message,
            final Instant detectedAt,
            final Person existingUser,
            final Person attemptedUser,
            final List<Recipient> recipients,
            final String notificationType,
            final String forceChannel) {
        this(type, subject, message, detectedAt, existingUser, attemptedUser, recipients, notificationType, forceChannel, Map.of());
    }


    public enum NotificationEvent {
        DUPLICATE_EMAIL("REGISTER_DUPLICATE_EMAIL", "EMAIL"),
        DUPLICATE_MOBILE("REGISTER_DUPLICATE_MOBILE", "MOBILE"),
        EMAIL_CONFIRMATION("REGISTER_EMAIL_CONFIRMATION", "EMAIL"),
        MOBILE_CONFIRMATION("REGISTER_MOBILE_CONFIRMATION", "MOBILE");

        private final String eventName;
        private final String reasonCode;

        NotificationEvent(final String eventName, final String reasonCode) {
            this.eventName = eventName;
            this.reasonCode = reasonCode;
        }

        public String eventName() {
            return eventName;
        }

        public String reasonCode() {
            return reasonCode;
        }
    }

    public record Person(String name, String email, String mobileNumber) {

        public Person {
            name = TextHelper.getDefaultWithTrim(name);
            email = TextHelper.getDefaultWithTrim(email);
            mobileNumber = TextHelper.getDefaultWithTrim(mobileNumber);
        }

        public static Person empty() {
            return new Person(TextHelper.getDefault(), TextHelper.getDefault(), TextHelper.getDefault());
        }

        public boolean hasContactInfo() {
            return !TextHelper.isEmpty(email) || !TextHelper.isEmpty(mobileNumber);
        }
    }

    public record Recipient(String role, String name, String email, String mobileNumber) {

        public Recipient {
            role = TextHelper.getDefaultWithTrim(role);
            name = TextHelper.getDefaultWithTrim(name);
            email = TextHelper.getDefaultWithTrim(email);
            mobileNumber = TextHelper.getDefaultWithTrim(mobileNumber);
        }

        public boolean hasContactInfo() {
            return !TextHelper.isEmpty(email) || !TextHelper.isEmpty(mobileNumber);
        }
    }
}
