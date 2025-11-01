package co.edu.uco.ucochallenge.application.user.searchUsers.interactor;

import co.edu.uco.ucochallenge.application.interactor.Interactor;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersResponseDTO;
import co.edu.uco.ucochallenge.application.user.searchUsers.dto.SearchUsersQueryDTO;

public interface SearchUsersInteractor extends Interactor<SearchUsersQueryDTO, ListUsersResponseDTO> {
}
