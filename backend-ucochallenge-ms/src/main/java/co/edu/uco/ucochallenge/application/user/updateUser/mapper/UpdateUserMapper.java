package co.edu.uco.ucochallenge.application.user.updateUser.mapper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.uco.ucochallenge.application.hateoas.LinkDTO;
import co.edu.uco.ucochallenge.application.user.updateUser.dto.UpdateUserOutputDTO;
import co.edu.uco.ucochallenge.application.user.updateUser.interactor.UpdateUserInteractor;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.domain.user.model.User;

@Mapper(componentModel = "spring")
public interface UpdateUserMapper {
	/*
	 * default User toDomain(final UpdateUserInteractor.Command command) { final var
	 * payload = command.payload(); return new User( command.id(), payload.idType(),
	 * payload.idNumber(), payload.firstName(), payload.secondName(),
	 * payload.firstSurname(), payload.secondSurname(), payload.homeCity(),
	 * payload.email(), payload.mobileNumber(), false, false); }
	 * 
	 * @Mapping(target = "userId", source = "user.id")
	 * 
	 * @Mapping(target = "email", source = "user.email")
	 * 
	 * @Mapping(target = "fullName", expression = "java(buildFullName(user))")
	 * UpdateUserOutputDTO toOutput(User user, List<LinkDTO> links);
	 * 
	 * default String buildFullName(final User user) { return
	 * Stream.of(user.firstName(), user.secondName(), user.firstSurname(),
	 * user.secondSurname()) .filter(name -> !TextHelper.isEmpty(name))
	 * .collect(Collectors.joining(" ")); }
	 */
}
