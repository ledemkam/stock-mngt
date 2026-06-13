package com.kte.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    private final CacheConfig cacheConfig = new CacheConfig();

    @Test
    void cacheManager_shouldReturnNonNullBean() {
        assertThat(cacheConfig.cacheManager()).isNotNull();
    }

    @Test
    void cacheManager_shouldBeConcurrentMapCacheManager() {
        CacheManager manager = cacheConfig.cacheManager();

        assertThat(manager).isInstanceOf(ConcurrentMapCacheManager.class);
    }

    @Test
    void cacheManager_shouldRegisterTenantSchemasCacheName() {
        CacheManager manager = cacheConfig.cacheManager();

        assertThat(manager.getCacheNames()).contains("tenantSchemas");
    }

    @Test
    void cacheManager_tenantSchemasCache_shouldBeAccessible() {
        CacheManager manager = cacheConfig.cacheManager();

        assertThat(manager.getCache("tenantSchemas")).isNotNull();
    }
}