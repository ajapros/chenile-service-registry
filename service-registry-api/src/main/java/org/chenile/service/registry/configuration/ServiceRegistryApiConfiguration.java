package org.chenile.service.registry.configuration;

import org.chenile.service.registry.cache.ServiceRegistryCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceRegistryApiConfiguration {
    @Value("${enforceImmutableServiceVersions:false}")
    private boolean enforceImmutableServiceVersions;

    @Bean
    ServiceRegistryCache serviceRegistryCache() {
        return new ServiceRegistryCache(enforceImmutableServiceVersions);
    }

}
