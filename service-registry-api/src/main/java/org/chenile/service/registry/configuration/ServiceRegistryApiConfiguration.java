package org.chenile.service.registry.configuration;

import org.chenile.service.registry.cache.ServiceRegistryCache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@EnableCaching
@Configuration
public class ServiceRegistryApiConfiguration {
    @Bean
    ServiceRegistryCache serviceRegistryCache() { return new ServiceRegistryCache();}

}
