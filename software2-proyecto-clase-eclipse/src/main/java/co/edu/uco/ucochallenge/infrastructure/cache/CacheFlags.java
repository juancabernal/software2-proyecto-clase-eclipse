// co.edu.uco.ucochallenge.infrastructure.cache.CacheFlags.java
package co.edu.uco.ucochallenge.infrastructure.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("cacheFlags")
public class CacheFlags {
    @Value("${app.cache.enabled:true}")
    private boolean enabled;

    public boolean enabled() { return enabled; }
}
