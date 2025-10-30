package co.edu.uco.ucochallenge.application.user.deleteUser.interactor.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.application.user.deleteUser.interactor.DeleteUserInteractor;
import co.edu.uco.ucochallenge.application.user.deleteUser.usecase.DeleteUserUseCase;

@Service
@Transactional
public class DeleteUserInteractorImpl implements DeleteUserInteractor {

        private final DeleteUserUseCase useCase;

        public DeleteUserInteractorImpl(final DeleteUserUseCase useCase) {
                this.useCase = useCase;
        }

        @Override
        public Void execute(final UUID dto) {
                return useCase.execute(dto);
        }
}
