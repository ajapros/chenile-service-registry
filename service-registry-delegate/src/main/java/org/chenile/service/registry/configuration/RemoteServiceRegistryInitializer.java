package org.chenile.service.registry.configuration;

import org.chenile.core.model.ChenileConfiguration;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.chenile.service.registry.service.ServiceRegistryService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Map;

@Configuration
@PropertySource("classpath:${chenile.properties:chenile.properties}")
public class RemoteServiceRegistryInitializer implements ApplicationListener<ApplicationReadyEvent>, Ordered {

    @Value("${chenile.remote.service.registry:}")
    private String chenileRemoteServiceRegistry;

    @Autowired
    ChenileConfiguration chenileConfiguration;

    @Autowired
    ServiceRegistryService serviceRegistryService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (chenileRemoteServiceRegistry.isEmpty())
            return;
        // push all the beans that are registered to the remote service registry
        for (Map.Entry<String, ChenileServiceDefinition> entry: chenileConfiguration.getServices().entrySet()){
            ChenileServiceDefinition sd = entry.getValue();
            serviceRegistryService.save(new ChenileRemoteServiceDefinition(sd));
        }
    }

    @Override
    public int getOrder() {
        return 10001;
    }
}
