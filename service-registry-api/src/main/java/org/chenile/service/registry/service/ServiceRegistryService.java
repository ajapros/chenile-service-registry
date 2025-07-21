package org.chenile.service.registry.service;

import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;

public interface ServiceRegistryService {
    public ChenileRemoteServiceDefinition save(ChenileRemoteServiceDefinition serviceDefinition);
    public ChenileRemoteServiceDefinition retrieve(String serviceId, String serviceVersion);
}
