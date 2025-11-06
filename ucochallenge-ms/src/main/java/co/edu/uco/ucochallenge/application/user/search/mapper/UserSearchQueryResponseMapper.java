package co.edu.uco.ucochallenge.application.user.search.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.application.interactor.mapper.DomainMapper;
import co.edu.uco.ucochallenge.application.user.search.dto.UserSearchQueryResponseDTO;
import co.edu.uco.ucochallenge.application.user.search.dto.UserSearchQuerySummaryDTO;
import co.edu.uco.ucochallenge.domain.user.search.model.UserSearchResultDomainModel;
import co.edu.uco.ucochallenge.domain.user.search.model.UserSearchSummaryDomainModel;

@Component
public class UserSearchQueryResponseMapper
                implements DomainMapper<UserSearchQueryResponseDTO, UserSearchResultDomainModel> {

        @Override
        public UserSearchResultDomainModel toDomain(final UserSearchQueryResponseDTO dto) {
                final List<UserSearchSummaryDomainModel> summaries = new ArrayList<>();
                if (dto.getUsers() != null) {
                        dto.getUsers().forEach(user -> summaries.add(UserSearchSummaryDomainModel.builder()
                                        .id(user.getId())
                                        .firstName(user.getFirstName())
                                        .firstSurname(user.getLastName())
                                        .email(user.getEmail())
                                        .mobileNumber(user.getMobileNumber())
                                        .emailConfirmed(Boolean.TRUE.equals(user.getEmailConfirmed()))
                                        .mobileNumberConfirmed(Boolean.TRUE.equals(user.getMobileNumberConfirmed()))
                                        .build()));
                }

                return UserSearchResultDomainModel.builder()
                                .page(dto.getPage())
                                .size(dto.getSize())
                                .totalElements(dto.getTotalElements())
                                .users(summaries)
                                .build();
        }

        @Override
        public UserSearchQueryResponseDTO toDto(final UserSearchResultDomainModel domain) {
                final var dto = new UserSearchQueryResponseDTO();
                dto.setPage(domain.getPage());
                dto.setSize(domain.getSize());
                dto.setTotalElements(domain.getTotalElements());

                final List<UserSearchQuerySummaryDTO> users = new ArrayList<>();
                domain.getUsers().forEach(user -> {
                        final var summary = new UserSearchQuerySummaryDTO();
                        summary.setId(user.getId());
                        summary.setFirstName(user.getFirstName());
                        summary.setLastName(user.getFirstSurname());
                        summary.setEmail(user.getEmail());
                        summary.setMobileNumber(user.getMobileNumber());
                        summary.setEmailConfirmed(user.isEmailConfirmed());
                        summary.setMobileNumberConfirmed(user.isMobileNumberConfirmed());
                        users.add(summary);
                });
                dto.setUsers(users);
                return dto;
        }
}
