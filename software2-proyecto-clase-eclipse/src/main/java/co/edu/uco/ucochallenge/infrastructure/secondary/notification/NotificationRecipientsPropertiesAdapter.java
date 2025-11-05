package co.edu.uco.ucochallenge.infrastructure.secondary.notification;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.application.notification.NotificationRecipientsProvider;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage.Recipient;
import co.edu.uco.ucochallenge.infrastructure.secondary.notification.config.NotificationApiProperties;
import co.edu.uco.ucochallenge.infrastructure.secondary.notification.config.NotificationApiProperties.RecipientProperties;

@Component
public class NotificationRecipientsPropertiesAdapter implements NotificationRecipientsProvider {

    private final NotificationApiProperties properties;

    public NotificationRecipientsPropertiesAdapter(final NotificationApiProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<Recipient> getAdminRecipients() {
        return convert(properties.getAdminRecipients());
    }

    @Override
    public List<Recipient> getUserRecipients() {
        return convert(properties.getUserRecipients());
    }

    private List<Recipient> convert(final List<RecipientProperties> source) {
        return source.stream()
                .filter(Objects::nonNull)
                .filter(RecipientProperties::hasContactInfo)
                .map(this::map)
                .collect(Collectors.toList());
    }

    private Recipient map(final RecipientProperties properties) {
        final String name = TextHelper.getDefaultWithTrim(properties.getName());
        final String email = TextHelper.getDefaultWithTrim(properties.getEmail());
        final String mobileNumber = TextHelper.getDefaultWithTrim(properties.getMobileNumber());
        return new Recipient(null, name, email, mobileNumber);
    }
}
