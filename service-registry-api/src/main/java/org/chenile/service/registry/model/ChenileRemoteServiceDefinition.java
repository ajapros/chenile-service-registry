package org.chenile.service.registry.model;

import jakarta.persistence.*;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.OperationDefinition;
import org.chenile.jpautils.entity.BaseJpaEntity;
import org.chenile.owiz.Command;
import org.chenile.service.registry.context.RemoteChenileExchange;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_definition")
public class ChenileRemoteServiceDefinition extends BaseJpaEntity {
    public String baseUrl;
    public String serviceId;
    public String getServiceId(){ return this.serviceId;}
    public String serviceVersion;
    public String moduleName;

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER,orphanRemoval = true)
    public List<ChenileRemoteOperationDefinition> operations;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "client_service_interceptors",
            joinColumns = @JoinColumn(name = "id", referencedColumnName = "ID")
    )
    @Column(name = "interceptor_name")
    public List<String> clientInterceptorNames;
    @Transient public List<Command<RemoteChenileExchange>> clientInterceptors ;

    public ChenileRemoteServiceDefinition() {}
    public ChenileRemoteServiceDefinition(ChenileServiceDefinition serviceDefinition){
        this.baseUrl = serviceDefinition.getBaseUrl();
        this.clientInterceptorNames = serviceDefinition.getClientInterceptorComponentNames();
        this.serviceId = serviceDefinition.getId();
        this.serviceVersion = serviceDefinition.getVersion();
        this.moduleName = serviceDefinition.getModuleName();
        List<ChenileRemoteOperationDefinition> ops = new ArrayList<>();
        for (OperationDefinition od : serviceDefinition.getOperations()){
            ChenileRemoteOperationDefinition crod = new ChenileRemoteOperationDefinition(od);
            ops.add(crod);
        }
        this.operations = ops;
    }
}
