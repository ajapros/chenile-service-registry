package org.chenile.service.registry.model;

import jakarta.persistence.*;
import org.chenile.core.model.MimeType;
import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.chenile.jpautils.entity.BaseJpaEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "operation_definition")
public class ChenileRemoteOperationDefinition extends BaseJpaEntity {
    public String description;
    public String name;
    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER,orphanRemoval = true)
    public List<ChenileRemoteParamDefinition> params;
    public MimeType consumes;

    public ChenileRemoteOperationDefinition(){}
    public ChenileRemoteOperationDefinition(OperationDefinition od) {
        this.name = od.getName();
        this.consumes = od.getConsumes();
        this.description = od.getDescription();
        List<ChenileRemoteParamDefinition> params = new ArrayList<>();
        for (ParamDefinition pd : od.getParams()){
            ChenileRemoteParamDefinition crpd = new ChenileRemoteParamDefinition(pd);
            params.add(crpd);
        }
        this.params = params;
    }
}
