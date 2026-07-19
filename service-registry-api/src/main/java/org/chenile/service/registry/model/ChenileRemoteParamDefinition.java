package org.chenile.service.registry.model;

import jakarta.persistence.*;
import org.chenile.core.model.HttpBindingType;
import org.chenile.core.model.ParamDefinition;
import org.chenile.core.util.convert.ChenileTypeUtils;
import org.chenile.jpautils.entity.BaseJpaEntity;

@Entity
@Table(name = "param_definition")
public class ChenileRemoteParamDefinition extends BaseJpaEntity {
    public  String name;
    public  String description;
    public  String paramClassName;
    @Column(name = "param_type_reference")
    public  String paramTypeReference;
    @Enumerated(EnumType.STRING)
    public  HttpBindingType type;

    public ChenileRemoteParamDefinition(){}
    public ChenileRemoteParamDefinition(ParamDefinition pd) {
        this.name = pd.getName();
        this.description = pd.getDescription();
        this.paramClassName = pd.getParamClass() == null ? null : pd.getParamClass().getName();
        this.paramTypeReference = ChenileTypeUtils.typeToString(pd.getParamType());
        this.type = pd.getType();
    }
}
