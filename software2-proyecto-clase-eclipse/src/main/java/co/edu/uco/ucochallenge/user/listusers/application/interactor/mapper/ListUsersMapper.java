package co.edu.uco.ucochallenge.user.listusers.application.interactor.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import co.edu.uco.ucochallenge.secondary.adapters.repository.entity.CityEntity;
import co.edu.uco.ucochallenge.secondary.adapters.repository.entity.IdTypeEntity;
import co.edu.uco.ucochallenge.secondary.adapters.repository.entity.StateEntity;
import co.edu.uco.ucochallenge.secondary.adapters.repository.entity.UserEntity;
import co.edu.uco.ucochallenge.user.listusers.application.interactor.dto.ListUsersInputDTO;
import co.edu.uco.ucochallenge.user.listusers.application.interactor.dto.ListUsersOutputDTO;
import co.edu.uco.ucochallenge.user.listusers.application.interactor.dto.UserSummaryDTO;
import co.edu.uco.ucochallenge.user.listusers.application.usecase.domain.ListUsersPageDomain;
import co.edu.uco.ucochallenge.user.listusers.application.usecase.domain.ListUsersQueryDomain;
import co.edu.uco.ucochallenge.user.listusers.application.usecase.domain.UserSummaryDomain;

@Component
public class ListUsersMapper {

    public ListUsersQueryDomain toDomain(final ListUsersInputDTO dto) {
        if (dto == null) {
            return null;
        }
        return ListUsersQueryDomain.builder()
                .withPage(dto.page())
                .withSize(dto.size())
                .build();
    }

    public ListUsersOutputDTO toDTO(final ListUsersPageDomain domain) {
        if (domain == null) {
            return null;
        }

        final List<UserSummaryDTO> users = domain.getUsers().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new ListUsersOutputDTO(users, domain.getPage(), domain.getSize(), domain.getTotalElements(),
                domain.getTotalPages(), domain.isHasNext(), domain.isHasPrevious());
    }

    public ListUsersPageDomain toPageDomain(final Page<UserEntity> page) {
        if (page == null) {
            return null;
        }

        final List<UserSummaryDomain> users = page.stream()
                .map(this::toUserSummaryDomain)
                .collect(Collectors.toList());

        return ListUsersPageDomain.builder()
                .withUsers(users)
                .withPage(page.getNumber())
                .withSize(page.getSize())
                .withTotalElements(page.getTotalElements())
                .withTotalPages(page.getTotalPages())
                .withHasNext(page.hasNext())
                .withHasPrevious(page.hasPrevious())
                .build();
    }

    private UserSummaryDTO toDTO(final UserSummaryDomain domain) {
        if (domain == null) {
            return null;
        }

        return new UserSummaryDTO(domain.getId(), domain.getIdTypeId(), domain.getIdTypeName(), domain.getIdNumber(),
                domain.getFirstName(), domain.getSecondName(), domain.getFirstSurname(), domain.getSecondSurname(),
                domain.getHomeCityId(), domain.getHomeCityName(), domain.getHomeStateId(), domain.getHomeStateName(),
                domain.getEmail(), domain.getMobileNumber(), domain.isEmailConfirmed(), domain.isMobileNumberConfirmed());
    }

    private UserSummaryDomain toUserSummaryDomain(final UserEntity entity) {
        if (entity == null) {
            return null;
        }

        final IdTypeEntity idType = entity.getIdType();
        final CityEntity city = entity.getHomeCity();
        final StateEntity state = city != null ? city.getState() : null;

        return UserSummaryDomain.builder()
                .withId(UUIDHelper.getDefault(entity.getId()))
                .withIdType(UUIDHelper.getDefault(idType != null ? idType.getId() : null),
                        TextHelper.getDefaultWithTrim(idType != null ? idType.getName() : null))
                .withIdNumber(TextHelper.getDefaultWithTrim(entity.getIdNumber()))
                .withFirstName(TextHelper.getDefaultWithTrim(entity.getFirstName()))
                .withSecondName(TextHelper.getDefaultWithTrim(entity.getSecondName()))
                .withFirstSurname(TextHelper.getDefaultWithTrim(entity.getFirstSurname()))
                .withSecondSurname(TextHelper.getDefaultWithTrim(entity.getSecondSurname()))
                .withHomeCity(UUIDHelper.getDefault(city != null ? city.getId() : null),
                        TextHelper.getDefaultWithTrim(city != null ? city.getName() : null),
                        UUIDHelper.getDefault(state != null ? state.getId() : null),
                        TextHelper.getDefaultWithTrim(state != null ? state.getName() : null))
                .withEmail(TextHelper.getDefaultWithTrim(entity.getEmail()))
                .withMobileNumber(TextHelper.getDefaultWithTrim(entity.getMobileNumber()))
                .withEmailConfirmed(entity.isEmailConfirmed())
                .withMobileNumberConfirmed(entity.isMobileNumberConfirmed())
                .build();
    }
}