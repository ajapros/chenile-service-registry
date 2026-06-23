package org.chenile.service.registry.configuration.dao;

import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRegistryRepository extends JpaRepository<ChenileRemoteServiceDefinition,String> {
    List<ChenileRemoteServiceDefinition> findByServiceIdAndServiceVersion(String serviceId, String serviceVersion);
}
