package co.edu.uco.ucochallenge.application.user.getUser.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.application.user.getUser.dto.GetUserOutputDTO;
import co.edu.uco.ucochallenge.application.user.getUser.interactor.GetUserInteractor;
import co.edu.uco.ucochallenge.application.user.getUser.mapper.GetUserMapper;
import co.edu.uco.ucochallenge.application.user.getUser.usecase.GetUserUseCase;

@Service
@Transactional(readOnly = true)
public class GetUserInteractorImpl implements GetUserInteractor {

        private final GetUserUseCase useCase;
        private final GetUserMapper mapper;

        public GetUserInteractorImpl(final GetUserUseCase useCase, final GetUserMapper mapper) {
                this.useCase = useCase;
                this.mapper = mapper;
        }

        @Override
        public GetUserOutputDTO execute(final UUID dto) {
                return mapper.toOutput(useCase.execute(dto));
        }
}
