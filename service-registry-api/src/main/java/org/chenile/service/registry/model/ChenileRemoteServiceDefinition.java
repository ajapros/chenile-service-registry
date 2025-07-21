package org.chenile.service.registry.model;

import jakarta.persistence.*;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.OperationDefinition;
import org.chenile.jpautils.entity.BaseJpaEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_definition")
public class ChenileRemoteServiceDefinition extends BaseJpaEntity {
    public String serviceId;
    public String getServiceId(){ return this.serviceId;}
    public String serviceVersion;
    public String moduleName;

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER,orphanRemoval = true)
    public List<ChenileRemoteOperationDefinition> operations;

    public ChenileRemoteServiceDefinition() {}
    public ChenileRemoteServiceDefinition(ChenileServiceDefinition serviceDefinition){
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
