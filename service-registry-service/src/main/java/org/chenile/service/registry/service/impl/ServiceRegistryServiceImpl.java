package org.chenile.service.registry.service.impl;

import org.chenile.base.exception.NotFoundException;
import org.chenile.service.registry.configuration.dao.ServiceRegistryRepository;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.chenile.service.registry.service.ServiceRegistryService;
import org.chenile.service.registry.cache.ServiceRegistryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class ServiceRegistryServiceImpl implements ServiceRegistryService {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryServiceImpl.class);
    @Autowired
    ServiceRegistryRepository serviceregistryRepository;
    @Autowired
    ServiceRegistryCache serviceRegistryCache;

    /**
     * This saves it into the Service Registry database and cache. <br/>
     * If it exists already, it returns immediately since a combo of service ID and
     * version are immutable. (so nothing to update)
     * @param entity the Service definition that needs to be saved.
     * @return the service definition
     */
    @Override
    public ChenileRemoteServiceDefinition save(ChenileRemoteServiceDefinition entity) {
        if (serviceRegistryCache.exists(entity)) return entity;
        entity = serviceregistryRepository.save(entity);
        serviceRegistryCache.store(entity);
        return entity;
    }

    @Override
    public ChenileRemoteServiceDefinition retrieveByIdVersion(String serviceId, String serviceVersion) {
        ChenileRemoteServiceDefinition entity = serviceRegistryCache.retrieve(serviceId,serviceVersion);
        if(entity != null) return entity;
        throw new NotFoundException(1500,"Unable to find service registry with Service ID " + serviceId + " and version " + serviceVersion);
    }

    @Override
    public ChenileRemoteServiceDefinition retrieveById(String serviceId) {
        ChenileRemoteServiceDefinition entity = serviceRegistryCache.retrieve(serviceId);
        if(entity != null) return entity;
        throw new NotFoundException(1500,"Unable to find service registry with Service ID " + serviceId );
    }

    /**
     *
     * @param id - the auto generated ID
     * @return the service
     */
    public ChenileRemoteServiceDefinition retrieveFromDb(String id) {
        Optional<ChenileRemoteServiceDefinition> entity = serviceregistryRepository.findById(id);
        if (entity.isPresent()) return entity.get();
        throw new NotFoundException(1500,"Unable to find service registry with ID " + id);
    }
}