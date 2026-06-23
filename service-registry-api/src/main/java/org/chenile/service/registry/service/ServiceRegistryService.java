package org.chenile.service.registry.service;

import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.chenile.service.registry.model.ServiceRegistryDiagnostics;

import java.util.List;

public interface ServiceRegistryService {
    public ChenileRemoteServiceDefinition save(ChenileRemoteServiceDefinition serviceDefinition);
    public ChenileRemoteServiceDefinition retrieveByIdVersion(String serviceId, String serviceVersion);
    public ChenileRemoteServiceDefinition retrieveById(String serviceId);
    public List<ChenileRemoteServiceDefinition> list();
    public ServiceRegistryDiagnostics diagnostics();
}
