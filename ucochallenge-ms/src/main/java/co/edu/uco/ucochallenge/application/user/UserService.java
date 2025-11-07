package co.edu.uco.ucochallenge.application.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.entity.CityEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.entity.IdTypeEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.entity.UserEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.jpa.UserRepository;
import co.edu.uco.ucochallenge.crosscutting.legacy.helper.UUIDHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

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

        final boolean emailConfirmed = resolveConfirmation(user.isEmailConfirmed(),
                user.isEmailConfirmedIsDefaultValue());
        final boolean mobileNumberConfirmed = resolveConfirmation(user.isMobileNumberConfirmed(),
                user.isMobileNumberConfirmedIsDefaultValue());

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
                .emailConfirmed(emailConfirmed)
                .mobileNumberConfirmed(mobileNumberConfirmed);

        return builder.build();
    }

    private IdTypeEntity resolveIdType(final IdTypeEntity source) {
        if (source == null || !hasValidIdentifier(source.getId())) {
            return null;
        }
        try {
            return entityManager.getReference(IdTypeEntity.class, source.getId());
        } catch (EntityNotFoundException ex) {
            throw new IllegalArgumentException("Tipo de identificación inválido");
        }
    }

    private CityEntity resolveCity(final CityEntity source) {
        if (source == null || !hasValidIdentifier(source.getId())) {
            return null;
        }
        try {
            return entityManager.getReference(CityEntity.class, source.getId());
        } catch (EntityNotFoundException ex) {
            throw new IllegalArgumentException("Ciudad de residencia inválida");
        }
    }

    private boolean hasValidIdentifier(final UUID id) {
        return id != null && !UUIDHelper.getDefault().equals(id);
    }

    private boolean resolveConfirmation(final boolean value, final boolean isDefault) {
        return isDefault ? false : value;
    }

    private String sanitize(final String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .orElse("");
    }
}
