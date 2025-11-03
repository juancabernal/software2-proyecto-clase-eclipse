package co.edu.uco.api_gateway.dto;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        int status,
        String userMessage,
        String technicalMessage,
        OffsetDateTime timestamp) {

    public static ApiErrorResponse of(final int status, final String userMessage, final String technicalMessage) {
        return new ApiErrorResponse(status, userMessage, technicalMessage, OffsetDateTime.now());
    }
}
