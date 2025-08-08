package org.chenile.service.registry.configuration;

import org.chenile.core.model.ChenileConfiguration;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.service.registry.cache.ServiceRegistryCache;
import org.chenile.service.registry.configuration.dao.ServiceRegistryRepository;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.chenile.service.registry.service.ServiceRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;


import java.util.Map;


@Configuration
@PropertySource("classpath:${chenile.properties:chenile.properties}")
public class ServiceRegistryInitializer implements ApplicationListener<ApplicationReadyEvent>, Ordered {
    private static Logger logger = LoggerFactory.getLogger(ServiceRegistryInitializer.class);
    @Value("${chenile.remote.service.registry:}")
    private String chenileRemoteServiceRegistry;

    @Autowired
    ServiceRegistryCache serviceRegistryCache;
    @Autowired
    ServiceRegistryRepository repository;
    @Autowired
    ServiceRegistryService serviceRegistryService;

    @Autowired
    ChenileConfiguration chenileConfiguration;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // retrieve beans from the database and store them in the local cache.
        // this ensures that we start gracefully from where we left.
        for (ChenileRemoteServiceDefinition service:  repository.findAll()){
            serviceRegistryCache.store(service);
        }

        // push all the beans from the local chenile configuration to the service registry
        for (Map.Entry<String, ChenileServiceDefinition> entry: chenileConfiguration.getServices().entrySet()){
            ChenileServiceDefinition sd = entry.getValue();
            ChenileRemoteServiceDefinition csrd = new ChenileRemoteServiceDefinition(sd);
            logger.info("Storing service " + csrd.serviceId + " and version = " + csrd.serviceVersion);
            serviceRegistryService.save(csrd);
        }
    }

    @Override
    public int getOrder() {
        return 50001;
    }
}
