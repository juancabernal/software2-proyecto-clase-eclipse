package co.edu.uco.ucochallenge.application.user.updateUser.usecase.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.user.updateUser.usecase.UpdateUserUseCase;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;

@Service
public class UpdateUserUseCaseImpl implements UpdateUserUseCase {

        private final UserRepository repository;

        public UpdateUserUseCaseImpl(final UserRepository repository) {
                this.repository = repository;
        }

        @Override
        public User execute(final User changes) {
                final User current = repository.findById(changes.id())
                                .orElseThrow(() -> DomainException.buildFromCatalog(
                                                MessageCodes.Domain.User.NOT_FOUND_TECHNICAL,
                                                MessageCodes.Domain.User.NOT_FOUND_USER));

                validateUniqueness(changes);

                final User userToSave = new User(
                                changes.id(),
                                changes.idType(),
                                changes.idNumber(),
                                changes.firstName(),
                                changes.secondName(),
                                changes.firstSurname(),
                                changes.secondSurname(),
                                changes.homeCity(),
                                changes.email(),
                                changes.mobileNumber(),
                                current.emailConfirmed(),
                                current.mobileNumberConfirmed());

                return repository.save(userToSave);
        }

        private void validateUniqueness(final User changes) {
                final UUID userId = changes.id();

                if (repository.existsByEmailExcludingId(userId, changes.email())) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.EMAIL_ALREADY_REGISTERED_TECHNICAL,
                                        MessageCodes.Domain.User.EMAIL_ALREADY_REGISTERED_USER);
                }

                if (repository.existsByIdTypeAndIdNumberExcludingId(userId, changes.idType(), changes.idNumber())) {
                        throw DomainException.buildFromCatalog(
                                        MessageCodes.Domain.User.ID_NUMBER_ALREADY_REGISTERED_TECHNICAL,
                                        MessageCodes.Domain.User.ID_NUMBER_ALREADY_REGISTERED_USER);
                }

                if (repository.existsByMobileNumberExcludingId(userId, changes.mobileNumber())) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.MOBILE_ALREADY_REGISTERED_TECHNICAL,
                                        MessageCodes.Domain.User.MOBILE_ALREADY_REGISTERED_USER);
                }
        }
}
