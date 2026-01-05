package org.chenile.service.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.chenile.core.model.HTTPMethod;
import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.chenile.core.util.convert.ChenileTypeUtils;
import org.chenile.jpautils.entity.BaseJpaEntity;
import org.chenile.owiz.Command;
import org.chenile.service.registry.context.RemoteChenileExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "operation_definition")
public class ChenileRemoteOperationDefinition extends BaseJpaEntity {
    @Transient @JsonIgnore
    private Logger logger = LoggerFactory.getLogger(getClass());
    public String description;
    public String name;
    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER,orphanRemoval = true)
    public List<ChenileRemoteParamDefinition> params;
    public String consumes = "JSON";
    public String url;
    public String output;
    @Enumerated(EnumType.STRING)
    public HTTPMethod httpMethod;



    @JsonIgnore @Transient
    public ParameterizedTypeReference<?> outputAsParameterizedReference;
    /**
     * The Parameterized Type reference is stored as string here for the purpose of
     * serialization. The actual reference cannot be serialized and hence ignored when
     * storing to DB or when echoed out as JSON.
     */
    private String outputAsStringReference;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "client_op_interceptors",
            joinColumns = @JoinColumn(name = "id", referencedColumnName = "ID")
    )
    @Column(name = "interceptor_name")
    public List<String> clientInterceptorNames;
    @JsonIgnore @Transient
    public List<Command<RemoteChenileExchange>> clientInterceptors ;

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
        if (od.getOutputAsParameterizedReference() != null) {
            this.outputAsStringReference = od.getOutputAsParameterizedReference().getType().getTypeName();
            this.outputAsParameterizedReference = od.getOutputAsParameterizedReference();
        }
        this.output = od.getOutput()!=null?od.getOutput().getName():null;
        this.params = params;
    }

    public String getOutputAsStringReference() {
        return this.outputAsStringReference;
    }

    public void setOutputAsStringReference(String outputAsStringReference) {
        this.outputAsStringReference = outputAsStringReference;
        try {
            this.outputAsParameterizedReference = ChenileTypeUtils.makeParameterizedTypeReference(outputAsStringReference);
        }catch(Exception ignore){
            logger.warn("Unable to deserialize the string received for parameterized type:|{}|", outputAsStringReference);
        }
    }
}
