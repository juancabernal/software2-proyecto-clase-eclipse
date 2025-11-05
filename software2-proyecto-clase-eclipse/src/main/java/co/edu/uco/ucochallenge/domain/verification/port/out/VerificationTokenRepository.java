package co.edu.uco.ucochallenge.domain.verification.port.out;

import java.time.LocalDateTime;
import java.util.Optional;

import co.edu.uco.ucochallenge.domain.verification.model.VerificationToken;

public interface VerificationTokenRepository {

    VerificationToken save(VerificationToken token);

    Optional<VerificationToken> findByContact(String contact);

    void deleteByContact(String contact);

    int deleteExpired(LocalDateTime reference);
}
