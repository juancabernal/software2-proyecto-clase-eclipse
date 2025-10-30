package co.edu.uco.ucochallenge.infrastructure.secondary.repository.mapper;

import java.util.List;
import java.util.UUID;

import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.CityEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.IdTypeEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

        @Mapping(target = "idType", source = "idType.id")
        @Mapping(target = "homeCity", source = "homeCity.id")
        User toDomain(UserEntity entity);

        List<User> toDomainList(List<UserEntity> entities);

        @InheritInverseConfiguration
        @BeanMapping(builder = @Builder(disableBuilder = false))
        @Mapping(target = "idType", source = "idType", qualifiedByName = "mapIdType")
        @Mapping(target = "homeCity", source = "homeCity", qualifiedByName = "mapCity")
        @Mapping(target = "emailConfirmedIsDefaultValue", ignore = true)
        @Mapping(target = "mobileNumberConfirmedIsDefaultValue", ignore = true)
        UserEntity toEntity(User user);

        @Named("mapIdType")
        default IdTypeEntity mapIdType(final UUID idType) {
                return new IdTypeEntity.Builder().id(idType).build();
        }

        @Named("mapCity")
        default CityEntity mapCity(final UUID cityId) {
                return new CityEntity.Builder().id(cityId).build();
        }
}
