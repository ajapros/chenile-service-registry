package org.chenile.service.registry.delegate;

import org.chenile.service.registry.cache.ServiceRegistryCache;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceRegistryClientImplTest {
    @Test
    void saveReturnsOriginalDefinitionAndDoesNotCacheWhenRemoteWriteFails() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        ServiceRegistryClientImpl client = new ServiceRegistryClientImpl(restTemplate);
        ServiceRegistryCache cache = new ServiceRegistryCache(false);
        ReflectionTestUtils.setField(client, "chenileRemoteServiceRegistry", "http://registry");
        client.serviceRegistryCache = cache;
        ChenileRemoteServiceDefinition definition = new ChenileRemoteServiceDefinition();
        definition.serviceId = "orders";
        definition.serviceVersion = "v1";
        when(restTemplate.exchange(
                eq("http://registry/serviceregistry"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new RuntimeException("remote down"));

        ChenileRemoteServiceDefinition saved = client.save(definition);

        assertSame(definition, saved);
        assertNull(cache.retrieve("orders", "v1"));
    }
}
