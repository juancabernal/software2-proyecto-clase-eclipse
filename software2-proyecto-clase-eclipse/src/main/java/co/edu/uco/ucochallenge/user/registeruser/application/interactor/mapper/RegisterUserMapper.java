package co.edu.uco.ucochallenge.user.registeruser.application.interactor.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.uco.ucochallenge.secondary.adapters.repository.entity.CityEntity;
import co.edu.uco.ucochallenge.secondary.adapters.repository.entity.IdTypeEntity;
import co.edu.uco.ucochallenge.secondary.adapters.repository.entity.UserEntity;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.dto.RegisterUserInputDTO;
import co.edu.uco.ucochallenge.user.registeruser.application.usecase.domain.RegisterUserDomain;

@Mapper(componentModel = "spring")
public interface RegisterUserMapper {

        @Mapping(target = ".", expression = "java(toDomainInternal(dto))")
        RegisterUserDomain toDomain(RegisterUserInputDTO dto);

        @Mapping(target = ".", expression = "java(toEntityInternal(domain))")
        UserEntity toEntity(RegisterUserDomain domain);

        default RegisterUserDomain toDomainInternal(final RegisterUserInputDTO dto) {
                if (dto == null) {
                        return null;
                }

                return RegisterUserDomain.construir()
                                .conTipoIdentificacion(dto.idType())
                                .conNumeroIdentificacion(dto.idNumber())
                                .conPrimerNombre(dto.firstName())
                                .conSegundoNombre(dto.secondName())
                                .conPrimerApellido(dto.firstSurname())
                                .conSegundoApellido(dto.secondSurname())
                                .conCiudadResidencia(dto.homeCity())
                                .conCorreo(dto.email())
                                .conNumeroCelular(dto.mobileNumber())
                                .construir();
        }

        default UserEntity toEntityInternal(final RegisterUserDomain domain) {
                if (domain == null) {
                        return null;
                }

                return new UserEntity.Builder()
                                .id(UUID.randomUUID())
                                .idType(toIdTypeEntity(domain.getIdType()))
                                .idNumber(domain.getIdNumber())
                                .firstName(domain.getFirstName())
                                .secondName(domain.getSecondName())
                                .firstSurname(domain.getFirstSurname())
                                .secondSurname(domain.getSecondSurname())
                                .homeCity(toCityEntity(domain.getHomeCity()))
                                .email(domain.getEmail())
                                .mobileNumber(domain.getMobileNumber())
                                .build();
        }

        default IdTypeEntity toIdTypeEntity(final UUID id) {
                if (id == null) {
                        return null;
                }

                return new IdTypeEntity.Builder()
                                .id(id)
                                .build();
        }

        default CityEntity toCityEntity(final UUID id) {
                if (id == null) {
                        return null;
                }

                return new CityEntity.Builder()
                                .id(id)
                                .build();
        }
}