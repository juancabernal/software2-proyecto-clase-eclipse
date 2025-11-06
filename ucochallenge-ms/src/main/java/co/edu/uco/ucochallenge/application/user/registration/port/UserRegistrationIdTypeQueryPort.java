package co.edu.uco.ucochallenge.application.user.registration.port;

import java.util.Optional;
import java.util.UUID;

public interface UserRegistrationIdTypeQueryPort {

        boolean existsById(UUID idType);

        Optional<UUID> findIdByName(String idTypeName);
}
