package co.edu.uco.ucochallenge.application.user.listUsers.usecase;

import java.util.List;

import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.application.usecase.UseCase;
import co.edu.uco.ucochallenge.domain.user.model.User;

public interface ListUsersUseCase extends UseCase<Void, List<User>> {
}
