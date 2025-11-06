package co.edu.uco.ucochallenge.application.user.search.dto;

public record UserSearchQueryRequestDTO(Integer page, Integer size) {

        public static UserSearchQueryRequestDTO normalize(final Integer page, final Integer size) {
                final int sanitizedPage = page == null || page < 0 ? 0 : page;
                final int sanitizedSize = size == null || size <= 0 ? 10 : size;
                return new UserSearchQueryRequestDTO(sanitizedPage, sanitizedSize);
        }
}
