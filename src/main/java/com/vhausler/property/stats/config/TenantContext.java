package com.vhausler.property.stats.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantContext {

    public static final String DEFAULT_TENANT = "default-tenant";
    public static final String MIGRATION_TENANT = "migration-tenant";
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void setCurrentTenant(String tenant) {
        log.debug("Switching tenant to: {}.", tenant);
        CURRENT_TENANT.set(tenant);
    }
}
