package co.edu.uco.ucochallenge.application.user.registerUser.usecase.impl;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.user.registerUser.usecase.RegisterUserUseCase;
import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.domain.ai.port.out.UserIntelligencePort;
import co.edu.uco.ucochallenge.domain.user.model.User;
import co.edu.uco.ucochallenge.domain.user.port.out.UserRepository;

@Service
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

        private final UserRepository repository;
        private final UserIntelligencePort intelligencePort;

        public RegisterUserUseCaseImpl(final UserRepository repository, final UserIntelligencePort intelligencePort) {
                this.repository = repository;
                this.intelligencePort = intelligencePort;
        }

        @Override
        public User execute(final User domain) {
                validateUniqueness(domain);
                final User savedUser = repository.save(domain);
                intelligencePort.publishUserRegistrationInsight(savedUser);
                return savedUser;
        }

        private void validateUniqueness(final User domain) {
                if (repository.existsByEmail(domain.email())) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.EMAIL_ALREADY_REGISTERED_TECHNICAL,
                                        MessageCodes.Domain.User.EMAIL_ALREADY_REGISTERED_USER);
                }

                if (repository.existsByIdTypeAndIdNumber(domain.idType(), domain.idNumber())) {
                        throw DomainException.buildFromCatalog(
                                        MessageCodes.Domain.User.ID_NUMBER_ALREADY_REGISTERED_TECHNICAL,
                                        MessageCodes.Domain.User.ID_NUMBER_ALREADY_REGISTERED_USER);
                }

                if (repository.existsByMobileNumber(domain.mobileNumber())) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.MOBILE_ALREADY_REGISTERED_TECHNICAL,
                                        MessageCodes.Domain.User.MOBILE_ALREADY_REGISTERED_USER);
                }
        }
}
