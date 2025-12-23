package org.chenile.service.registry.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.chenile.core.model.HttpBindingType;
import org.chenile.core.model.ParamDefinition;
import org.chenile.jpautils.entity.BaseJpaEntity;

@Entity
@Table(name = "param_definition")
public class ChenileRemoteParamDefinition extends BaseJpaEntity {
    public  String name;
    public  String description;
    public  String paramClassName;

    public  HttpBindingType type;

    public ChenileRemoteParamDefinition(){}
    public ChenileRemoteParamDefinition(ParamDefinition pd) {
        this.name = pd.getName();
        this.description = pd.getDescription();
        this.paramClassName = pd.getParamClass().getName();
        this.type = pd.getType();
    }
}
