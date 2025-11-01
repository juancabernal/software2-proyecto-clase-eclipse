package co.edu.uco.ucochallenge.domain.user.model;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import co.edu.uco.ucochallenge.crosscuting.exception.DomainException;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import co.edu.uco.ucochallenge.crosscuting.messages.MessageCodes;
import co.edu.uco.ucochallenge.crosscuting.parameter.ParameterCodes;
import co.edu.uco.ucochallenge.crosscuting.parameter.ParameterProvider;

public record User(
                UUID id,
                UUID idType,
                String idNumber,
                String firstName,
                String secondName,
                String firstSurname,
                String secondSurname,
                UUID homeCity,
                String email,
                String mobileNumber,
                boolean emailConfirmed,
                boolean mobileNumberConfirmed) {

        public User {
                id = normalizeId(id);
                idType = validateIdType(idType);
                idNumber = validateIdNumber(idNumber);
                firstName = validateMandatoryName(firstName, "primer nombre");
                secondName = validateOptionalName(secondName, "segundo nombre");
                firstSurname = validateMandatoryName(firstSurname, "primer apellido");
                secondSurname = validateOptionalName(secondSurname, "segundo apellido");
                homeCity = validateHomeCity(homeCity);
                email = validateEmail(email);
                mobileNumber = validateMobileNumber(mobileNumber);
        }

        private static UUID normalizeId(final UUID id) {
                final UUID normalized = UUIDHelper.getDefault(id);
                if (UUIDHelper.getDefault().equals(normalized)) {
                        return UUID.randomUUID();
                }
                return normalized;
        }

        private static UUID validateIdType(final UUID idType) {
                final UUID normalized = UUIDHelper.getDefault(idType);
                if (UUIDHelper.getDefault().equals(normalized)) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.ID_TYPE_MANDATORY_TECHNICAL,
                                        MessageCodes.Domain.User.ID_TYPE_MANDATORY_USER);
                }
                return normalized;
        }

        private static String validateIdNumber(final String idNumber) {
                final String normalized = TextHelper.getDefaultWithTrim(idNumber);
                if (TextHelper.isEmpty(normalized)) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.ID_NUMBER_EMPTY_TECHNICAL,
                                        MessageCodes.Domain.User.ID_NUMBER_EMPTY_USER);
                }
                if (!getIdNumberPattern().matcher(normalized).matches()) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.ID_NUMBER_INVALID_CHARS_TECHNICAL,
                                        MessageCodes.Domain.User.ID_NUMBER_INVALID_CHARS_USER);
                }
                final int minLength = ParameterProvider.getInteger(ParameterCodes.User.ID_NUMBER_MIN_LENGTH);
                final int maxLength = ParameterProvider.getInteger(ParameterCodes.User.ID_NUMBER_MAX_LENGTH);
                if (normalized.length() < minLength || normalized.length() > maxLength) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.ID_NUMBER_LENGTH_TECHNICAL,
                                        MessageCodes.Domain.User.ID_NUMBER_LENGTH_USER,
                                        Map.of("minLength", String.valueOf(minLength),
                                                        "maxLength", String.valueOf(maxLength)));
                }
                return normalized;
        }

        private static String validateMandatoryName(final String name, final String fieldName) {
                final String normalized = TextHelper.getDefaultWithTrim(name);
                if (TextHelper.isEmpty(normalized)) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.MANDATORY_FIELD_TECHNICAL,
                                        MessageCodes.Domain.User.MANDATORY_FIELD_USER,
                                        Map.of("fieldName", fieldName));
                }
                validateNameFormat(normalized, fieldName);
                return normalized;
        }

        private static String validateOptionalName(final String name, final String fieldName) {
                final String normalized = TextHelper.getDefaultWithTrim(name);
                if (TextHelper.isEmpty(normalized)) {
                        return TextHelper.getDefault();
                }
                validateNameFormat(normalized, fieldName);
                return normalized;
        }

        private static void validateNameFormat(final String value, final String fieldName) {
                if (!getNamePattern().matcher(value).matches()) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.FIELD_INVALID_CHARS_TECHNICAL,
                                        MessageCodes.Domain.User.FIELD_INVALID_CHARS_USER,
                                        Map.of("fieldName", fieldName));
                }
                final int minLength = ParameterProvider.getInteger(ParameterCodes.User.NAME_MIN_LENGTH);
                final int maxLength = ParameterProvider.getInteger(ParameterCodes.User.NAME_MAX_LENGTH);
                if (value.length() < minLength || value.length() > maxLength) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.FIELD_LENGTH_TECHNICAL,
                                        MessageCodes.Domain.User.FIELD_LENGTH_USER,
                                        Map.of("fieldName", fieldName, "minLength", String.valueOf(minLength),
                                                        "maxLength", String.valueOf(maxLength)));
                }
        }

        private static UUID validateHomeCity(final UUID homeCity) {
                final UUID normalized = UUIDHelper.getDefault(homeCity);
                if (UUIDHelper.getDefault().equals(normalized)) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.HOME_CITY_MANDATORY_TECHNICAL,
                                        MessageCodes.Domain.User.HOME_CITY_MANDATORY_USER);
                }
                return normalized;
        }

        private static String validateEmail(final String email) {
                final String normalized = TextHelper.getDefaultWithTrim(email).toLowerCase();
                if (TextHelper.isEmpty(normalized)) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.EMAIL_EMPTY_TECHNICAL,
                                        MessageCodes.Domain.User.EMAIL_EMPTY_USER);
                }
                final int minLength = ParameterProvider.getInteger(ParameterCodes.User.EMAIL_MIN_LENGTH);
                final int maxLength = ParameterProvider.getInteger(ParameterCodes.User.EMAIL_MAX_LENGTH);
                if (normalized.length() < minLength || normalized.length() > maxLength) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.EMAIL_LENGTH_TECHNICAL,
                                        MessageCodes.Domain.User.EMAIL_LENGTH_USER,
                                        Map.of("minLength", String.valueOf(minLength),
                                                        "maxLength", String.valueOf(maxLength)));
                }
                if (!getEmailPattern().matcher(normalized).matches()) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.EMAIL_FORMAT_INVALID_TECHNICAL,
                                        MessageCodes.Domain.User.EMAIL_FORMAT_INVALID_USER);
                }
                return normalized;
        }

        private static String validateMobileNumber(final String mobileNumber) {
                final String normalized = TextHelper.getDefaultWithTrim(mobileNumber);
                if (TextHelper.isEmpty(normalized)) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.MOBILE_EMPTY_TECHNICAL,
                                        MessageCodes.Domain.User.MOBILE_EMPTY_USER);
                }
                final int expectedLength = ParameterProvider.getInteger(ParameterCodes.User.MOBILE_LENGTH);
                final Pattern pattern = getMobilePattern();
                if (!pattern.matcher(normalized).matches()) {
                        throw DomainException.buildFromCatalog(MessageCodes.Domain.User.MOBILE_FORMAT_INVALID_TECHNICAL,
                                        MessageCodes.Domain.User.MOBILE_FORMAT_INVALID_USER,
                                        Map.of("expectedLength", String.valueOf(expectedLength)));
                }
                return normalized;
        }

        private static Pattern getNamePattern() {
                return ParameterProvider.getPattern(ParameterCodes.User.NAME_PATTERN);
        }

        private static Pattern getIdNumberPattern() {
                return ParameterProvider.getPattern(ParameterCodes.User.ID_NUMBER_PATTERN);
        }

        private static Pattern getEmailPattern() {
                return ParameterProvider.getPattern(ParameterCodes.User.EMAIL_PATTERN);
        }

        private static Pattern getMobilePattern() {
                return ParameterProvider.getPattern(ParameterCodes.User.MOBILE_PATTERN);
        }
}
