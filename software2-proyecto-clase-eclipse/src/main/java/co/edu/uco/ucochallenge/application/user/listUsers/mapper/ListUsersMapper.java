package co.edu.uco.ucochallenge.application.user.listUsers.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersOutputDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersResponseDTO;
import co.edu.uco.ucochallenge.domain.user.model.User;

//.
@Mapper(componentModel = "spring")
public interface ListUsersMapper {
	
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "idType", expression = "java(user.idType().toString())")
    @Mapping(target = "fullName", expression = "java(buildFullName(user))")
    ListUsersOutputDTO toOutput(User user);

    default ListUsersResponseDTO toResponse(final List<User> users) {
            return new ListUsersResponseDTO(users.stream().map(this::toOutput).toList());
    }

    default String buildFullName(final User user) {
            return String.join(" ",
                            user.firstName(),
                            user.secondName(),
                            user.firstSurname(),
                            user.secondSurname());
    }	
	
}
