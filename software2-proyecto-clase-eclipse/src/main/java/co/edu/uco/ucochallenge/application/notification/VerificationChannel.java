package co.edu.uco.ucochallenge.application.notification;

public enum VerificationChannel {
    EMAIL,
    MOBILE;

    public boolean isEmail() {
        return this == EMAIL;
    }

    public boolean isMobile() {
        return this == MOBILE;
    }
}