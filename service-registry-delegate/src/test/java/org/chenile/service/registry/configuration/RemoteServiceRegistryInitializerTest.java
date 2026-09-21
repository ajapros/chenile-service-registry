package org.chenile.service.registry.configuration;

import org.chenile.core.model.ChenileConfiguration;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.service.registry.service.ServiceRegistryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RemoteServiceRegistryInitializerTest {
	@Test
	void readOnlyModeDoesNotPublishAnyLocalService() {
		RemoteServiceRegistryInitializer initializer = initializer(true);

		initializer.onApplicationEvent(mock(ApplicationReadyEvent.class));

		verify(initializer.serviceRegistryService, never()).save(any());
	}

	@Test
	void normalModeHonorsPerServicePublicationSetting() {
		RemoteServiceRegistryInitializer initializer = initializer(false);

		initializer.onApplicationEvent(mock(ApplicationReadyEvent.class));

		verify(initializer.serviceRegistryService).save(any());
		verify(initializer.serviceRegistryService, never()).save(org.mockito.ArgumentMatchers.argThat(
				definition -> "private".equals(definition.serviceId)));
	}

	private RemoteServiceRegistryInitializer initializer(boolean readOnly) {
		RemoteServiceRegistryInitializer initializer = new RemoteServiceRegistryInitializer();
		ReflectionTestUtils.setField(initializer, "chenileRemoteServiceRegistry", "http://registry");
		initializer.readOnly = readOnly;
		initializer.serviceRegistryService = mock(ServiceRegistryService.class);
		ChenileConfiguration configuration = new ChenileConfiguration("test", null);
		configuration.setService("published", service("published", true));
		configuration.setService("private", service("private", false));
		initializer.chenileConfiguration = configuration;
		return initializer;
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
