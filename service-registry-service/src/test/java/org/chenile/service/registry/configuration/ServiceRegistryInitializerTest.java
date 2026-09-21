package org.chenile.service.registry.configuration;

import org.chenile.core.model.ChenileConfiguration;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.service.registry.cache.ServiceRegistryCache;
import org.chenile.service.registry.configuration.dao.ServiceRegistryRepository;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.chenile.service.registry.service.ServiceRegistryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceRegistryInitializerTest {
	@Test
	void publishesOnlyServicesEnabledForRemoteRegistry() {
		ServiceRegistryInitializer initializer = new ServiceRegistryInitializer();
		initializer.repository = mock(ServiceRegistryRepository.class);
		initializer.serviceRegistryCache = new ServiceRegistryCache(false);
		initializer.serviceRegistryService = mock(ServiceRegistryService.class);
		when(initializer.repository.findAll()).thenReturn(List.of());

		ChenileConfiguration configuration = new ChenileConfiguration("test", null);
		configuration.setService("published", service("published", true));
		configuration.setService("headless", service("headless", false));
		initializer.chenileConfiguration = configuration;

		initializer.onApplicationEvent(mock(ApplicationReadyEvent.class));

		verify(initializer.serviceRegistryService).save(any());
		verify(initializer.serviceRegistryService, never()).save(org.mockito.ArgumentMatchers.argThat(
				definition -> "headless".equals(definition.serviceId)));
	}

	@Test
	void readOnlyModeLoadsExistingDefinitionsButDoesNotPublishLocalServices() {
		ServiceRegistryInitializer initializer = new ServiceRegistryInitializer();
		initializer.repository = mock(ServiceRegistryRepository.class);
		initializer.serviceRegistryCache = new ServiceRegistryCache(false);
		initializer.serviceRegistryService = mock(ServiceRegistryService.class);
		ChenileRemoteServiceDefinition existing = new ChenileRemoteServiceDefinition();
		existing.serviceId = "existing";
		existing.serviceVersion = "v1";
		when(initializer.repository.findAll()).thenReturn(List.of(existing));

		ChenileConfiguration configuration = new ChenileConfiguration("test", null);
		configuration.setService("local", service("local", true));
		initializer.chenileConfiguration = configuration;
		initializer.readOnly = true;

		initializer.onApplicationEvent(mock(ApplicationReadyEvent.class));

		verify(initializer.serviceRegistryService, never()).save(any());
		org.junit.jupiter.api.Assertions.assertSame(existing,
				initializer.serviceRegistryCache.retrieve("existing", "v1"));
	}

	private ChenileServiceDefinition service(String id, boolean registerInServiceRegistry) {
		ChenileServiceDefinition definition = new ChenileServiceDefinition();
		definition.setId(id);
		definition.setVersion("test");
		definition.setOperations(List.of());
		definition.setRegisterInServiceRegistry(registerInServiceRegistry);
		return definition;
	}
}
