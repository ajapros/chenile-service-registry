package org.chenile.service.registry.cache;

import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;

import java.util.HashMap;
import java.util.List;
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
        ChenileRemoteServiceDefinition csrd = this.services.get(new ServiceKey(entity));
        return equalsCheck(csrd,entity);
        // return this.services.containsKey(new ServiceKey(entity));
    }

    /**
     * Does a deep check to see if anything has changed between the two. If it has changed returns false
     * else returns true. This is to safeguard against situations when the service definition has changed but
     * the version has not been bumped up.
     * @param retrievedOne - the one in the database
     * @param passedOne - the one that has been passed
     * @return if the retrievedOne equals passed One.
     */
    private boolean equalsCheck(ChenileRemoteServiceDefinition retrievedOne, ChenileRemoteServiceDefinition passedOne) {
        if (retrievedOne == null)
            return false;
        if (!Objects.equals(retrievedOne.baseUrl, passedOne.baseUrl))
            return false;
        if (!Objects.equals(retrievedOne.serviceId, passedOne.serviceId))
            return false;
        if (!Objects.equals(retrievedOne.serviceVersion, passedOne.serviceVersion))
            return false;
        if (!Objects.equals(retrievedOne.moduleName, passedOne.moduleName))
            return false;
        if (!Objects.equals(retrievedOne.clientInterceptorNames, passedOne.clientInterceptorNames))
            return false;
        if(!compareOperations(retrievedOne.operations, passedOne.operations))
            return false;
        return true;
    }

    /**
     *
     * @param retrievedOps the operations that are retrieved from cache.
     * @param passedOps The operations that are passed to be updated
     * @return false if any of the operations has changed.
     */
    private boolean compareOperations(List<ChenileRemoteOperationDefinition> retrievedOps,
                                      List<ChenileRemoteOperationDefinition> passedOps){
        if (retrievedOps == null || passedOps == null)
            return retrievedOps == passedOps;
        if (retrievedOps.size() != passedOps.size())
            return false;
        for (int i = 0; i < retrievedOps.size(); i++){
            ChenileRemoteOperationDefinition retrieved = retrievedOps.get(i);
            ChenileRemoteOperationDefinition passed = passedOps.get(i);
            if (!equalsCheck(retrieved,passed))
                return false;
        }
        return true;
    }

    private boolean equalsCheck(ChenileRemoteOperationDefinition retrieved,
                                ChenileRemoteOperationDefinition passed){
        if (retrieved == null || passed == null)
            return retrieved == passed;
        if (!Objects.equals(retrieved.description, passed.description))
            return false;
        if (!Objects.equals(retrieved.name, passed.name))
            return false;
        if (!compareParams(retrieved.params, passed.params))
            return false;
        if (!Objects.equals(retrieved.consumes, passed.consumes))
            return false;
        if (!Objects.equals(retrieved.url, passed.url))
            return false;
        if (!Objects.equals(retrieved.output, passed.output))
            return false;
        if (!Objects.equals(retrieved.httpMethod, passed.httpMethod))
            return false;
        if (!Objects.equals(retrieved.getOutputAsStringReference(), passed.getOutputAsStringReference()))
            return false;
        if (!Objects.equals(retrieved.clientInterceptorNames, passed.clientInterceptorNames))
            return false;
        return true;
    }

    private boolean compareParams(List<org.chenile.service.registry.model.ChenileRemoteParamDefinition> retrievedParams,
                                  List<org.chenile.service.registry.model.ChenileRemoteParamDefinition> passedParams) {
        if (retrievedParams == null || passedParams == null)
            return retrievedParams == passedParams;
        if (retrievedParams.size() != passedParams.size())
            return false;
        for (int i = 0; i < retrievedParams.size(); i++) {
            if (!equalsCheck(retrievedParams.get(i), passedParams.get(i)))
                return false;
        }
        return true;
    }

    private boolean equalsCheck(org.chenile.service.registry.model.ChenileRemoteParamDefinition retrieved,
                                org.chenile.service.registry.model.ChenileRemoteParamDefinition passed) {
        if (retrieved == null || passed == null)
            return retrieved == passed;
        return Objects.equals(retrieved.name, passed.name)
                && Objects.equals(retrieved.description, passed.description)
                && Objects.equals(retrieved.paramClassName, passed.paramClassName)
                && Objects.equals(retrieved.type, passed.type);
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
