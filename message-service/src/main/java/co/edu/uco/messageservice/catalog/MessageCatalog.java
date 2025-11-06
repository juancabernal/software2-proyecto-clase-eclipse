package co.edu.uco.messageservice.catalog;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MessageCatalog {

    private static final Map<String, MessageEntry> MESSAGES = new ConcurrentHashMap<>();

    static {
        register("register.user.validation.contact.required",
                "Debes registrar al menos un correo electrónico o un número móvil para continuar.",
                "User registration without email and mobileNumber. Validate contact information before invoking the API.",
                "Contact information is required.");
        register("register.user.validation.idtype.required",
                "Selecciona un tipo de identificación válido.",
                "idType/idTypeName was missing or not found in catalog during registration.",
                "Identification type is required.");
        register("register.user.validation.country.required",
                "Debes elegir un país válido.",
                "countryId is null or does not exist in the locations catalog.",
                "Country is required.");
        register("register.user.validation.department.required",
                "Debes elegir un departamento válido.",
                "departmentId is null or does not exist in the locations catalog.",
                "Department is required.");
        register("register.user.validation.city.required",
                "Debes elegir una ciudad de residencia válida.",
                "homeCity is null or does not exist in the locations catalog.",
                "City is required.");
        register("register.user.validation.idnumber.required",
                "El número de identificación es obligatorio.",
                "idNumber value is empty or missing in registration payload.",
                "Identification number is required.");
        register("register.user.validation.firstname.required",
                "El primer nombre es obligatorio.",
                "firstName value is empty or missing in registration payload.",
                "First name is required.");
        register("register.user.validation.lastname.required",
                "El primer apellido es obligatorio.",
                "firstSurname value is empty or missing in registration payload.",
                "First surname is required.");
        register("register.user.validation.email.required",
                "El correo electrónico es obligatorio.",
                "email value is empty or missing in registration payload.",
                "Email is required.");
        register("register.user.validation.email.invalid",
                "El correo electrónico no tiene un formato válido.",
                "Email field failed format validation (Jakarta @Email constraint).",
                "Email format is invalid.");
        register("register.user.duplicated",
                "Ya existe un usuario con los datos suministrados. Verifica la información e inténtalo de nuevo.",
                "UserRegistrationDomainValidator reported duplicated data. Review notification errors for details.",
                "User already exists.");
        register("register.user.identifier.unavailable",
                "No fue posible generar un identificador único para el usuario. Intenta nuevamente.",
                "UUID generator exhausted retries while trying to assign a unique user id.",
                "Unable to generate user identifier.");
        register("REGISTER_USER_EMAIL_DUPLICATED",
                "El correo ingresado ya está asociado a otra cuenta.",
                "Existing user found for provided email during uniqueness validation.",
                "Email already registered.");
        register("REGISTER_USER_IDENTIFICATION_DUPLICATED",
                "Ya existe un usuario con el mismo tipo y número de identificación.",
                "Combination of idType and idNumber already exists in the system.",
                "Identification already registered.");
        register("REGISTER_USER_MOBILE_DUPLICATED",
                "El número móvil ya se encuentra registrado.",
                "Existing user found for provided mobile number during uniqueness validation.",
                "Mobile number already registered.");
        register("verification.code.notfound",
                "El código de verificación no existe o ya fue utilizado.",
                "No verification code persisted for the normalized contact.",
                "Verification code not found.");
        register("verification.code.max.attempts",
                "Superaste el número máximo de intentos. Solicita un nuevo código.",
                "Attempts counter reached the configured maximum for the contact.",
                "Maximum attempts exceeded.");
        register("verification.code.expired",
                "El código de verificación expiró. Solicita uno nuevo.",
                "Verification code expiration timestamp is in the past.",
                "Verification code expired.");
        register("verification.code.invalid",
                "El código ingresado es incorrecto.",
                "Provided verification code does not match the stored value.",
                "Verification code is invalid.");
        register("verification.code.format.invalid",
                "El código debe contener exactamente 6 dígitos numéricos.",
                "Provided verification code does not match the expected ^\\d{6}$ pattern.",
                "Verification code format is invalid.");
        register("verification.user.notfound",
                "No encontramos el usuario solicitado.",
                "User repository returned no results for the provided identifier.",
                "User not found.");
        register("verification.email.missing",
                "El usuario no tiene un correo electrónico configurado.",
                "User record lacks an email address when sending verification.",
                "User email missing.");
        register("verification.mobile.missing",
                "El usuario no tiene un número móvil configurado.",
                "User record lacks a mobile number when sending verification.",
                "User mobile missing.");
        register("verification.channel.invalid",
                "El canal de verificación no es válido.",
                "Unsupported verification channel received; expected 'email' or 'mobile'.",
                "Invalid verification channel.");
        register("notification.delivery.failure",
                "No pudimos enviar el código de verificación. Inténtalo nuevamente en unos minutos.",
                "Notification provider returned an error while sending the verification message.",
                "Unable to deliver verification message.");
        register("user.search.filters.invalid",
                "Los filtros de búsqueda son inválidos. Revisa los valores ingresados.",
                "Domain validation detected invalid pagination filters for the search use case.",
                "Invalid search filters.");
        register("FIND_USERS_PAGE_NEGATIVE",
                "El número de página debe ser mayor o igual a cero.",
                "The requested page index was negative.",
                "Page number must be greater or equal to zero.");
        register("FIND_USERS_SIZE_RANGE",
                "El tamaño de página debe estar entre 1 y 100 registros.",
                "The requested page size is outside the allowed [1,100] range.",
                "Page size out of range.");
        register("validation.constraint.violation",
                "Algunos datos no cumplen las validaciones requeridas.",
                "Jakarta ConstraintViolationException triggered while validating request payload.",
                "Constraint violation detected.");
        register("validation.method.argument",
                "Existen campos con formato inválido en la solicitud.",
                "Spring MethodArgumentNotValidException occurred due to binding errors.",
                "Invalid method argument.");
        register("exception.general.technical",
                "Se presentó un problema técnico al procesar tu solicitud.",
                "A technical failure occurred while interacting with infrastructure resources (database, cache, etc.).",
                "Technical error.");
        register("exception.general.validation",
                "Algunos datos relacionados no fueron encontrados.",
                "Related entity required for the operation could not be located.",
                "Related entity missing.");
        register("exception.general.unexpected",
                "Ocurrió un error inesperado. Por favor intenta nuevamente más tarde.",
                "Unhandled exception reached the global handler. Review logs for stacktrace.",
                "Unexpected error.");
    }

    private MessageCatalog() {
        // utilidad
    }

    public static Map<String, MessageEntry> getAllMessages() {
        return Collections.unmodifiableMap(MESSAGES);
    }

    public static MessageEntry getMessage(String code) {
        return MESSAGES.get(normalize(code));
    }

    public static MessageEntry upsertMessage(String code, MessageEntry entry) {
        Objects.requireNonNull(entry, "entry");
        String normalized = normalize(code);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("El código del mensaje es obligatorio");
        }
        MessageEntry sanitized = new MessageEntry(normalized,
                entry.getUserMessage(),
                entry.getTechnicalMessage(),
                entry.getGeneralMessage());
        MESSAGES.put(normalized, sanitized);
        return sanitized;
    }

    public static MessageEntry removeMessage(String code) {
        return MESSAGES.remove(normalize(code));
    }

    private static void register(String code, String userMessage, String technicalMessage, String generalMessage) {
        MessageEntry entry = new MessageEntry(code, userMessage, technicalMessage, generalMessage);
        MESSAGES.put(code, entry);
    }

    private static String normalize(String code) {
        return code == null ? null : code.trim();
    }
}
