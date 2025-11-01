package co.edu.uco.ucochallenge.application.user.registerUser.interactor.impl;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.user.registerUser.interactor.RegisterUserInteractor;
import co.edu.uco.ucochallenge.application.user.registerUser.dto.RegisterUserInputDTO;
import co.edu.uco.ucochallenge.application.user.registerUser.usecase.RegisterUserUseCase;
import co.edu.uco.ucochallenge.application.user.registerUser.dto.RegisterUserOutputDTO;
import co.edu.uco.ucochallenge.application.user.registerUser.mapper.RegisterUserMapper;
import co.edu.uco.ucochallenge.domain.user.model.User;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class RegisterUserInteractorImpl implements RegisterUserInteractor {

        private final RegisterUserUseCase useCase;
        private final RegisterUserMapper mapper;

        public RegisterUserInteractorImpl(final RegisterUserUseCase useCase, final RegisterUserMapper mapper) {
                this.useCase = useCase;
                this.mapper = mapper;
        }

        @Override
        public RegisterUserOutputDTO execute(final RegisterUserInputDTO dto) {
                final RegisterUserInputDTO normalizedDTO = RegisterUserInputDTO.normalize(dto);
                final User user = mapper.toDomain(normalizedDTO);
                final User registeredUser = useCase.execute(user);
                return mapper.toOutput(registeredUser);
        }

}
