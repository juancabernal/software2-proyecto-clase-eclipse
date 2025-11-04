package co.edu.uco.ucochallenge.application.notification;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import co.edu.uco.ucochallenge.crosscuting.parameter.ParameterCodes;
import co.edu.uco.ucochallenge.crosscuting.parameter.ParameterProvider;

@Service
public class VerificationTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationTokenService.class);
    private static final String KEY_PATTERN = "verify:%s:%s";
    private static final int DEFAULT_TTL_MINUTES = 5;

    private final StringRedisTemplate redisTemplate;

    public VerificationTokenService(final StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public int issueOrRefresh(final String userId, final String channel) {
        final String key = buildKey(userId, channel);
        final int ttlSeconds = resolveTtlSeconds();
        final String value = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
        return ttlSeconds;
    }

    public int getRemainingSeconds(final String userId, final String channel) {
        final String key = buildKey(userId, channel);
        final Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl.longValue() == -2L) {
            return -1;
        }
        if (ttl < 0) {
            return 0;
        }
        return ttl.intValue();
    }

    private String buildKey(final String userId, final String channel) {
        return KEY_PATTERN.formatted(channel, userId);
    }

    private int resolveTtlSeconds() {
        int minutes = DEFAULT_TTL_MINUTES;
        try {
            final int configured = ParameterProvider
                    .getInteger(ParameterCodes.Verification.VERIFICATION_CODE_EXPIRATION_MINUTES);
            if (configured > 0) {
                minutes = configured;
            }
        } catch (final RuntimeException exception) {
            LOGGER.warn("Falling back to default verification token TTL of {} minutes due to parameter retrieval error.",
                    DEFAULT_TTL_MINUTES, exception);
        }
        return Math.max(minutes, 1) * 60;
    }
}

