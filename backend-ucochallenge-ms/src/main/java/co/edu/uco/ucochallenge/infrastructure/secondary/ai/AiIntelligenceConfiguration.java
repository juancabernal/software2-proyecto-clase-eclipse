package co.edu.uco.ucochallenge.infrastructure.secondary.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import co.edu.uco.ucochallenge.domain.ai.port.out.UserIntelligencePort;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiIntelligenceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AiIntelligenceConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(UserIntelligencePort.class)
    public UserIntelligencePort noOpUserIntelligencePort() {
        return user -> log.debug(
                "AI insight generation skipped for user {}. Configure an AI provider to enable insights.",
                user.id());
    }

    @Bean
    @ConditionalOnProperty(prefix = "uco-challenge.ai.openai", name = "enabled", havingValue = "true")
    public UserIntelligencePort openAiUserIntelligencePort(
            final AiProperties properties,
            final RestClient.Builder restClientBuilder) {
        return new OpenAiUserIntelligenceAdapter(properties, restClientBuilder);
    }
}
