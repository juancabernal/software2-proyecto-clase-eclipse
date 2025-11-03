package co.edu.uco.ucochallenge.infrastructure.primary.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.uco.ucochallenge.application.Void;
import co.edu.uco.ucochallenge.application.pagination.dto.PaginationMetadataDTO;
import co.edu.uco.ucochallenge.application.user.deleteUser.interactor.DeleteUserInteractor;
import co.edu.uco.ucochallenge.application.user.getUser.interactor.GetUserInteractor;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersOutputDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.dto.ListUsersResponseDTO;
import co.edu.uco.ucochallenge.application.user.listUsers.interactor.ListUsersInteractor;
import co.edu.uco.ucochallenge.application.user.registerUser.dto.RegisterUserInputDTO;
import co.edu.uco.ucochallenge.application.user.registerUser.dto.RegisterUserOutputDTO;
import co.edu.uco.ucochallenge.application.user.registerUser.interactor.RegisterUserInteractor;
import co.edu.uco.ucochallenge.application.user.searchUsers.interactor.SearchUsersInteractor;
import co.edu.uco.ucochallenge.crosscuting.key.MessageKey;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageServicePortHolder;
import co.edu.uco.ucochallenge.infrastructure.primary.controller.response.ApiSuccessResponse;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.CityJpaRepository;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.DepartmentJpaRepository;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.IdTypeJpaRepository;
import co.edu.uco.ucochallenge.shared.InMemoryMessageServicePort;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final Map<String, String> MESSAGES = Map.ofEntries(
            Map.entry(MessageKey.RegisterUser.SUCCESS, "Usuario registrado exitosamente."),
            Map.entry(MessageKey.ListUsers.SUCCESS, "Usuarios obtenidos exitosamente."),
            Map.entry(MessageKey.GetUser.SUCCESS, "Usuario obtenido exitosamente."),
            Map.entry(MessageKey.SearchUsers.SUCCESS, "Usuarios filtrados exitosamente."),
            Map.entry(MessageKey.DeleteUser.SUCCESS, "Usuario eliminado exitosamente."));

    @Mock
    private RegisterUserInteractor registerUserInteractor;
    @Mock
    private ListUsersInteractor listUsersInteractor;
    @Mock
    private GetUserInteractor getUserInteractor;
    @Mock
    private SearchUsersInteractor searchUsersInteractor;
    @Mock
    private DeleteUserInteractor deleteUserInteractor;
    @Mock
    private DepartmentJpaRepository departmentJpaRepository;
    @Mock
    private CityJpaRepository cityJpaRepository;
    @Mock
    private IdTypeJpaRepository idTypeJpaRepository;

    private UserController controller;

    @BeforeEach
    void setUp() {
        MessageServicePortHolder.configure(new InMemoryMessageServicePort(MESSAGES));
        controller = new UserController(
                registerUserInteractor,
                listUsersInteractor,
                getUserInteractor,
                searchUsersInteractor,
                deleteUserInteractor,
                departmentJpaRepository,
                cityJpaRepository,
                idTypeJpaRepository);
    }

    @AfterEach
    void tearDown() {
        MessageServicePortHolder.configure(null);
    }

    @Test
    void registerUserReturnsCatalogMessage() {
        final RegisterUserInputDTO input = new RegisterUserInputDTO(
                UUID.randomUUID(),
                "123456",
                "John",
                "",
                "Doe",
                "",
                UUID.randomUUID(),
                "john.doe@example.com",
                "3001234567");
        final RegisterUserOutputDTO output = new RegisterUserOutputDTO(UUID.randomUUID(), "John Doe", "john.doe@example.com");
        when(registerUserInteractor.execute(input)).thenReturn(output);

        final ResponseEntity<ApiSuccessResponse<RegisterUserOutputDTO>> response = controller.registerUser(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Usuario registrado exitosamente.", response.getBody().userMessage());
        assertEquals(output, response.getBody().data());
    }

    @Test
    void listUsersReturnsCatalogMessage() {
        final ListUsersResponseDTO responseDto = new ListUsersResponseDTO(
                Collections.singletonList(new ListUsersOutputDTO(
                        UUID.randomUUID(),
                        "CC",
                        "123456",
                        "John Doe",
                        "john.doe@example.com",
                        "3001234567",
                        true,
                        true)),
                new PaginationMetadataDTO(0, 10, 1, 1, false, false));
        when(listUsersInteractor.execute(any())).thenReturn(responseDto);

        final ResponseEntity<ApiSuccessResponse<ListUsersResponseDTO>> response = controller.listUsers(null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Usuarios obtenidos exitosamente.", response.getBody().userMessage());
        assertEquals(responseDto, response.getBody().data());
    }

    @Test
    void deleteUserReturnsCatalogMessage() {
        final UUID userId = UUID.randomUUID();

        final ResponseEntity<ApiSuccessResponse<Void>> response = controller.deleteUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Usuario eliminado exitosamente.", response.getBody().userMessage());
        assertEquals(Void.returnVoid(), response.getBody().data());
    }
}
