package co.edu.uco.ucochallenge.domain.notification.port.out;

import co.edu.uco.ucochallenge.domain.notification.model.NotificationMessage;

public interface NotificationSenderPort {

    void send(NotificationMessage message);
}
