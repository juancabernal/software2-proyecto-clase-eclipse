package co.edu.uco.ucochallenge.infrastructure.primary.controller.response;

public record ApiSuccessResponse<T>(String userMessage, T data) {

        public static <T> ApiSuccessResponse<T> of(final String userMessage, final T data) {
                return new ApiSuccessResponse<>(userMessage, data);
        }
}
