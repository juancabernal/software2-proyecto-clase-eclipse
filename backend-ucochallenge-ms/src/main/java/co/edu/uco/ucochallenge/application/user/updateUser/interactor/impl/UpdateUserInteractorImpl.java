package co.edu.uco.ucochallenge.application.user.updateUser.interactor.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.hateoas.LinkDTO;
import co.edu.uco.ucochallenge.application.user.updateUser.dto.UpdateUserInputDTO;
import co.edu.uco.ucochallenge.application.user.updateUser.dto.UpdateUserOutputDTO;
import co.edu.uco.ucochallenge.application.user.updateUser.interactor.UpdateUserInteractor;
import co.edu.uco.ucochallenge.application.user.updateUser.mapper.UpdateUserMapper;
import co.edu.uco.ucochallenge.application.user.updateUser.usecase.UpdateUserUseCase;
import co.edu.uco.ucochallenge.domain.user.model.User;
import jakarta.transaction.Transactional;

/*@Service
@Transactional
public class UpdateUserInteractorImpl implements UpdateUserInteractor {*/

	/*
	 * private static final String USERS_BASE_PATH = "/uco-challenge/api/v1/users";
	 * 
	 * private final UpdateUserUseCase useCase; private final UpdateUserMapper
	 * mapper;
	 * 
	 * public UpdateUserInteractorImpl(final UpdateUserUseCase useCase, final
	 * UpdateUserMapper mapper) { this.useCase = useCase; this.mapper = mapper; }
	 * 
	 * @Override public UpdateUserOutputDTO execute(final Command command) { final
	 * UpdateUserInputDTO normalizedPayload =
	 * UpdateUserInputDTO.normalize(command.payload()); final Command
	 * normalizedCommand = Command.of(command.id(), normalizedPayload); final User
	 * changes = mapper.toDomain(normalizedCommand); final User updatedUser =
	 * useCase.execute(changes); final List<LinkDTO> links =
	 * buildLinks(updatedUser.id()); return mapper.toOutput(updatedUser, links); }
	 * 
	 * private List<LinkDTO> buildLinks(final UUID userId) { final String userPath =
	 * USERS_BASE_PATH + "/" + userId; return List.of( LinkDTO.of("self", userPath,
	 * "GET"), LinkDTO.of("update", userPath, "PUT"), LinkDTO.of("delete", userPath,
	 * "DELETE"), LinkDTO.of("collection", USERS_BASE_PATH, "GET")); }
	 
}*/
