package co.edu.uco.ucochallenge.infrastructure.secondary.repository.mapper;

import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.domain.verification.model.VerificationToken;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.VerificationTokenEntity;

@Component
public class VerificationTokenEntityMapperImpl implements VerificationTokenEntityMapper {

    @Override
    public VerificationToken toDomain(final VerificationTokenEntity entity) {
        if (entity == null) {
            return null;
        }
        return new VerificationToken(
                entity.getId(),
                entity.getContact(),
                entity.getCode(),
                entity.getExpiration(),
                entity.getAttempts(),
                entity.getCreatedAt());
    }

    @Override
    public VerificationTokenEntity toEntity(final VerificationToken token) {
        if (token == null) {
            return null;
        }
        final VerificationTokenEntity entity = new VerificationTokenEntity();
        entity.setId(token.id());
        entity.setContact(token.contact());
        entity.setCode(token.code());
        entity.setExpiration(token.expiration());
        entity.setAttempts(token.attempts());
        entity.setCreatedAt(token.createdAt());
        return entity;
    }
}
