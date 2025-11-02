package co.edu.uco.ucochallenge.domain.user.model;

import java.util.UUID;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;

public record UserFilter(
                UUID idType,
                String idNumber,
                String firstName,
                String firstSurname,
                UUID homeCity,
                String email,
                String mobileNumber) {

        public UserFilter {
                idType = UUIDHelper.getDefault(idType);
                idNumber = TextHelper.getDefaultWithTrim(idNumber);
                firstName = TextHelper.getDefaultWithTrim(firstName);
                firstSurname = TextHelper.getDefaultWithTrim(firstSurname);
                homeCity = UUIDHelper.getDefault(homeCity);
                email = TextHelper.getDefaultWithTrim(email).toLowerCase();
                mobileNumber = TextHelper.getDefaultWithTrim(mobileNumber);
        }

        public boolean hasIdType() {
                return !UUIDHelper.getDefault().equals(idType);
        }

        public boolean hasHomeCity() {
                return !UUIDHelper.getDefault().equals(homeCity);
        }

        public boolean hasIdNumber() {
                return !TextHelper.isEmpty(idNumber);
        }

        public boolean hasFirstName() {
                return !TextHelper.isEmpty(firstName);
        }

        public boolean hasFirstSurname() {
                return !TextHelper.isEmpty(firstSurname);
        }

        public boolean hasEmail() {
                return !TextHelper.isEmpty(email);
        }

        public boolean hasMobileNumber() {
                return !TextHelper.isEmpty(mobileNumber);
        }
}
