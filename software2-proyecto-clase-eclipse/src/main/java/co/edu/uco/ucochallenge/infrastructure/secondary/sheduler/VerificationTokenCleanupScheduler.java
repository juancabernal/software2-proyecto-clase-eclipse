package co.edu.uco.ucochallenge.infrastructure.secondary.sheduler;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.domain.verification.port.out.VerificationTokenRepository;

@Component
public class VerificationTokenCleanupScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationTokenCleanupScheduler.class);

    private final VerificationTokenRepository repository;

    public VerificationTokenCleanupScheduler(final VerificationTokenRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelayString = "PT5M")
    public void purgeExpiredTokens() {
        final int removed = repository.deleteExpired(Instant.now());
        if (removed > 0) {
            LOGGER.info("Removed {} expired verification tokens", removed);
        }
    }
}