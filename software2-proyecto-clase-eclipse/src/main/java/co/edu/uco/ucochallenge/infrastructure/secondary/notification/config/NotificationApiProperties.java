package co.edu.uco.ucochallenge.infrastructure.secondary.notification.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;

@ConfigurationProperties(prefix = "notification.api")
public class NotificationApiProperties {

    private String baseUrl;
    private String duplicatePath = "/sender";
    private String apiKey;
    private String apiSecret;
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(5);
    private final List<RecipientProperties> adminRecipients = new ArrayList<>();
    private final List<RecipientProperties> userRecipients = new ArrayList<>();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDuplicatePath() {
        return TextHelper.getDefaultWithTrim(duplicatePath);
    }

    public void setDuplicatePath(final String duplicatePath) {
        this.duplicatePath = duplicatePath;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(final String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(final Duration connectTimeout) {
        if (connectTimeout != null && !connectTimeout.isNegative()) {
            this.connectTimeout = connectTimeout;
        }
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(final Duration readTimeout) {
        if (readTimeout != null && !readTimeout.isNegative()) {
            this.readTimeout = readTimeout;
        }
    }

    public List<RecipientProperties> getAdminRecipients() {
        return adminRecipients;
    }

    public List<RecipientProperties> getUserRecipients() {
        return userRecipients;
    }

    public static class RecipientProperties {

        private String name;
        private String email;
        private String mobileNumber;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(final String email) {
            this.email = email;
        }

        public String getMobileNumber() {
            return mobileNumber;
        }

        public void setMobileNumber(final String mobileNumber) {
            this.mobileNumber = mobileNumber;
        }

        public boolean hasContactInfo() {
            return !TextHelper.isEmpty(email) || !TextHelper.isEmpty(mobileNumber);
        }
    }
}
