package co.edu.uco.ucochallenge.infrastructure.secondary.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.VerificationTokenEntity;

@Repository
public interface VerificationTokenJpaRepository extends JpaRepository<VerificationTokenEntity, UUID> {

    Optional<VerificationTokenEntity> findTopByContactOrderByCreatedAtDesc(String contact);

    Optional<VerificationTokenEntity> findTopByContactIgnoreCaseAndCodeIgnoreCaseOrderByCreatedAtDesc(String contact, String code);

    @Modifying
    @Query("DELETE FROM VerificationTokenEntity v WHERE LOWER(v.contact) = LOWER(:contact)")
    void deleteByContactIgnoreCase(@Param("contact") String contact);

    @Modifying
    @Query("DELETE FROM VerificationTokenEntity v WHERE v.expiration <= :reference")
    int deleteExpired(@Param("reference") LocalDateTime reference);
}
