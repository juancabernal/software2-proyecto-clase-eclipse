package co.edu.uco.ucochallenge.application.user.registration.port;

public interface UserRegistrationContactConfirmationPort {

        void confirmEmail(String email);

        void confirmMobileNumber(String mobileNumber);
}
