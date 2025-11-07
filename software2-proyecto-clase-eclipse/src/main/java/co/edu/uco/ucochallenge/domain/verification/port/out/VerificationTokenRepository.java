package co.edu.uco.ucochallenge.domain.verification.port.out;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import co.edu.uco.ucochallenge.domain.verification.model.VerificationToken;

public interface VerificationTokenRepository {

    VerificationToken save(VerificationToken token);

    Optional<VerificationToken> findById(UUID id);

    Optional<VerificationToken> findByContact(String contact);

    Optional<VerificationToken> findByContactAndCode(String contact, String code);

    void deleteByContact(String contact);

    void deleteById(UUID id);

    int deleteExpired(LocalDateTime reference);
}
