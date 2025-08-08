package org.chenile.service.registry.context;

import org.chenile.core.context.ChenileExchange;
import org.chenile.service.registry.model.ChenileRemoteOperationDefinition;
import org.chenile.service.registry.model.ChenileRemoteServiceDefinition;

/**
 * Used to
 */
public class RemoteChenileExchange extends ChenileExchange {
    public ChenileRemoteServiceDefinition remoteServiceDefinition;
    public ChenileRemoteOperationDefinition remoteOperationDefinition;
}
