package co.edu.uco.ucochallenge.crosscuting.exception;

import java.util.Collections;
import java.util.Map;

import co.edu.uco.ucochallenge.crosscuting.messages.MessageProvider;

public final class ApplicationException extends UcoChallengeException {

        private static final long serialVersionUID = 1L;

        private ApplicationException(final String technicalMessage, final String userMessage, final Throwable cause) {
                super(technicalMessage, userMessage, cause);
        }

        public static ApplicationException build(final String technicalMessage, final String userMessage,
                        final Throwable cause) {
                return new ApplicationException(technicalMessage, userMessage, cause);
        }

        public static ApplicationException build(final String technicalMessage, final String userMessage) {
                return new ApplicationException(technicalMessage, userMessage, null);
        }

        public static ApplicationException build(final String message) {
                return new ApplicationException(message, message, null);
        }

        public static ApplicationException buildFromCatalog(final String technicalCode, final String userCode,
                        final Map<String, String> parameters, final Throwable cause) {
                final String technicalMessage = MessageProvider.getMessage(technicalCode, parameters);
                final String userMessage = MessageProvider.getMessage(userCode, parameters);
                return new ApplicationException(technicalMessage, userMessage, cause);
        }

        public static ApplicationException buildFromCatalog(final String technicalCode, final String userCode,
                        final Map<String, String> parameters) {
                return buildFromCatalog(technicalCode, userCode, parameters, null);
        }

        public static ApplicationException buildFromCatalog(final String technicalCode, final String userCode) {
                return buildFromCatalog(technicalCode, userCode, Collections.emptyMap(), null);
        }

        public static ApplicationException buildFromCatalog(final String messageCode) {
                final String message = MessageProvider.getMessage(messageCode);
                return new ApplicationException(message, message, null);
        }
}
