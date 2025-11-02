package co.edu.uco.ucochallenge.infrastructure.secondary.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.pagination.PageCriteria;
import co.edu.uco.ucochallenge.domain.pagination.PaginatedResult;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.model.UserFilter;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.entity.UserEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.repository.mapper.UserEntityMapper;

@Component
public class UserRepositoryAdapter implements UserRepository {

        private final UserJpaRepository jpaRepository;
        private final UserEntityMapper mapper;
        private final CityJpaRepository cityJpaRepository;
        private final IdTypeJpaRepository idTypeJpaRepository;

        public UserRepositoryAdapter(final UserJpaRepository jpaRepository, final UserEntityMapper mapper,
                        final CityJpaRepository cityJpaRepository, final IdTypeJpaRepository idTypeJpaRepository) {
                this.jpaRepository = jpaRepository;
                this.mapper = mapper;
                this.cityJpaRepository = cityJpaRepository;
                this.idTypeJpaRepository = idTypeJpaRepository;
        }

        @Override
        public boolean existsByEmail(final String email) {
                return jpaRepository.existsByEmailIgnoreCase(email);
        }

        @Override
        public boolean existsByIdTypeAndIdNumber(final UUID idType, final String idNumber) {
                return jpaRepository.existsByIdTypeIdAndIdNumber(idType, idNumber);
        }

        @Override
        public boolean existsByMobileNumber(final String mobileNumber) {
                return jpaRepository.existsByMobileNumber(mobileNumber);
        }

        @Override
        public boolean existsByEmailExcludingId(final UUID id, final String email) {
                return jpaRepository.existsByEmailIgnoreCaseAndIdNot(email, id);
        }

        @Override
        public boolean existsByIdTypeAndIdNumberExcludingId(final UUID id, final UUID idType, final String idNumber) {
                return jpaRepository.existsByIdTypeIdAndIdNumberAndIdNot(idType, idNumber, id);
        }

        @Override
        public boolean existsByMobileNumberExcludingId(final UUID id, final String mobileNumber) {
                return jpaRepository.existsByMobileNumberAndIdNot(mobileNumber, id);
        }

        @Override
        public User save(final User user) {
                validateReferences(user);
                final UserEntity entity = mapper.toEntity(user);
                final UserEntity savedEntity = jpaRepository.save(entity);
                return mapper.toDomain(savedEntity);
        }

        private void validateReferences(final User user) {
                if (!idTypeJpaRepository.existsById(user.idType())) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.ID_TYPE_NOT_FOUND_TECHNICAL,
                                        MessageCodes.Domain.User.ID_TYPE_NOT_FOUND_USER);
                }

                if (!cityJpaRepository.existsById(user.homeCity())) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.HOME_CITY_NOT_FOUND_TECHNICAL,
                                        MessageCodes.Domain.User.HOME_CITY_NOT_FOUND_USER);
                }
        }

        @Override
        public PaginatedResult<User> findAll(final PageCriteria pagination) {
                final Pageable pageable = buildPageable(pagination);
                final Page<UserEntity> page = jpaRepository.findAll(pageable);
                return toPaginatedResult(page);
        }

        @Override
        public Optional<User> findById(final UUID id) {
                return jpaRepository.findById(id).map(mapper::toDomain);
        }

        @Override
        public void deleteById(final UUID id) {
                if (!jpaRepository.existsById(id)) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.NOT_FOUND_TECHNICAL,
                                        MessageCodes.Domain.User.NOT_FOUND_USER);
                }
                jpaRepository.deleteById(id);
        }

        @Override
        public PaginatedResult<User> findByFilter(final UserFilter filter, final PageCriteria pagination) {
                final UUID idType = filter.hasIdType() ? filter.idType() : null;
                final UUID homeCity = filter.hasHomeCity() ? filter.homeCity() : null;
                final String idNumber = filter.hasIdNumber() ? filter.idNumber() : null;
                final String firstName = filter.hasFirstName() ? filter.firstName() : null;
                final String firstSurname = filter.hasFirstSurname() ? filter.firstSurname() : null;
                final String email = filter.hasEmail() ? filter.email() : null;
                final String mobileNumber = filter.hasMobileNumber() ? filter.mobileNumber() : null;

                final Pageable pageable = buildPageable(pagination);
                final Page<UserEntity> page = jpaRepository.search(
                                idType,
                                homeCity,
                                idNumber,
                                firstName,
                                firstSurname,
                                email,
                                mobileNumber,
                                pageable);

                return toPaginatedResult(page);
        }

        private Pageable buildPageable(final PageCriteria pagination) {
                return PageRequest.of(pagination.page(), pagination.size());
        }

        private PaginatedResult<User> toPaginatedResult(final Page<UserEntity> page) {
                final Page<User> users = page.map(mapper::toDomain);
                return PaginatedResult.of(
                                users.getContent(),
                                page.getTotalElements(),
                                page.getTotalPages(),
                                page.getNumber(),
                                page.getSize());
        }
}
