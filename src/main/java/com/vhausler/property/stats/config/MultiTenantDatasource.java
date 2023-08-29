package com.vhausler.property.stats.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class MultiTenantDatasource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant();
    }
}
