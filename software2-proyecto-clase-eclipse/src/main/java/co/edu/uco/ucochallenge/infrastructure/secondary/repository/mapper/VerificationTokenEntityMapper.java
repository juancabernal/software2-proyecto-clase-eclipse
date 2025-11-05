package co.edu.uco.ucochallenge.infrastructure.secondary.repository.mapper;

import org.mapstruct.Mapper;

import co.edu.uco.ucochallenge.domain.verification.model.VerificationToken;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.VerificationTokenEntity;

@Mapper(componentModel = "spring")
public interface VerificationTokenEntityMapper {

    VerificationToken toDomain(VerificationTokenEntity entity);

    VerificationTokenEntity toEntity(VerificationToken token);

    } 