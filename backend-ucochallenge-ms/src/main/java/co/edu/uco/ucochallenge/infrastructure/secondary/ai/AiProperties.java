package co.edu.uco.ucochallenge.infrastructure.secondary.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "uco-challenge.ai")
public class AiProperties {

    private final OpenAi openai = new OpenAi();

    public OpenAi getOpenai() {
        return openai;
    }

    public static final class OpenAi {
        private boolean enabled;
        private String endpoint;
        private String apiKey;
        private String model;
        private double temperature = 0.2;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(final String endpoint) {
            this.endpoint = endpoint;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(final String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(final String model) {
            this.model = model;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(final double temperature) {
            this.temperature = temperature;
        }
    }
}
