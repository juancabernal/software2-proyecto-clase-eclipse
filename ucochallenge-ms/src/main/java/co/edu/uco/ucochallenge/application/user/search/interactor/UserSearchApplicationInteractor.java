package co.edu.uco.ucochallenge.application.user.search.interactor;

import co.edu.uco.ucochallenge.application.interactor.Interactor;
import co.edu.uco.ucochallenge.application.user.search.dto.UserSearchQueryRequestDTO;
import co.edu.uco.ucochallenge.application.user.search.dto.UserSearchQueryResponseDTO;

public interface UserSearchApplicationInteractor
                extends Interactor<UserSearchQueryRequestDTO, UserSearchQueryResponseDTO> {
}
