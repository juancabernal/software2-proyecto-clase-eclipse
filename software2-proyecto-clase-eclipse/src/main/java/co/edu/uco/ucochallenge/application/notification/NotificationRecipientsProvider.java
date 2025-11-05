package co.edu.uco.ucochallenge.application.notification;

import java.util.List;

import co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage.Recipient;

public interface NotificationRecipientsProvider {

    List<Recipient> getAdminRecipients();

    List<Recipient> getUserRecipients();
}
