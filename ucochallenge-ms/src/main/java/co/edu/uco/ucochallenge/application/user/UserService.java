package co.edu.uco.ucochallenge.application.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.entity.CityEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.entity.IdTypeEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.entity.UserEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.jpa.UserRepository;
import jakarta.persistence.EntityManager;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public UserService(final UserRepository userRepository, final EntityManager entityManager) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public UserEntity registerUser(final UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("Datos de usuario obligatorios");
        }

        final String email = sanitize(user.getEmail());
        if (email.isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email ya registrado");
        }

        final UserEntity normalizedUser = buildNormalizedUser(user, email);
        return userRepository.save(normalizedUser);
    }

    private UserEntity buildNormalizedUser(final UserEntity user, final String email) {
        final IdTypeEntity idType = resolveIdType(user.getIdType());
        final CityEntity homeCity = resolveCity(user.getHomeCity());

        final UserEntity.Builder builder = new UserEntity.Builder()
                .id(UUID.randomUUID())
                .idType(idType)
                .idNumber(sanitize(user.getIdNumber()))
                .firstName(sanitize(user.getFirstName()))
                .secondName(sanitize(user.getSecondName()))
                .firstSurname(sanitize(user.getFirstSurname()))
                .secondSurname(sanitize(user.getSecondSurname()))
                .homeCity(homeCity)
                .email(email)
                .mobileNumber(sanitize(user.getMobileNumber()))
                .emailConfirmed(user.isEmailConfirmed())
                .mobileNumberConfirmed(user.isMobileNumberConfirmed());

        return builder.build();
    }

    private IdTypeEntity resolveIdType(final IdTypeEntity source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        return entityManager.getReference(IdTypeEntity.class, source.getId());
    }

    private CityEntity resolveCity(final CityEntity source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        return entityManager.getReference(CityEntity.class, source.getId());
    }

    private String sanitize(final String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .orElse("");
    }
}
