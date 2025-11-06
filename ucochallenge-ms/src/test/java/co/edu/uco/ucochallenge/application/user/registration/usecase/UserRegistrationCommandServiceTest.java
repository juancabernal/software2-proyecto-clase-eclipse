package co.edu.uco.ucochallenge.application.user.registration.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.uco.ucochallenge.application.user.registration.port.UserRegistrationContactConfirmationPort;
import co.edu.uco.ucochallenge.application.user.registration.port.UserRegistrationIdTypeQueryPort;
import co.edu.uco.ucochallenge.application.user.registration.port.UserRegistrationLocationQueryPort;
import co.edu.uco.ucochallenge.application.user.registration.port.UserRegistrationNotificationPort;
import co.edu.uco.ucochallenge.application.user.registration.port.UserRegistrationRepositoryPort;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainValidationException;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import co.edu.uco.ucochallenge.domain.user.registration.model.UserRegistrationDomainModel;

@ExtendWith(MockitoExtension.class)
class UserRegistrationCommandServiceTest {

        @Mock
        private UserRegistrationRepositoryPort repositoryPort;
        @Mock
        private UserRegistrationNotificationPort notificationPort;
        @Mock
        private UserRegistrationContactConfirmationPort contactConfirmationPort;
        @Mock
        private UserRegistrationIdTypeQueryPort idTypeQueryPort;
        @Mock
        private UserRegistrationLocationQueryPort locationQueryPort;
        @Mock
        private Supplier<UUID> idGenerator;

        @InjectMocks
        private UserRegistrationCommandService useCase;

        @Test
        void shouldThrowDomainValidationExceptionWhenDomainValidationFails() {
                var domain = UserRegistrationDomainModel.builder()
                                .id(UUID.randomUUID())
                                .idType(UUIDHelper.getDefault())
                                .idNumber("")
                                .firstName("")
                                .firstSurname("")
                                .homeCity(UUID.randomUUID())
                                .email("")
                                .mobileNumber("")
                                .build();

                assertThrows(DomainValidationException.class, () -> useCase.execute(domain));

                verify(repositoryPort, never()).save(domain);
                verify(contactConfirmationPort, never()).confirmEmail(ArgumentMatchers.anyString());
        }
}
