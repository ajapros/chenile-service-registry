package org.chenile.service.registry.service.impl;

import jakarta.persistence.EntityManager;
import org.chenile.core.model.HTTPMethod;
import org.chenile.core.model.HttpBindingType;
import org.chenile.service.registry.SpringTestConfig;
import org.chenile.service.registry.configuration.dao.ServiceRegistryRepository;
import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.chenile.service.registry.model.ChenileRemoteParamDefinition;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SpringBootTest(classes = SpringTestConfig.class)
@ActiveProfiles("unittest")
@Transactional
class ServiceRegistryJpaOrderingTest {
    @Autowired
    ServiceRegistryRepository repository;
    @Autowired
    EntityManager entityManager;

    @Test
    void persistedWorkflowOperationReloadsWithStableParamSelectorAndInterceptorOrder() {
        ChenileRemoteServiceDefinition saved = repository.saveAndFlush(workflowService());
        String id = saved.id;

        entityManager.clear();

        ChenileRemoteServiceDefinition reloaded = repository.findById(id).orElseThrow();
        ChenileRemoteOperationDefinition processById = reloaded.operations.get(1);

        assertEquals("activityStateEntityService", reloaded.serviceId);
        assertIterableEquals(List.of("serviceAuthInterceptor", "serviceTraceInterceptor"),
                reloaded.clientInterceptorNames);
        assertIterableEquals(List.of("create", "processById"),
                reloaded.operations.stream().map(operation -> operation.name).toList());
        assertIterableEquals(List.of("id", "eventID", "eventPayload"),
                processById.params.stream().map(param -> param.name).toList());
        assertIterableEquals(List.of(HttpBindingType.HEADER, HttpBindingType.HEADER, HttpBindingType.BODY),
                processById.params.stream().map(param -> param.type).toList());
        assertIterableEquals(List.of("java.lang.String", "java.lang.String", "java.lang.Object"),
                processById.params.stream().map(param -> param.paramTypeReference).toList());
        assertIterableEquals(List.of("eventPayloadBodySelector", "fallbackBodySelector"),
                processById.bodyTypeSelectorComponentNames);
        assertIterableEquals(List.of("opAuthInterceptor", "opMetricsInterceptor"),
                processById.clientInterceptorNames);
    }

    private ChenileRemoteServiceDefinition workflowService() {
        ChenileRemoteServiceDefinition service = new ChenileRemoteServiceDefinition();
        service.baseUrl = "http://localhost:8080";
        service.serviceId = "activityStateEntityService";
        service.serviceVersion = "jpa-order-test";
        service.moduleName = "workflow-module";
        service.healthCheckerName = "activityHealthChecker";
        service.clientInterceptorNames = List.of("serviceAuthInterceptor", "serviceTraceInterceptor");
        service.operations = List.of(createOperation(), processByIdOperation());
        return service;
    }

    private ChenileRemoteOperationDefinition createOperation() {
        ChenileRemoteOperationDefinition operation = operation("create", "/activity", HTTPMethod.POST);
        operation.params = List.of(bodyParam("activity", "com.example.Activity"));
        return operation;
    }

    private ChenileRemoteOperationDefinition processByIdOperation() {
        ChenileRemoteOperationDefinition operation = operation("processById", "/activity/{id}/{eventID}", HTTPMethod.PATCH);
        operation.bodyTypeSelectorComponentNames = List.of("eventPayloadBodySelector", "fallbackBodySelector");
        operation.clientInterceptorNames = List.of("opAuthInterceptor", "opMetricsInterceptor");
        operation.params = List.of(
                headerParam("id"),
                headerParam("eventID"),
                bodyParam("eventPayload", "java.lang.Object"));
        return operation;
    }

    private ChenileRemoteOperationDefinition operation(String name, String url, HTTPMethod httpMethod) {
        ChenileRemoteOperationDefinition operation = new ChenileRemoteOperationDefinition();
        operation.description = name;
        operation.name = name;
        operation.consumes = "JSON";
        operation.url = url;
        operation.output = "java.lang.String";
        operation.httpMethod = httpMethod;
        operation.setOutputAsStringReference("java.lang.String");
        return operation;
    }

    private ChenileRemoteParamDefinition headerParam(String name) {
        ChenileRemoteParamDefinition param = param(name, "java.lang.String");
        param.type = HttpBindingType.HEADER;
        return param;
    }

    private ChenileRemoteParamDefinition bodyParam(String name, String typeReference) {
        ChenileRemoteParamDefinition param = param(name, typeReference);
        param.type = HttpBindingType.BODY;
        return param;
    }

    private ChenileRemoteParamDefinition param(String name, String typeReference) {
        ChenileRemoteParamDefinition param = new ChenileRemoteParamDefinition();
        param.name = name;
        param.description = name;
        param.paramClassName = typeReference;
        param.paramTypeReference = typeReference;
        return param;
    }
}
