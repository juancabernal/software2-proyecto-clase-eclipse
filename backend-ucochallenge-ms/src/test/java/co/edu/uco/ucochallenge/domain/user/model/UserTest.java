package co.edu.uco.ucochallenge.domain.user.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.domain.user.model.User;

class UserTest {

        @Test
        @DisplayName("Should normalize and keep email in lowercase")
        void shouldCreateUserSuccessfully() {
                final User user = new User(
                                null,
                                UUID.randomUUID(),
                                "123456",
                                "Juan",
                                "Carlos",
                                "Pérez",
                                "Gómez",
                                UUID.randomUUID(),
                                "TEST@EMAIL.COM",
                                "3001234567",
                                false,
                                false);

                assertEquals("test@email.com", user.email());
        }

        @Test
        @DisplayName("Should fail when email format is invalid")
        void shouldFailWhenEmailIsInvalid() {
                assertThrows(DomainException.class, () -> new User(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "123456",
                                "Juan",
                                "Carlos",
                                "Pérez",
                                "Gómez",
                                UUID.randomUUID(),
                                "invalid-email",
                                "3001234567",
                                false,
                                false));
        }
}
