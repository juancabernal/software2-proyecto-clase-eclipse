package co.edu.uco.ucochallenge.user.registeruser.application.interactor.impl;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.RegisterUserInteractor;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.dto.RegisterUserInputDTO;
import co.edu.uco.ucochallenge.user.registeruser.application.interactor.usecase.RegisterUserUseCase;
import co.edu.uco.ucochallenge.user.registeruser.application.usecase.domain.RegisterUserDomain;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class RegisterUserInteractorImpl implements RegisterUserInteractor {

	private RegisterUserUseCase useCase;

	public RegisterUserInteractorImpl(RegisterUserUseCase useCase) {
		this.useCase = useCase;
	}

	@Override
	public Void execute(final RegisterUserInputDTO dto) {

		// DataMapper/MapStruct could be used here
		RegisterUserDomain registerUserDomain = null;
		return useCase.execute(registerUserDomain); // Mapping from DTO to Domain is needed
	}

}
