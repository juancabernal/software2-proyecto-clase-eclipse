// ...existing code...
package co.edu.uco.ucochallenge.infrastructure.secondary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("cacheFlags")
public class CacheFlags {

    @Value("${app.cache.enabled:true}")
    private boolean enabled;

    public boolean enabled() {
        return enabled;
    }
}

