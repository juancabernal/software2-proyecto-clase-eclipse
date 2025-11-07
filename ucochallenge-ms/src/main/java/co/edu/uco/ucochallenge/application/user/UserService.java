package co.edu.uco.ucochallenge.application.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.entity.CityEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.entity.IdTypeEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.entity.UserEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.jpa.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
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
        final IdTypeEntity idType = Optional.ofNullable(user.getIdType())
                .map(this::normalizeIdType)
                .orElseGet(() -> new IdTypeEntity.Builder().build());
        final CityEntity homeCity = Optional.ofNullable(user.getHomeCity())
                .map(this::normalizeCity)
                .orElseGet(() -> new CityEntity.Builder().build());

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
                .mobileNumber(sanitize(user.getMobileNumber()));

        if (user.isEmailConfirmed()) {
            builder.emailConfirmed(true);
        }
        if (user.isMobileNumberConfirmed()) {
            builder.mobileNumberConfirmed(true);
        }

        return builder.build();
    }

    private IdTypeEntity normalizeIdType(final IdTypeEntity source) {
        return new IdTypeEntity.Builder()
                .id(source.getId())
                .name(sanitize(source.getName()))
                .build();
    }

    private CityEntity normalizeCity(final CityEntity source) {
        final CityEntity.Builder builder = new CityEntity.Builder()
                .id(source.getId())
                .name(sanitize(source.getName()));

        if (source.getState() != null) {
            builder.state(source.getState());
        }

        return builder.build();
    }

    private String sanitize(final String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .orElse("");
    }
}
