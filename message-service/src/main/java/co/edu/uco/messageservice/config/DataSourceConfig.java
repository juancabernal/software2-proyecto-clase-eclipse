package co.edu.uco.messageservice.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import co.edu.uco.messageservice.secret.SecretProviderPort;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    private final SecretProviderPort secretProvider;
    private final DataSourceProperties properties;

    public DataSourceConfig(final SecretProviderPort secretProvider, final DataSourceProperties properties) {
        this.secretProvider = secretProvider;
        this.properties = properties;
    }

    @Primary
    @Bean
    public DataSource dataSource() {
        final HikariConfig config = new HikariConfig();

        String url = secretProvider.getSecret("db-url");
        if (!StringUtils.hasText(url)) {
            url = properties.getUrl();
        }

        if (!StringUtils.hasText(url)) {
            throw new IllegalStateException("Database URL is missing. Provide it via Key Vault or configuration.");
        }
        config.setJdbcUrl(url);

        final String driverClassName = properties.getDriverClassName();
        if (StringUtils.hasText(driverClassName)) {
            config.setDriverClassName(driverClassName);
        }

        final String username = secretProvider.getSecret("db-username");
        final String password = secretProvider.getSecret("db-password");

        if (!StringUtils.hasText(username)) {
            log.warn("Database username is empty.");
        }
        if (!StringUtils.hasText(password)) {
            log.warn("Database password is empty.");
        }

        config.setUsername(username);
        config.setPassword(password);

        final DataSourceProperties.Hikari hikari = properties.getHikari();
        if (hikari.getMaximumPoolSize() != null) {
            config.setMaximumPoolSize(hikari.getMaximumPoolSize());
        }
        if (hikari.getMinimumIdle() != null) {
            config.setMinimumIdle(hikari.getMinimumIdle());
        }
        if (hikari.getConnectionTimeout() != null) {
            config.setConnectionTimeout(hikari.getConnectionTimeout());
        }
        if (StringUtils.hasText(hikari.getPoolName())) {
            config.setPoolName(hikari.getPoolName());
        }

        log.info("DataSource initialized for URL: {}", url);
        return new HikariDataSource(config);
    }
}
