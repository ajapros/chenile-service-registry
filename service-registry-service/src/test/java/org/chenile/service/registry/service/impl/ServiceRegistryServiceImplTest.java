package org.chenile.service.registry.service.impl;

import org.chenile.core.model.HTTPMethod;
import org.chenile.core.model.HttpBindingType;
import org.chenile.base.exception.BadRequestException;
import org.chenile.service.registry.cache.ServiceRegistryCache;
import org.chenile.service.registry.configuration.dao.ServiceRegistryRepository;
import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.chenile.service.registry.model.ChenileRemoteParamDefinition;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.chenile.service.registry.model.ServiceRegistryDiagnostics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceRegistryServiceImplTest {
    ServiceRegistryRepository repository;
    ServiceRegistryCache cache;
    ServiceRegistryServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(ServiceRegistryRepository.class);
        cache = new ServiceRegistryCache(false);
        service = new ServiceRegistryServiceImpl();
        service.serviceregistryRepository = repository;
        service.serviceRegistryCache = cache;
    }

    @Test
    void saveReturnsExistingRowForSameServiceVersionWhenPayloadIsEquivalent() {
        ChenileRemoteServiceDefinition existing = sampleService("row-1");
        ChenileRemoteServiceDefinition incoming = sampleService(null);
        when(repository.findByServiceIdAndServiceVersion("orders", "v1")).thenReturn(List.of(existing));

        ChenileRemoteServiceDefinition saved = service.save(incoming);

        assertSame(existing, saved);
        verify(repository, never()).save(any());
    }

    @Test
    void saveRefreshesCanonicalRowWhenSameServiceVersionChangedWithoutVersionBump() {
        ChenileRemoteServiceDefinition existing = sampleService("row-1");
        ChenileRemoteServiceDefinition incoming = sampleService(null);
        incoming.operations.get(0).url = "/orders/search/v2";
        when(repository.findByServiceIdAndServiceVersion("orders", "v1")).thenReturn(List.of(existing));
        when(repository.saveAndFlush(existing)).thenReturn(existing);

        ChenileRemoteServiceDefinition saved = service.save(incoming);

        assertSame(existing, saved);
        assertEquals("/orders/search/v2", existing.operations.get(0).url);
        verify(repository).saveAndFlush(existing);
    }

    @Test
    void saveInsertsWhenServiceVersionIsNew() {
        ChenileRemoteServiceDefinition incoming = sampleService(null);
        when(repository.findByServiceIdAndServiceVersion("orders", "v1")).thenReturn(List.of());
        when(repository.saveAndFlush(incoming)).thenReturn(incoming);

        ChenileRemoteServiceDefinition saved = service.save(incoming);

        assertSame(incoming, saved);
        verify(repository).saveAndFlush(incoming);
    }

    @Test
    void saveReloadsExistingRowWhenConcurrentInsertHitsUniqueConstraint() {
        ChenileRemoteServiceDefinition incoming = sampleService(null);
        ChenileRemoteServiceDefinition existing = sampleService("row-1");
        when(repository.findByServiceIdAndServiceVersion("orders", "v1"))
                .thenReturn(List.of())
                .thenReturn(List.of(existing));
        when(repository.saveAndFlush(incoming)).thenThrow(new DataIntegrityViolationException("duplicate"));

        ChenileRemoteServiceDefinition saved = service.save(incoming);

        assertSame(existing, saved);
    }

    @Test
    void saveRejectsMissingServiceKey() {
        ChenileRemoteServiceDefinition incoming = sampleService(null);
        incoming.serviceVersion = "";

        assertThrows(BadRequestException.class, () -> service.save(incoming));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void diagnosticsReportsDuplicateRowsChangedVersionsAndDuplicateLinks() {
        ChenileRemoteServiceDefinition rowOne = sampleService("row-1");
        rowOne.operations = List.of(sampleOperation(), sampleOperation());
        ChenileRemoteServiceDefinition rowTwo = sampleService("row-2");
        rowTwo.operations.get(0).url = "/orders/search/v2";
        rowTwo.operations.get(0).params = List.of(sampleParam(), sampleParam());
        when(repository.findAll()).thenReturn(List.of(rowOne, rowTwo));

        ServiceRegistryDiagnostics diagnostics = service.diagnostics();

        assertEquals(2, diagnostics.totalServices);
        assertEquals(1, diagnostics.duplicateServiceVersionGroups);
        assertEquals(1, diagnostics.changedSameVersionGroups);
        assertEquals(1, diagnostics.duplicateOperationLinks);
        assertEquals(1, diagnostics.duplicateParamLinks);
    }

    @Test
    void diagnosticsReportsInvalidServiceRows() {
        ChenileRemoteServiceDefinition invalid = sampleService("row-1");
        invalid.serviceId = "";
        when(repository.findAll()).thenReturn(List.of(invalid));

        ServiceRegistryDiagnostics diagnostics = service.diagnostics();

        assertEquals(1, diagnostics.invalidServiceRows);
        assertEquals(1, diagnostics.invalidServices.size());
    }

    private ChenileRemoteServiceDefinition sampleService(String id) {
        ChenileRemoteServiceDefinition service = new ChenileRemoteServiceDefinition();
        service.id = id;
        service.baseUrl = "http://localhost:8080";
        service.serviceId = "orders";
        service.serviceVersion = "v1";
        service.moduleName = "orders-module";
        service.healthCheckerName = "ordersHealthChecker";
        service.clientInterceptorNames = List.of("authInterceptor");
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
