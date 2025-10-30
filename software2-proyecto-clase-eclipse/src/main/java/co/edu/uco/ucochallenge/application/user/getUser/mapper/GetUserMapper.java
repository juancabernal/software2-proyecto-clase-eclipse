package co.edu.uco.ucochallenge.application.user.getUser.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.uco.ucochallenge.application.user.getUser.dto.GetUserOutputDTO;
import co.edu.uco.ucochallenge.domain.user.model.User;

@Mapper(componentModel = "spring")
public interface GetUserMapper {

        @Mapping(target = "userId", source = "id")
        GetUserOutputDTO toOutput(User user);
}
