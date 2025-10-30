package co.edu.uco.ucochallenge.application.user.updateUser.interactor;

import java.util.UUID;

import co.edu.uco.ucochallenge.application.interactor.Interactor;
import co.edu.uco.ucochallenge.application.user.updateUser.dto.UpdateUserInputDTO;
import co.edu.uco.ucochallenge.application.user.updateUser.dto.UpdateUserOutputDTO;

public interface UpdateUserInteractor extends Interactor<UpdateUserInteractor.Command, UpdateUserOutputDTO> {

        record Command(UUID id, UpdateUserInputDTO payload) {

                public static Command of(final UUID id, final UpdateUserInputDTO payload) {
                        return new Command(id, payload);
                }
        }
}
