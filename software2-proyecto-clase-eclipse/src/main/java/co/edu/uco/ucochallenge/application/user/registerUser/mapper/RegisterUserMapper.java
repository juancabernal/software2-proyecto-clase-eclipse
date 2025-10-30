package co.edu.uco.ucochallenge.application.user.registerUser.mapper;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.uco.ucochallenge.application.user.registerUser.dto.RegisterUserInputDTO;
import co.edu.uco.ucochallenge.application.user.registerUser.dto.RegisterUserOutputDTO;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.domain.user.model.User;

@Mapper(componentModel = "spring")
public interface RegisterUserMapper {

        @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
        @Mapping(target = "emailConfirmed", constant = "false")
        @Mapping(target = "mobileNumberConfirmed", constant = "false")
        User toDomain(RegisterUserInputDTO dto);

        @Mapping(target = "userId", source = "id")
        @Mapping(target = "email", source = "email")
        @Mapping(target = "fullName", expression = "java(buildFullName(user))")
        RegisterUserOutputDTO toOutput(User user);

        default String buildFullName(final User user) {
                return Stream.of(user.firstName(), user.secondName(), user.firstSurname(), user.secondSurname())
                                .filter(name -> !TextHelper.isEmpty(name))
                                .collect(Collectors.joining(" "));
        }
}
