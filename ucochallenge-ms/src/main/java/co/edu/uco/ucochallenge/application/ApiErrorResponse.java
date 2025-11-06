package co.edu.uco.ucochallenge.application;

import co.edu.uco.ucochallenge.crosscutting.dto.MessageDTO;
import co.edu.uco.ucochallenge.crosscutting.legacy.helper.TextHelper;

public final class ApiErrorResponse extends Response<ApiErrorResponse.ErrorData> {

        private ApiErrorResponse(final boolean returnData, final ErrorData data) {
                super(returnData, data);
        }

    public static ApiErrorResponse businessError(final String messageCode, final MessageDTO message) {
        return new ApiErrorResponse(true, ErrorData.from("BUSINESS_ERROR", messageCode, message));
    }

    public static ApiErrorResponse validationError(final String messageCode, final MessageDTO message) {
        return new ApiErrorResponse(true, ErrorData.from("VALIDATION_ERROR", messageCode, message));
    }

    public static ApiErrorResponse unexpectedError(final String messageCode, final MessageDTO message) {
        return new ApiErrorResponse(true, ErrorData.from("UNEXPECTED_ERROR", messageCode, message));
    }

    public record ErrorData(String category, String messageCode, String userMessage, String technicalMessage) {

        private static final String DEFAULT_USER_MESSAGE = "Ocurrió un error inesperado.";

        public ErrorData {
            category = TextHelper.getDefaultWithTrim(category);
            messageCode = TextHelper.getDefaultWithTrim(messageCode);
            userMessage = TextHelper.getDefaultWithTrim(userMessage);
            technicalMessage = TextHelper.getDefaultWithTrim(technicalMessage);
        }

        private static ErrorData from(final String category, final String messageCode, final MessageDTO dto) {
            final String resolvedUser = resolveUserMessage(dto, messageCode);
            final String resolvedTechnical = resolveTechnicalMessage(dto, messageCode, resolvedUser);
            return new ErrorData(category,
                    TextHelper.getDefaultWithTrim(messageCode),
                    resolvedUser,
                    resolvedTechnical);
        }

        private static String resolveUserMessage(final MessageDTO dto, final String messageCode) {
            if (dto != null) {
                final String candidate = TextHelper.getDefaultWithTrim(dto.getUserMessage());
                if (!candidate.isEmpty()) {
                    return candidate;
                }
                final String general = TextHelper.getDefaultWithTrim(dto.getGeneralMessage());
                if (!general.isEmpty()) {
                    return general;
                }
            }
            final String fallback = TextHelper.getDefaultWithTrim(messageCode);
            if (!fallback.isEmpty()) {
                return fallback;
            }
            return DEFAULT_USER_MESSAGE;
        }

        private static String resolveTechnicalMessage(final MessageDTO dto, final String messageCode, final String userMessage) {
            if (dto != null) {
                final String candidate = TextHelper.getDefaultWithTrim(dto.getTechnicalMessage());
                if (!candidate.isEmpty()) {
                    return candidate;
                }
            }
            final String fallback = TextHelper.getDefaultWithTrim(messageCode);
            if (!fallback.isEmpty()) {
                return fallback;
            }
            return userMessage;
        }
    }
}
