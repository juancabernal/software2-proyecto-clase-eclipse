package co.edu.uco.ucochallenge.crosscuting.messages;

public final class MessageCodes {

    private MessageCodes() {
    }

    public static final class Application {
        public static final String UNEXPECTED_ERROR_TECHNICAL = "application.unexpectedError.technical";
        public static final String UNEXPECTED_ERROR_USER = "application.unexpectedError.user";

        private Application() {
        }
    }

    public static final class Infrastructure {

        private Infrastructure() {
        }

        public static final class MessageService {
            public static final String UNAVAILABLE_TECHNICAL =
                    "infrastructure.messageService.unavailable.technical";
            public static final String UNAVAILABLE_USER =
                    "infrastructure.messageService.unavailable.user";

            private MessageService() {
            }
        }

        public static final class ParameterService {
            public static final String UNAVAILABLE_TECHNICAL =
                    "infrastructure.parameterService.unavailable.technical";
            public static final String UNAVAILABLE_USER =
                    "infrastructure.parameterService.unavailable.user";
            public static final String INVALID_RESPONSE_TECHNICAL =
                    "infrastructure.parameterService.invalidResponse.technical";
            public static final String INVALID_RESPONSE_USER =
                    "infrastructure.parameterService.invalidResponse.user";

            private ParameterService() {
            }
        }
    }

    public static final class Domain {

        private Domain() {
        }

        public static final class User {
            public static final String ID_TYPE_MANDATORY_TECHNICAL = "domain.user.idType.mandatory.technical";
            public static final String ID_TYPE_MANDATORY_USER = "domain.user.idType.mandatory.user";
            public static final String ID_NUMBER_EMPTY_TECHNICAL = "domain.user.idNumber.empty.technical";
            public static final String ID_NUMBER_EMPTY_USER = "domain.user.idNumber.empty.user";
            public static final String ID_NUMBER_INVALID_CHARS_TECHNICAL =
                    "domain.user.idNumber.invalidChars.technical";
            public static final String ID_NUMBER_INVALID_CHARS_USER =
                    "domain.user.idNumber.invalidChars.user";
            public static final String ID_NUMBER_LENGTH_TECHNICAL = "domain.user.idNumber.length.technical";
            public static final String ID_NUMBER_LENGTH_USER = "domain.user.idNumber.length.user";
            public static final String MANDATORY_FIELD_TECHNICAL = "domain.user.field.mandatory.technical";
            public static final String MANDATORY_FIELD_USER = "domain.user.field.mandatory.user";
            public static final String FIELD_INVALID_CHARS_TECHNICAL = "domain.user.field.invalidChars.technical";
            public static final String FIELD_INVALID_CHARS_USER = "domain.user.field.invalidChars.user";
            public static final String FIELD_LENGTH_TECHNICAL = "domain.user.field.length.technical";
            public static final String FIELD_LENGTH_USER = "domain.user.field.length.user";
            public static final String HOME_CITY_MANDATORY_TECHNICAL = "domain.user.homeCity.mandatory.technical";
            public static final String HOME_CITY_MANDATORY_USER = "domain.user.homeCity.mandatory.user";
            public static final String EMAIL_EMPTY_TECHNICAL = "domain.user.email.empty.technical";
            public static final String EMAIL_EMPTY_USER = "domain.user.email.empty.user";
            public static final String EMAIL_LENGTH_TECHNICAL = "domain.user.email.length.technical";
            public static final String EMAIL_LENGTH_USER = "domain.user.email.length.user";
            public static final String EMAIL_FORMAT_INVALID_TECHNICAL =
                    "domain.user.email.invalidFormat.technical";
            public static final String EMAIL_FORMAT_INVALID_USER = "domain.user.email.invalidFormat.user";
            public static final String MOBILE_EMPTY_TECHNICAL = "domain.user.mobile.empty.technical";
            public static final String MOBILE_EMPTY_USER = "domain.user.mobile.empty.user";
            public static final String MOBILE_FORMAT_INVALID_TECHNICAL =
                    "domain.user.mobile.invalidFormat.technical";
            public static final String MOBILE_FORMAT_INVALID_USER = "domain.user.mobile.invalidFormat.user";
            public static final String NOT_FOUND_TECHNICAL = "domain.user.notFound.technical";
            public static final String NOT_FOUND_USER = "domain.user.notFound.user";
            public static final String EMAIL_ALREADY_REGISTERED_TECHNICAL =
                    "domain.user.email.alreadyRegistered.technical";
            public static final String EMAIL_ALREADY_REGISTERED_USER =
                    "domain.user.email.alreadyRegistered.user";
            public static final String ID_NUMBER_ALREADY_REGISTERED_TECHNICAL =
                    "domain.user.idNumber.alreadyRegistered.technical";
            public static final String ID_NUMBER_ALREADY_REGISTERED_USER =
                    "domain.user.idNumber.alreadyRegistered.user";
            public static final String MOBILE_ALREADY_REGISTERED_TECHNICAL =
                    "domain.user.mobile.alreadyRegistered.technical";
            public static final String MOBILE_ALREADY_REGISTERED_USER =
                    "domain.user.mobile.alreadyRegistered.user";
            public static final String ID_TYPE_NOT_FOUND_TECHNICAL = "domain.user.idType.notFound.technical";
            public static final String ID_TYPE_NOT_FOUND_USER = "domain.user.idType.notFound.user";
            public static final String HOME_CITY_NOT_FOUND_TECHNICAL = "domain.user.homeCity.notFound.technical";
            public static final String HOME_CITY_NOT_FOUND_USER = "domain.user.homeCity.notFound.user";

            private User() {
            }
        }
    }
}
