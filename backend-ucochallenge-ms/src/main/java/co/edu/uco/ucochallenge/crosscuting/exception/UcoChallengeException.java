package co.edu.uco.ucochallenge.crosscuting.exception;

import java.util.Collections;
import java.util.Map;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageProvider;

public class UcoChallengeException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final String technicalMessage;
        private final String userMessage;

        protected UcoChallengeException(final String technicalMessage, final String userMessage, final Throwable cause) {
                super(TextHelper.getDefault(technicalMessage), cause);
                this.technicalMessage = TextHelper.getDefault(technicalMessage);
                this.userMessage = TextHelper.getDefault(userMessage);
        }

        public static UcoChallengeException build(final String technicalMessage, final String userMessage, final Throwable cause) {
                return new UcoChallengeException(technicalMessage, userMessage, cause);
        }

        public static UcoChallengeException build(final String technicalMessage, final String userMessage) {
                return new UcoChallengeException(technicalMessage, userMessage, null);
        }

        public static UcoChallengeException build(final String message) {
                return new UcoChallengeException(message, message, null);
        }

        public static UcoChallengeException buildFromCatalog(final String technicalCode, final String userCode,
                        final Map<String, String> parameters, final Throwable cause) {
                final String technicalMessage = MessageProvider.getMessage(technicalCode, parameters);
                final String userMessage = MessageProvider.getMessage(userCode, parameters);
                return new UcoChallengeException(technicalMessage, userMessage, cause);
        }

        public static UcoChallengeException buildFromCatalog(final String technicalCode, final String userCode,
                        final Map<String, String> parameters) {
                return buildFromCatalog(technicalCode, userCode, parameters, null);
        }

        public static UcoChallengeException buildFromCatalog(final String technicalCode, final String userCode) {
                return buildFromCatalog(technicalCode, userCode, Collections.emptyMap(), null);
        }

        public static UcoChallengeException buildFromCatalog(final String messageCode) {
                final String message = MessageProvider.getMessage(messageCode);
                return new UcoChallengeException(message, message, null);
        }

        public String getTechnicalMessage() {
                return technicalMessage;
        }

        public String getUserMessage() {
                return userMessage;
        }
}
