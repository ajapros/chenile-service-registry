package org.chenile.service.registry.model;

import jakarta.persistence.*;
import org.chenile.core.model.HttpBindingType;
import org.chenile.core.model.ParamDefinition;
import org.chenile.jpautils.entity.BaseJpaEntity;

@Entity
@Table(name = "param_definition")
public class ChenileRemoteParamDefinition extends BaseJpaEntity {
    public  String name;
    public  String description;
    public  String paramClassName;
    @Enumerated(EnumType.STRING)
    public  HttpBindingType type;

    public ChenileRemoteParamDefinition(){}
    public ChenileRemoteParamDefinition(ParamDefinition pd) {
        this.name = pd.getName();
        this.description = pd.getDescription();
        this.paramClassName = pd.getParamClass().getName();
        this.type = pd.getType();
    }
}
