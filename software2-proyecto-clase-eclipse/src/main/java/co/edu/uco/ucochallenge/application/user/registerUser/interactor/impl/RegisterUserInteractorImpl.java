package co.edu.uco.ucochallenge.application.user.registerUser.interactor.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.user.registerUser.dto.RegisterUserInputDTO;
import co.edu.uco.ucochallenge.application.user.registerUser.dto.RegisterUserOutputDTO;
import co.edu.uco.ucochallenge.application.user.registerUser.interactor.RegisterUserInteractor;
import co.edu.uco.ucochallenge.application.user.registerUser.mapper.RegisterUserMapper;
import co.edu.uco.ucochallenge.application.user.registerUser.usecase.RegisterUserUseCase;
import co.edu.uco.ucochallenge.domain.user.model.User;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class RegisterUserInteractorImpl implements RegisterUserInteractor {

        // Mantén consistentes estos nombres con tu RedisCacheConfig
        private static final String USERS_BY_ID_CACHE = "users.byId";
        private static final String USERS_PAGES_CACHE = "users.pages";

        private final RegisterUserUseCase useCase;
        private final RegisterUserMapper mapper;

        public RegisterUserInteractorImpl(final RegisterUserUseCase useCase, final RegisterUserMapper mapper) {
                this.useCase = useCase;
                this.mapper = mapper;
        }

        @Override
        @CachePut(
                value = USERS_BY_ID_CACHE,
                key = "#result.userId()",
                condition = "@cacheFlags.enabled()",// usa el ID del usuario recién creado
                unless = "#result == null"             // no cachear si hubo algo raro
        )
        @CacheEvict(
                value = USERS_PAGES_CACHE,
                condition = "@cacheFlags.enabled()",
                allEntries = true                      // invalidar todas las páginas
        )
        public RegisterUserOutputDTO execute(final RegisterUserInputDTO dto) {
                final RegisterUserInputDTO normalizedDTO = RegisterUserInputDTO.normalize(dto);
                final User user = mapper.toDomain(normalizedDTO);
                final User registeredUser = useCase.execute(user);
                return mapper.toOutput(registeredUser);  // <- el @CachePut usa este "result"
        }
}
