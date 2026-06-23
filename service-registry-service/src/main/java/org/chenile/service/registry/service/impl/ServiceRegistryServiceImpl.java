package org.chenile.service.registry.service.impl;

import org.chenile.base.exception.BadRequestException;
import org.chenile.base.exception.NotFoundException;
import org.chenile.service.registry.configuration.dao.ServiceRegistryRepository;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;
import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.chenile.service.registry.model.ChenileRemoteParamDefinition;
import org.chenile.service.registry.model.ServiceRegistryDiagnostics;
import org.chenile.service.registry.model.ServiceRegistryFingerprint;
import org.chenile.service.registry.service.ServiceRegistryService;
import org.chenile.service.registry.cache.ServiceRegistryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        validateServiceKey(entity);
        if (serviceRegistryCache.exists(entity)) return entity;
        ChenileRemoteServiceDefinition incoming = entity;
        List<ChenileRemoteServiceDefinition> existing = serviceregistryRepository
                .findByServiceIdAndServiceVersion(incoming.serviceId, incoming.serviceVersion);
        if (!existing.isEmpty()) {
            ChenileRemoteServiceDefinition canonical = existing.stream()
                    .min(Comparator.comparing(definition -> nullSafe(definition.id)))
                    .orElse(existing.get(0));
            existing.forEach(serviceRegistryCache::store);
            Optional<ChenileRemoteServiceDefinition> exactMatch = existing.stream()
                    .filter(definition -> ServiceRegistryFingerprint.semanticallyEquals(definition, incoming))
                    .findFirst();
            if (exactMatch.isPresent()) {
                return exactMatch.get();
            }
            logger.warn("Service registry definition changed without version bump. serviceId={} serviceVersion={}. "
                    + "Not creating a duplicate registry row.", entity.serviceId, entity.serviceVersion);
            return canonical;
        }
        try {
            entity = serviceregistryRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            logger.warn("Concurrent service registry registration detected for serviceId={} serviceVersion={}. "
                    + "Reloading existing registry row.", incoming.serviceId, incoming.serviceVersion);
            return findExistingForConcurrentInsert(incoming, e);
        }
        serviceRegistryCache.store(entity);
        return entity;
    }

    @Override
    public ChenileRemoteServiceDefinition retrieveByIdVersion(String serviceId, String serviceVersion) {
        ChenileRemoteServiceDefinition entity = serviceRegistryCache.retrieve(serviceId,serviceVersion);
        if(entity != null) return entity;
        throw new NotFoundException("1500","Unable to find service registry with Service ID " + serviceId + " and version " + serviceVersion);
    }

    @Override
    public ChenileRemoteServiceDefinition retrieveById(String serviceId) {
        ChenileRemoteServiceDefinition entity = serviceRegistryCache.retrieve(serviceId);
        if(entity != null) return entity;
        throw new NotFoundException("1500","Unable to find service registry with Service ID " + serviceId );
    }

    @Override
    public List<ChenileRemoteServiceDefinition> list() {
        return serviceregistryRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((ChenileRemoteServiceDefinition definition) -> nullSafe(definition.serviceId))
                        .thenComparing(definition -> nullSafe(definition.serviceVersion))
                        .thenComparing(definition -> nullSafe(definition.baseUrl)))
                .toList();
    }

    @Override
    public ServiceRegistryDiagnostics diagnostics() {
        List<ChenileRemoteServiceDefinition> services = serviceregistryRepository.findAll();
        ServiceRegistryDiagnostics diagnostics = new ServiceRegistryDiagnostics();
        diagnostics.totalServices = services.size();
        diagnostics.invalidServices = services.stream()
                .filter(service -> isBlank(service.serviceId) || isBlank(service.serviceVersion))
                .map(this::invalidServiceIssue)
                .toList();

        Map<String, List<ChenileRemoteServiceDefinition>> groupedByVersion = services.stream()
                .filter(service -> !isBlank(service.serviceId) && !isBlank(service.serviceVersion))
                .collect(Collectors.groupingBy(ServiceRegistryFingerprint::serviceKey));
        for (List<ChenileRemoteServiceDefinition> group : groupedByVersion.values()) {
            if (group.size() <= 1) {
                continue;
            }
            ServiceRegistryDiagnostics.ServiceVersionIssue issue = serviceVersionIssue(group);
            diagnostics.duplicateServiceVersions.add(issue);
            if (issue.fingerprintCount > 1) {
                diagnostics.changedSameVersions.add(issue);
            }
        }

        for (ChenileRemoteServiceDefinition service : services) {
            diagnostics.duplicateOperationLinkDetails.addAll(duplicateOperationIssues(service));
            diagnostics.duplicateParamLinkDetails.addAll(duplicateParamIssues(service));
        }

        diagnostics.duplicateServiceVersionGroups = diagnostics.duplicateServiceVersions.size();
        diagnostics.changedSameVersionGroups = diagnostics.changedSameVersions.size();
        diagnostics.duplicateOperationLinks = diagnostics.duplicateOperationLinkDetails.size();
        diagnostics.duplicateParamLinks = diagnostics.duplicateParamLinkDetails.size();
        diagnostics.invalidServiceRows = diagnostics.invalidServices.size();
        if (diagnostics.invalidServiceRows > 0) {
            diagnostics.warnings.add("Some service_definition rows have blank serviceId or serviceVersion.");
        }
        if (diagnostics.duplicateServiceVersionGroups > 0) {
            diagnostics.warnings.add("Multiple service_definition rows exist for the same serviceId and serviceVersion.");
        }
        if (diagnostics.changedSameVersionGroups > 0) {
            diagnostics.warnings.add("Some duplicate service/version groups contain different definitions. Bump the service version before changing a definition.");
        }
        if (diagnostics.duplicateOperationLinks > 0 || diagnostics.duplicateParamLinks > 0) {
            diagnostics.warnings.add("Duplicate join rows were found. Review cleanup SQL before deleting data.");
        }
        return diagnostics;
    }

    /**
     *
     * @param id - the auto generated ID
     * @return the service
     */
    public ChenileRemoteServiceDefinition retrieveFromDb(String id) {
        Optional<ChenileRemoteServiceDefinition> entity = serviceregistryRepository.findById(id);
        if (entity.isPresent()) return entity.get();
        throw new NotFoundException("1500","Unable to find service registry with ID " + id);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private void validateServiceKey(ChenileRemoteServiceDefinition entity) {
        if (entity == null)
            throw new BadRequestException("1501", "Service registry definition cannot be null");
        if (isBlank(entity.serviceId))
            throw new BadRequestException("1501", "Service registry definition must have serviceId");
        if (isBlank(entity.serviceVersion))
            throw new BadRequestException("1501", "Service registry definition must have serviceVersion");
    }

    private ChenileRemoteServiceDefinition findExistingForConcurrentInsert(ChenileRemoteServiceDefinition incoming,
                                                                          DataIntegrityViolationException original) {
        List<ChenileRemoteServiceDefinition> existing = serviceregistryRepository
                .findByServiceIdAndServiceVersion(incoming.serviceId, incoming.serviceVersion);
        if (existing.isEmpty())
            throw original;
        ChenileRemoteServiceDefinition exactMatch = existing.stream()
                .filter(definition -> ServiceRegistryFingerprint.semanticallyEquals(definition, incoming))
                .findFirst()
                .orElseGet(() -> existing.stream()
                        .min(Comparator.comparing(definition -> nullSafe(definition.id)))
                        .orElse(existing.get(0)));
        existing.forEach(serviceRegistryCache::store);
        return exactMatch;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ServiceRegistryDiagnostics.ServiceVersionIssue serviceVersionIssue(
            List<ChenileRemoteServiceDefinition> group) {
        ChenileRemoteServiceDefinition first = group.get(0);
        ServiceRegistryDiagnostics.ServiceVersionIssue issue =
                new ServiceRegistryDiagnostics.ServiceVersionIssue();
        issue.serviceId = first.serviceId;
        issue.serviceVersion = first.serviceVersion;
        issue.rowCount = group.size();
        issue.fingerprintCount = (int) group.stream()
                .map(ServiceRegistryFingerprint::serviceFingerprint)
                .distinct()
                .count();
        issue.rowIds = group.stream()
                .map(ChenileRemoteServiceDefinition::getId)
                .sorted(Comparator.nullsLast(String::compareTo))
                .toList();
        return issue;
    }

    private ServiceRegistryDiagnostics.ServiceVersionIssue invalidServiceIssue(
            ChenileRemoteServiceDefinition service) {
        ServiceRegistryDiagnostics.ServiceVersionIssue issue =
                new ServiceRegistryDiagnostics.ServiceVersionIssue();
        issue.serviceId = service.serviceId;
        issue.serviceVersion = service.serviceVersion;
        issue.rowCount = 1;
        issue.fingerprintCount = 1;
        issue.rowIds = List.of(nullSafe(service.getId()));
        return issue;
    }

    private List<ServiceRegistryDiagnostics.ServiceLinkIssue> duplicateOperationIssues(
            ChenileRemoteServiceDefinition service) {
        if (service.operations == null)
            return List.of();
        return service.operations.stream()
                .collect(Collectors.groupingBy(ServiceRegistryFingerprint::operationFingerprint, Collectors.toList()))
                .values()
                .stream()
                .filter(group -> group.size() > 1)
                .map(group -> serviceLinkIssue(service, group.get(0), null, group.size()))
                .toList();
    }

    private List<ServiceRegistryDiagnostics.ServiceLinkIssue> duplicateParamIssues(
            ChenileRemoteServiceDefinition service) {
        if (service.operations == null)
            return List.of();
        return service.operations.stream()
                .filter(operation -> operation.params != null)
                .flatMap(operation -> operation.params.stream()
                        .collect(Collectors.groupingBy(ServiceRegistryFingerprint::paramFingerprint, Collectors.toList()))
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().size() > 1)
                        .map(entry -> serviceLinkIssue(service, operation, entry.getValue().get(0), entry.getValue().size())))
                .toList();
    }

    private ServiceRegistryDiagnostics.ServiceLinkIssue serviceLinkIssue(ChenileRemoteServiceDefinition service,
                                                                        ChenileRemoteOperationDefinition operation,
                                                                        ChenileRemoteParamDefinition param,
                                                                        int duplicateCount) {
        ServiceRegistryDiagnostics.ServiceLinkIssue issue = new ServiceRegistryDiagnostics.ServiceLinkIssue();
        issue.serviceId = service.serviceId;
        issue.serviceVersion = service.serviceVersion;
        issue.serviceRowId = service.getId();
        issue.operation = ServiceRegistryFingerprint.operationDisplayKey(operation);
        issue.param = ServiceRegistryFingerprint.paramDisplayKey(param);
        issue.duplicateCount = duplicateCount;
        return issue;
    }
}
