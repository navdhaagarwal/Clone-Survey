package com.nucleus.core.mutitenancy;

import static com.nucleus.core.mutitenancy.CustomSchemaResolver.CAS_SCHEMA;

import java.util.Map;

import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

public class CustomMultiTenantConnectionProvider extends AbstractMultiTenantConnectionProvider {

    private static final long                     serialVersionUID    = 1L;
    private final Map<String, ConnectionProvider> connectionProviders = ConnectionProviderUtil.PrepareConnectionProviders();

    @Override
    protected ConnectionProvider getAnyConnectionProvider() {
        return connectionProviders.get(CAS_SCHEMA);
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        return connectionProviders.get(tenantIdentifier);
    }

}