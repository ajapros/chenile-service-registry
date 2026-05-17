package org.chenile.service.registry.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.core.model.HTTPMethod;
import org.chenile.core.model.HttpBindingType;
import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.chenile.service.registry.model.ChenileRemoteParamDefinition;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceRegistryCacheTest {

    @Test
    void existsReturnsTrueForEquivalentServiceDefinition() {
        ServiceRegistryCache cache = new ServiceRegistryCache(false);
        ChenileRemoteServiceDefinition definition = sampleService();
        cache.store(definition);

        assertTrue(cache.exists(sampleService()));
    }

    @Test
    void existsReturnsFalseWhenOperationChanges() {
        ServiceRegistryCache cache = new ServiceRegistryCache(false);
        cache.store(sampleService());

        ChenileRemoteServiceDefinition changed = sampleService();
        changed.operations.get(0).url = "/orders/search/v2";

        assertFalse(cache.exists(changed));
    }

    @Test
    void existsReturnsFalseWhenParamChanges() {
        ServiceRegistryCache cache = new ServiceRegistryCache(false);
        cache.store(sampleService());

        ChenileRemoteServiceDefinition changed = sampleService();
        changed.operations.get(0).params.get(0).paramClassName = "java.lang.Integer";

        assertFalse(cache.exists(changed));
    }

    @Test
    void existsReturnsTrueForSameIdAndVersionWhenImmutableVersionsAreEnforced() {
        ServiceRegistryCache cache = new ServiceRegistryCache(true);
        cache.store(sampleService());

        ChenileRemoteServiceDefinition changed = sampleService();
        changed.operations.get(0).url = "/orders/search/v2";

        assertTrue(cache.exists(changed));
    }

    @Test
    void existsReturnsFalseWhenHealthCheckerChanges() {
        ServiceRegistryCache cache = new ServiceRegistryCache(false);
        cache.store(sampleService());

        ChenileRemoteServiceDefinition changed = sampleService();
        changed.healthCheckerName = "otherHealthChecker";

        assertFalse(cache.exists(changed));
    }

    @Test
    void remoteServiceDefinitionSerializesMonolithNameAndAcceptsModuleName() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ChenileRemoteServiceDefinition service = sampleService();

        String json = objectMapper.writeValueAsString(service);
        assertTrue(json.contains("\"monolithName\":\"orders-module\""));
        assertFalse(json.contains("\"moduleName\""));

        ChenileRemoteServiceDefinition legacy = objectMapper.readValue("{\"moduleName\":\"legacy-module\"}",
                ChenileRemoteServiceDefinition.class);
        assertTrue("legacy-module".equals(legacy.getMonolithName()));
        assertNull(legacy.serviceId);
    }

    private ChenileRemoteServiceDefinition sampleService() {
        ChenileRemoteServiceDefinition service = new ChenileRemoteServiceDefinition();
        service.baseUrl = "http://localhost:8080";
        service.serviceId = "orders";
        service.serviceVersion = "2.1.19";
        service.moduleName = "orders-module";
        service.healthCheckerName = "ordersHealthChecker";
        service.clientInterceptorNames = List.of("authInterceptor", "traceInterceptor");
        service.operations = List.of(sampleOperation());
        return service;
    }

    private ChenileRemoteOperationDefinition sampleOperation() {
        ChenileRemoteOperationDefinition operation = new ChenileRemoteOperationDefinition();
        operation.description = "Search orders";
        operation.name = "searchOrders";
        operation.consumes = "JSON";
        operation.url = "/orders/search";
        operation.output = "java.lang.String";
        operation.httpMethod = HTTPMethod.POST;
        operation.setOutputAsStringReference("java.util.List<java.lang.String>");
        operation.clientInterceptorNames = List.of("opAuthInterceptor");
        operation.params = List.of(sampleParam());
        return operation;
    }

    private ChenileRemoteParamDefinition sampleParam() {
        ChenileRemoteParamDefinition param = new ChenileRemoteParamDefinition();
        param.name = "request";
        param.description = "Search request";
        param.paramClassName = "java.lang.String";
        param.type = HttpBindingType.BODY;
        return param;
    }
}
