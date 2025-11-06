package co.edu.uco.ucochallenge.application.user.registration.validator;

import java.util.UUID;
import java.util.function.Supplier;

import co.edu.uco.ucochallenge.application.user.registration.port.UserRegistrationNotificationPort;
import co.edu.uco.ucochallenge.application.user.registration.port.UserRegistrationRepositoryPort;
import co.edu.uco.ucochallenge.crosscuting.notification.Notification;
import co.edu.uco.ucochallenge.domain.specification.Specification;
import co.edu.uco.ucochallenge.domain.user.registration.model.UserRegistrationDomainModel;
import co.edu.uco.ucochallenge.domain.user.registration.specification.UserRegistrationAvailableIdSpecification;
import co.edu.uco.ucochallenge.domain.user.registration.specification.UserRegistrationUniqueEmailSpecification;
import co.edu.uco.ucochallenge.domain.user.registration.specification.UserRegistrationUniqueIdentificationSpecification;
import co.edu.uco.ucochallenge.domain.user.registration.specification.UserRegistrationUniqueMobileNumberSpecification;

public class UserRegistrationDomainValidator {

        private final UserRegistrationRepositoryPort repositoryPort;
        private final UserRegistrationNotificationPort notificationPort;
        private final Supplier<UUID> idGenerator;

        public UserRegistrationDomainValidator(final UserRegistrationRepositoryPort repositoryPort,
                        final UserRegistrationNotificationPort notificationPort,
                        final Supplier<UUID> idGenerator) {
                this.repositoryPort = repositoryPort;
                this.notificationPort = notificationPort;
                this.idGenerator = idGenerator;
        }

        public Notification validate(final UserRegistrationDomainModel domain, final String executorIdentifier) {
                final var notification = Notification.create();
                notification.merge(domain.validate());

                final Specification<UserRegistrationDomainModel> availableIdSpec = new UserRegistrationAvailableIdSpecification(
                                repositoryPort::existsById, idGenerator);

                final Specification<UserRegistrationDomainModel> uniqueIdentificationSpec = new UserRegistrationUniqueIdentificationSpecification(
                                notification,
                                repositoryPort::findByIdentification,
                                notificationPort::notifyAdministrator,
                                notificationPort::notifyExecutor,
                                executorIdentifier);

                final Specification<UserRegistrationDomainModel> uniqueEmailSpec = new UserRegistrationUniqueEmailSpecification(
                                notification,
                                repositoryPort::findByEmail,
                                notificationPort::notifyEmailOwner,
                                notificationPort::notifyExecutor,
                                executorIdentifier);

                final Specification<UserRegistrationDomainModel> uniqueMobileSpec = new UserRegistrationUniqueMobileNumberSpecification(
                                notification,
                                repositoryPort::findByMobileNumber,
                                notificationPort::notifyMobileOwner,
                                notificationPort::notifyExecutor,
                                executorIdentifier);

                availableIdSpec.and(uniqueIdentificationSpec)
                                .and(uniqueEmailSpec)
                                .and(uniqueMobileSpec)
                                .isSatisfiedBy(domain);

                return notification;
        }
}
