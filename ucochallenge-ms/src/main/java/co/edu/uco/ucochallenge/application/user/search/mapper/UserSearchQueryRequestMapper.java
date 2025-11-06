package co.edu.uco.ucochallenge.application.user.search.mapper;

import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.application.interactor.mapper.DomainMapper;
import co.edu.uco.ucochallenge.application.user.search.dto.UserSearchQueryRequestDTO;
import co.edu.uco.ucochallenge.domain.user.search.model.UserSearchFilterDomainModel;

@Component
public class UserSearchQueryRequestMapper
                implements DomainMapper<UserSearchQueryRequestDTO, UserSearchFilterDomainModel> {

        @Override
        public UserSearchFilterDomainModel toDomain(final UserSearchQueryRequestDTO dto) {
                final var normalized = UserSearchQueryRequestDTO.normalize(dto.page(), dto.size());
                return UserSearchFilterDomainModel.builder()
                                .page(normalized.page())
                                .size(normalized.size())
                                .build();
        }

        @Override
        public UserSearchQueryRequestDTO toDto(final UserSearchFilterDomainModel domain) {
                return new UserSearchQueryRequestDTO(domain.getPage(), domain.getSize());
        }
}
