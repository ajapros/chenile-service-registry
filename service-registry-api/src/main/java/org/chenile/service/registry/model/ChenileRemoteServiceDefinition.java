package org.chenile.service.registry.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.OperationDefinition;
import org.chenile.jpautils.entity.BaseJpaEntity;
import org.chenile.owiz.Command;
import org.chenile.service.registry.context.RemoteChenileExchange;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_definition", uniqueConstraints = @UniqueConstraint(
        name = "uk_service_definition_service_version",
        columnNames = {"service_id", "service_version"}))
public class ChenileRemoteServiceDefinition extends BaseJpaEntity {
    public String baseUrl;
    @Column(name = "service_id", nullable = false)
    public String serviceId;
    public String getServiceId(){ return this.serviceId;}
    @Column(name = "service_version", nullable = false)
    public String serviceVersion;
    @JsonProperty("monolithName")
    @JsonAlias("moduleName")
    public String moduleName;
    public String healthCheckerName;

    public String getMonolithName() {
        return moduleName;
    }

    public void setMonolithName(String monolithName) {
        this.moduleName = monolithName;
    }

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER,orphanRemoval = true)
    @OrderColumn(name = "operation_order")
    public List<ChenileRemoteOperationDefinition> operations;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "client_service_interceptors",
            joinColumns = @JoinColumn(name = "id", referencedColumnName = "ID")
    )
    @OrderColumn(name = "interceptor_order")
    @Column(name = "interceptor_name")
    public List<String> clientInterceptorNames;
    @Transient public List<Command<RemoteChenileExchange>> clientInterceptors ;

    public ChenileRemoteServiceDefinition() {}
    public ChenileRemoteServiceDefinition(ChenileServiceDefinition serviceDefinition){
        this.baseUrl = serviceDefinition.getBaseUrl();
        this.clientInterceptorNames = serviceDefinition.getClientInterceptorComponentNames();
        this.serviceId = serviceDefinition.getId();
        this.serviceVersion = serviceDefinition.getVersion();
        this.moduleName = serviceDefinition.getMonolithName();
        this.healthCheckerName = serviceDefinition.getHealthCheckerName();
        List<ChenileRemoteOperationDefinition> ops = new ArrayList<>();
        for (OperationDefinition od : serviceDefinition.getOperations()){
            ChenileRemoteOperationDefinition crod = new ChenileRemoteOperationDefinition(od);
            ops.add(crod);
        }
        this.operations = ops;
    }
}
