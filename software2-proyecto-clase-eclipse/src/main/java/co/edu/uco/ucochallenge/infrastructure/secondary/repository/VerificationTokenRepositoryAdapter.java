package co.edu.uco.ucochallenge.infrastructure.secondary.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.domain.verification.model.VerificationToken;
import co.edu.uco.ucochallenge.domain.verification.port.out.VerificationTokenRepository;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.VerificationTokenEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.mapper.VerificationTokenEntityMapper;

@Component
public class VerificationTokenRepositoryAdapter implements VerificationTokenRepository {

    private final VerificationTokenJpaRepository jpaRepository;
    private final VerificationTokenEntityMapper mapper;

    public VerificationTokenRepositoryAdapter(final VerificationTokenJpaRepository jpaRepository,
            final VerificationTokenEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public VerificationToken save(final VerificationToken token) {
        final VerificationTokenEntity entity = mapper.toEntity(token);
        final VerificationTokenEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VerificationToken> findByContact(final String contact) {
        return jpaRepository.findTopByContactOrderByCreatedAtDesc(contact)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteByContact(final String contact) {
        jpaRepository.deleteByContactIgnoreCase(contact);
    }

    @Override
    @Transactional
    public int deleteExpired(final LocalDateTime reference) {
        return jpaRepository.deleteExpired(reference);
    }
}
