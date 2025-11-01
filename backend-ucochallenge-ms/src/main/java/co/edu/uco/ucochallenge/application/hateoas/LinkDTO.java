package co.edu.uco.ucochallenge.application.hateoas;

public record LinkDTO(String rel, String href, String method) {

        public static LinkDTO of(final String rel, final String href, final String method) {
                return new LinkDTO(rel, href, method);
        }
}
