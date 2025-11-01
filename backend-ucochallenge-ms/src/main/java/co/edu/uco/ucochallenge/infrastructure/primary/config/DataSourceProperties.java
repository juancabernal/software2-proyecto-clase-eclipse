package co.edu.uco.ucochallenge.infrastructure.primary.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the datasource configuration exposed through {@code spring.datasource}
 * so it can be injected into the {@link javax.sql.DataSource} factory without
 * coupling the infrastructure layer to Spring Boot internals.
 */
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties {

    private String url;
    private String driverClassName;
    private final Hikari hikari = new Hikari();

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(final String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public Hikari getHikari() {
        return hikari;
    }

    public static class Hikari {

        private Integer maximumPoolSize = 10;
        private Integer minimumIdle = 1;
        private Long connectionTimeout;
        private String poolName = "UcoHikariPool";

        public Integer getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(final Integer maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public Integer getMinimumIdle() {
            return minimumIdle;
        }

        public void setMinimumIdle(final Integer minimumIdle) {
            this.minimumIdle = minimumIdle;
        }

        public Long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(final Long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public String getPoolName() {
            return poolName;
        }

        public void setPoolName(final String poolName) {
            this.poolName = poolName;
        }
    }
}

