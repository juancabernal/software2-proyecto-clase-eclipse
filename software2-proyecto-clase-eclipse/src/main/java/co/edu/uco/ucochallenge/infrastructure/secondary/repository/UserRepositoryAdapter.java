package co.edu.uco.ucochallenge.infrastructure.secondary.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
@Component
public class UserRepositoryAdapter implements UserRepository {

        private final UserJpaRepository jpaRepository;
        private final UserEntityMapper mapper;
        private final CityJpaRepository cityJpaRepository;
        private final IdTypeJpaRepository idTypeJpaRepository;
        @PersistenceContext
        private EntityManager entityManager;
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

                final Pageable pageable = buildPageable(pagination);
                final CriteriaBuilder builder = entityManager.getCriteriaBuilder();

                final CriteriaQuery<UserEntity> query = builder.createQuery(UserEntity.class);
                final Root<UserEntity> root = query.from(UserEntity.class);
                final List<Predicate> predicates = buildPredicates(filter, builder, root);

                if (!predicates.isEmpty()) {
                        query.where(predicates.toArray(Predicate[]::new));
                }
                query.orderBy(
                        builder.asc(builder.lower(root.get("firstSurname"))),
                        builder.asc(builder.lower(root.get("firstName"))),
                        builder.asc(builder.lower(root.get("idNumber"))));

                final TypedQuery<UserEntity> typedQuery = entityManager.createQuery(query);
                typedQuery.setFirstResult((int) pageable.getOffset());
                typedQuery.setMaxResults(pageable.getPageSize());
                final List<UserEntity> content = typedQuery.getResultList();

                final CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
                final Root<UserEntity> countRoot = countQuery.from(UserEntity.class);
                final List<Predicate> countPredicates = buildPredicates(filter, builder, countRoot);
                if (!countPredicates.isEmpty()) {
                        countQuery.where(countPredicates.toArray(Predicate[]::new));
                }
                countQuery.select(builder.count(countRoot));
                final long total = entityManager.createQuery(countQuery).getSingleResult();

                final Page<UserEntity> page = new PageImpl<>(content, pageable, total);

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
        private List<Predicate> buildPredicates(
                final UserFilter filter,
                final CriteriaBuilder builder,
                final Root<UserEntity> root) {
                final List<Predicate> predicates = new ArrayList<>();

                if (filter.hasIdType()) {
                        predicates.add(builder.equal(root.get("idType").get("id"), filter.idType()));
                }

                if (filter.hasHomeCity()) {
                        predicates.add(builder.equal(root.get("homeCity").get("id"), filter.homeCity()));
                }

                if (filter.hasIdNumber()) {
                        predicates.add(likeIgnoreCase(builder, root.get("idNumber"), filter.idNumber()));
                }

                if (filter.hasFirstName()) {
                        predicates.add(likeIgnoreCase(builder, root.get("firstName"), filter.firstName()));
                }

                if (filter.hasFirstSurname()) {
                        predicates.add(likeIgnoreCase(builder, root.get("firstSurname"), filter.firstSurname()));
                }

                if (filter.hasEmail()) {
                        predicates.add(likeIgnoreCase(builder, root.get("email"), filter.email()));
                }

                if (filter.hasMobileNumber()) {
                        predicates.add(likeIgnoreCase(builder, root.get("mobileNumber"), filter.mobileNumber()));
                }

                final Predicate global = buildGlobalPredicate(filter, builder, root);
                if (global != null) {
                        predicates.add(global);
                }

                return predicates;
        }

        private Predicate buildGlobalPredicate(
                final UserFilter filter,
                final CriteriaBuilder builder,
                final Root<UserEntity> root) {
                if (!filter.hasGlobalQuery()) {
                        return null;
                }

                final List<Predicate> tokenPredicates = new ArrayList<>();
                for (final String token : filter.globalQueryTokens()) {
                        final List<Predicate> perToken = new ArrayList<>();
                        perToken.add(likeIgnoreCase(builder, root.get("firstName"), token));
                        perToken.add(likeIgnoreCase(builder, root.get("secondName"), token));
                        perToken.add(likeIgnoreCase(builder, root.get("firstSurname"), token));
                        perToken.add(likeIgnoreCase(builder, root.get("secondSurname"), token));
                        perToken.add(likeIgnoreCase(builder, root.get("idNumber"), token));
                        perToken.add(likeIgnoreCase(builder, root.get("email"), token));
                        perToken.add(likeIgnoreCase(builder, root.get("mobileNumber"), token));
                        perToken.add(likeIgnoreCase(builder, root.get("homeCity").get("name"), token));
                        perToken.add(likeIgnoreCase(builder, root.get("idType").get("name"), token));
                        tokenPredicates.add(builder.or(perToken.toArray(Predicate[]::new)));
                }

                if (tokenPredicates.isEmpty()) {
                        return null;
                }

                return builder.and(tokenPredicates.toArray(Predicate[]::new));
        }

        private Predicate likeIgnoreCase(
                final CriteriaBuilder builder,
                final Expression<String> expression,
                final String value) {
                final String pattern = containsPattern(value);
                return builder.like(builder.lower(expression), pattern);
        }

        private String containsPattern(final String value) {
                final String sanitized = value == null ? "" : value.toLowerCase(Locale.ROOT);
                return "%" + sanitized + "%";
        }
}
