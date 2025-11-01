package co.edu.uco.ucochallenge.crosscuting.exception;

import java.util.Collections;
import java.util.Map;

import co.edu.uco.ucochallenge.crosscuting.messages.MessageProvider;

public final class DomainException extends UcoChallengeException {

        private static final long serialVersionUID = 1L;

        private DomainException(final String technicalMessage, final String userMessage, final Throwable cause) {
                super(technicalMessage, userMessage, cause);
        }

        public static DomainException build(final String technicalMessage, final String userMessage, final Throwable cause) {
                return new DomainException(technicalMessage, userMessage, cause);
        }

        public static DomainException build(final String technicalMessage, final String userMessage) {
                return new DomainException(technicalMessage, userMessage, null);
        }

        public static DomainException build(final String message) {
                return new DomainException(message, message, null);
        }

        public static DomainException buildFromCatalog(final String technicalCode, final String userCode,
                        final Map<String, String> parameters, final Throwable cause) {
                final String technicalMessage = MessageProvider.getMessage(technicalCode, parameters);
                final String userMessage = MessageProvider.getMessage(userCode, parameters);
                return new DomainException(technicalMessage, userMessage, cause);
        }

        public static DomainException buildFromCatalog(final String technicalCode, final String userCode,
                        final Map<String, String> parameters) {
                return buildFromCatalog(technicalCode, userCode, parameters, null);
        }

        public static DomainException buildFromCatalog(final String technicalCode, final String userCode) {
                return buildFromCatalog(technicalCode, userCode, Collections.emptyMap(), null);
        }

        public static DomainException buildFromCatalog(final String messageCode) {
                final String message = MessageProvider.getMessage(messageCode);
                return new DomainException(message, message, null);
        }
}
