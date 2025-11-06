package co.edu.uco.ucochallenge.application.user.search.usecase;

import org.springframework.stereotype.Service;

import co.edu.uco.ucochallenge.application.user.search.port.UserSearchQueryRepositoryPort;
import co.edu.uco.ucochallenge.application.user.search.validator.UserSearchQueryValidator;
import co.edu.uco.ucochallenge.crosscuting.exception.BusinessException;
import co.edu.uco.ucochallenge.crosscuting.notification.Notification;
import co.edu.uco.ucochallenge.domain.user.search.model.UserSearchFilterDomainModel;
import co.edu.uco.ucochallenge.domain.user.search.model.UserSearchResultDomainModel;

@Service
public class UserSearchQueryService implements UserSearchQueryUseCase {

        private final UserSearchQueryRepositoryPort repositoryPort;
        private final UserSearchQueryValidator validator;

        public UserSearchQueryService(final UserSearchQueryRepositoryPort repositoryPort) {
                this.repositoryPort = repositoryPort;
                this.validator = new UserSearchQueryValidator();
        }

        @Override
        public UserSearchResultDomainModel execute(final UserSearchFilterDomainModel filter) {
                final Notification notification = validator.validate(filter);
                if (notification.hasErrors()) {
                        throw new BusinessException(notification.formattedMessages());
                }

                return repositoryPort.findAll(filter.getPage(), filter.getSize());
        }
}
