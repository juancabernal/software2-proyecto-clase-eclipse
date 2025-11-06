package co.edu.uco.ucochallenge.user.findusers.application.usecase.domain.validation;

import co.edu.uco.ucochallenge.crosscuting.notification.Notification;
import co.edu.uco.ucochallenge.domain.user.search.model.UserSearchFilterDomainModel;

public class FindUsersByFilterDomainValidator {

        public Notification validate(final UserSearchFilterDomainModel domain) {
                final var notification = Notification.create();
                notification.merge(domain.validate());
                return notification;
        }
}
