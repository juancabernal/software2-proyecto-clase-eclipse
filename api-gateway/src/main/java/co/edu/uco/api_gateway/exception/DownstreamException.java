package co.edu.uco.api_gateway.exception;

import org.springframework.http.HttpStatusCode;

import co.edu.uco.api_gateway.dto.ApiErrorResponse;

public class DownstreamException extends RuntimeException {

    private final HttpStatusCode status;
    private final ApiErrorResponse error;

    public DownstreamException(final HttpStatusCode status, final ApiErrorResponse error) {
        super(error != null ? error.technicalMessage() : status != null ? status.toString() : "DOWNSTREAM_ERROR");
        this.status = status;
        this.error = error;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public ApiErrorResponse getError() {
        return error;
    }
}
