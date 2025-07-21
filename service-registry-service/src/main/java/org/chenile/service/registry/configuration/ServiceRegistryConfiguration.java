package org.chenile.service.registry.configuration;

import org.chenile.service.registry.service.ServiceRegistryService;
import org.chenile.service.registry.service.healthcheck.ServiceregistryHealthChecker;
import org.chenile.service.registry.service.impl.ServiceRegistryServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 This is where you will instantiate all the required classes in Spring

*/
@Configuration
public class ServiceRegistryConfiguration {
	@Bean public ServiceRegistryService _serviceregistryService_() {
		return new ServiceRegistryServiceImpl();
	}

	@Bean ServiceregistryHealthChecker serviceregistryHealthChecker(){
    	return new ServiceregistryHealthChecker();
    }

}
