package co.edu.uco.ucochallenge.crosscutting.legacy.exception;

public class NotFoundException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final String code;

        public NotFoundException(final String code) {
                super(code);
                this.code = code;
        }

        public NotFoundException(final String code, final Throwable cause) {
                super(code, cause);
                this.code = code;
        }

        public String getCode() {
                return code;
        }
}
