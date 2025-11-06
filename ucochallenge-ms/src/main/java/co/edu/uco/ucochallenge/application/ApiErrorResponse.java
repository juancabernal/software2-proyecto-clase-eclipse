package co.edu.uco.ucochallenge.application;

import co.edu.uco.ucochallenge.crosscutting.legacy.helper.TextHelper;

public final class ApiErrorResponse extends Response<ApiErrorResponse.ErrorData> {

        private ApiErrorResponse(final ErrorData data) {
                super(true, data);
        }

        public static ApiErrorResponse businessError(
                        final String messageCode,
                        final String userMessage,
                        final String technicalMessage,
                        final String generalMessage) {
                return create("BUSINESS_ERROR", messageCode, userMessage, technicalMessage, generalMessage);
        }

        public static ApiErrorResponse validationError(
                        final String messageCode,
                        final String userMessage,
                        final String technicalMessage,
                        final String generalMessage) {
                return create("VALIDATION_ERROR", messageCode, userMessage, technicalMessage, generalMessage);
        }

        public static ApiErrorResponse unexpectedError(
                        final String messageCode,
                        final String userMessage,
                        final String technicalMessage,
                        final String generalMessage) {
                return create("UNEXPECTED_ERROR", messageCode, userMessage, technicalMessage, generalMessage);
        }

        private static ApiErrorResponse create(
                        final String errorType,
                        final String messageCode,
                        final String userMessage,
                        final String technicalMessage,
                        final String generalMessage) {
                return new ApiErrorResponse(new ErrorData(
                                errorType,
                                messageCode,
                                userMessage,
                                technicalMessage,
                                generalMessage,
                                userMessage));
        }

        public record ErrorData(
                        String code,
                        String messageCode,
                        String userMessage,
                        String technicalMessage,
                        String generalMessage,
                        String message) {

                public ErrorData {
                        code = TextHelper.getDefaultWithTrim(code);
                        messageCode = TextHelper.getDefaultWithTrim(messageCode);
                        userMessage = TextHelper.getDefaultWithTrim(userMessage);
                        technicalMessage = TextHelper.getDefaultWithTrim(technicalMessage);
                        generalMessage = TextHelper.getDefaultWithTrim(generalMessage);
                        message = TextHelper.getDefaultWithTrim(message);
                }
        }
}
