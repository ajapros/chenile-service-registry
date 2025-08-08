package org.chenile.service.registry.cache;

import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ServiceRegistryCache {
    private Map<ServiceKey, ChenileRemoteServiceDefinition> services = new HashMap<>();
    private Map<String,ChenileRemoteServiceDefinition> latestVersionOfServices = new HashMap<>();

    public void store(ChenileRemoteServiceDefinition entity) {
        this.services.put(new ServiceKey(entity),entity);
        String id = entity.serviceId;
        String version = entity.serviceVersion;
        ChenileRemoteServiceDefinition sd = latestVersionOfServices.get(id);
        if (sd == null || sd.serviceVersion.compareTo(version) < 0){
            latestVersionOfServices.put(id,entity);
        }
    }

    public ChenileRemoteServiceDefinition retrieve(String serviceId){
        return latestVersionOfServices.get(serviceId);
    }

    public ChenileRemoteServiceDefinition retrieve(String serviceId, String serviceVersion) {
        return this.services.get(new ServiceKey(serviceId,serviceVersion));
    }

    public boolean exists(ChenileRemoteServiceDefinition entity){
        return this.services.containsKey(new ServiceKey(entity));
    }

    public boolean exists(String serviceId, String serviceVersion){
        return this.services.containsKey(new ServiceKey(serviceId,serviceVersion));
    }


    public static class ServiceKey {
        public String serviceId;
        public String serviceVersion;

        public ServiceKey(String serviceId, String serviceVersion){
            this.serviceId = serviceId;
            this.serviceVersion = serviceVersion;
        }

        public ServiceKey(ChenileRemoteServiceDefinition crsd){
            this.serviceId = crsd.serviceId;
            this.serviceVersion = crsd.serviceVersion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ServiceKey that)) return false;
            return Objects.equals(serviceId, that.serviceId) && Objects.equals(serviceVersion, that.serviceVersion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceId, serviceVersion);
        }
    }
}
