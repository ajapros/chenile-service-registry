package org.chenile.service.registry.model;

import jakarta.persistence.*;
import org.chenile.core.model.HTTPMethod;
import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.chenile.jpautils.entity.BaseJpaEntity;
import org.chenile.owiz.Command;
import org.chenile.service.registry.context.RemoteChenileExchange;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "operation_definition")
public class ChenileRemoteOperationDefinition extends BaseJpaEntity {
    public String description;
    public String name;
    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER,orphanRemoval = true)
    public List<ChenileRemoteParamDefinition> params;
    public String consumes = "JSON";
    public String url;
    public HTTPMethod httpMethod;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "client_op_interceptors",
            joinColumns = @JoinColumn(name = "id", referencedColumnName = "ID")
    )
    @Column(name = "interceptor_name")
    public List<String> clientInterceptorNames;
    @Transient public List<Command<RemoteChenileExchange>> clientInterceptors ;

    public ChenileRemoteOperationDefinition(){}
    public ChenileRemoteOperationDefinition(OperationDefinition od) {
        this.clientInterceptorNames = od.getClientInterceptorComponentNames();
        this.name = od.getName();
        this.url = od.getUrl();
        this.httpMethod = od.getHttpMethod();
        if (od.getConsumes() != null)
         this.consumes = od.getConsumes().name();
        this.description = od.getDescription();
        List<ChenileRemoteParamDefinition> params = new ArrayList<>();
        for (ParamDefinition pd : od.getParams()){
            ChenileRemoteParamDefinition crpd = new ChenileRemoteParamDefinition(pd);
            params.add(crpd);
        }
        this.params = params;
    }
}
