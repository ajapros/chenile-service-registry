package org.chenile.service.registry.configuration;

import org.chenile.service.registry.delegate.ServiceRegistryClientImpl;
import org.chenile.service.registry.service.ServiceRegistryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RegistryDelegateConfiguration {
    @Bean
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    ServiceRegistryService serviceRegistryService(@Qualifier("restTemplate") RestTemplate restTemplate){
        return new ServiceRegistryClientImpl(restTemplate);
    }
}
