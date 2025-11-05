package co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity;

import java.time.Instant;
import java.util.UUID;

import co.edu.uco.ucochallenge.crosscuting.helper.ObjectHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "verification_tokens")
public class VerificationTokenEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "contact", nullable = false)
    private String contact;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "expiration", nullable = false)
    private Instant expiration;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public VerificationTokenEntity() {
        this.id = UUIDHelper.getDefault();
        this.contact = TextHelper.getDefault();
        this.code = TextHelper.getDefault();
        this.expiration = Instant.now();
        this.attempts = 0;
        this.createdAt = Instant.now();
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (id == null || UUIDHelper.getDefault().equals(id)) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = UUIDHelper.getDefault(id);
    }

    public String getContact() {
        return contact;
    }

    public void setContact(final String contact) {
        this.contact = TextHelper.getDefaultWithTrim(contact);
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = TextHelper.getDefaultWithTrim(code);
    }

    public Instant getExpiration() {
        return expiration;
    }

    public void setExpiration(final Instant expiration) {
        this.expiration = ObjectHelper.getDefault(expiration, Instant.now());
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(final int attempts) {
        this.attempts = Math.max(attempts, 0);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = ObjectHelper.getDefault(createdAt, Instant.now());
    }
}
