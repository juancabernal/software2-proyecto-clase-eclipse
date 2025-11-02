package co.edu.uco.api_gateway.dto;

public record ApiSuccessResponse<T>(String userMessage, T data) {
}
