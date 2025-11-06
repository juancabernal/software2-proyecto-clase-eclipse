package co.edu.uco.ucochallenge.crosscutting;

/**
 * Catálogo centralizado de los códigos de mensaje utilizados por el
 * microservicio principal. Mantener estos valores en un único lugar evita
 * discrepancias al consultar el {@code messages-service}.
 */
public final class MessageCodes {

    // =========================================================
    // ⚙️ EXCEPCIONES GENERALES
    // =========================================================
    public static final String EXCEPTION_GENERAL_UNEXPECTED = "exception.general.unexpected";
    public static final String EXCEPTION_GENERAL_TECHNICAL = "exception.general.technical";
    public static final String EXCEPTION_GENERAL_VALIDATION = "exception.general.validation";

    // =========================================================
    // 👤 REGISTRO DE USUARIOS
    // =========================================================
    public static final String REGISTER_USER_DUPLICATED = "register.user.duplicated";
    public static final String REGISTER_USER_IDENTIFIER_UNAVAILABLE = "register.user.identifier.unavailable";
    public static final String REGISTER_USER_VALIDATION_CONTACT_REQUIRED = "register.user.validation.contact.required";
    public static final String REGISTER_USER_VALIDATION_IDTYPE_REQUIRED = "register.user.validation.idtype.required";
    public static final String REGISTER_USER_VALIDATION_IDNUMBER_REQUIRED = "register.user.validation.idnumber.required";
    public static final String REGISTER_USER_VALIDATION_FIRSTNAME_REQUIRED = "register.user.validation.firstname.required";
    public static final String REGISTER_USER_VALIDATION_LASTNAME_REQUIRED = "register.user.validation.lastname.required";
    public static final String REGISTER_USER_VALIDATION_EMAIL_INVALID = "register.user.validation.email.invalid";
    public static final String REGISTER_USER_VALIDATION_PHONE_INVALID = "register.user.validation.phone.invalid";
    public static final String REGISTER_USER_VALIDATION_COUNTRY_REQUIRED = "register.user.validation.country.required";
    public static final String REGISTER_USER_VALIDATION_DEPARTMENT_REQUIRED = "register.user.validation.department.required";
    public static final String REGISTER_USER_VALIDATION_CITY_REQUIRED = "register.user.validation.city.required";

    // =========================================================
    // 🔍 BÚSQUEDA DE USUARIOS
    // =========================================================
    public static final String FIND_USERS_PAGE_NEGATIVE = "find.users.page.negative";
    public static final String FIND_USERS_SIZE_RANGE = "find.users.size.range";

    // =========================================================
    // ✅ CONFIRMACIÓN DE CONTACTO
    // =========================================================
    public static final String VERIFICATION_CHANNEL_REQUIRED = "verification.channel.required";
    public static final String VERIFICATION_CHANNEL_INVALID = "verification.channel.invalid";
    public static final String VERIFICATION_CODE_REQUIRED = "verification.code.required";
    public static final String VERIFICATION_CODE_NOT_FOUND = "verification.code.notfound";
    public static final String VERIFICATION_CODE_MAX_ATTEMPTS = "verification.code.max.attempts";
    public static final String VERIFICATION_CODE_EXPIRED = "verification.code.expired";
    public static final String VERIFICATION_CODE_INVALID = "verification.code.invalid";
    public static final String VERIFICATION_CODE_FORMAT_INVALID = "verification.code.format.invalid";
    public static final String VERIFICATION_USER_NOT_FOUND = "verification.user.notfound";
    public static final String VERIFICATION_CONTACT_EMAIL_MISSING = "verification.contact.email.missing";
    public static final String VERIFICATION_CONTACT_MOBILE_MISSING = "verification.contact.mobile.missing";
    public static final String VERIFICATION_NOTIFICATION_DELIVERY_FAILED = "verification.notification.delivery.failed";

    private MessageCodes() {
        // evitar instanciación
    }
}
