package co.edu.uco.ucochallenge.application.user.getUser.usecase;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.usecase.UseCase;
import co.edu.uco.ucochallenge.domain.user.model.User;

public interface GetUserUseCase extends UseCase<UUID, User> {
}
