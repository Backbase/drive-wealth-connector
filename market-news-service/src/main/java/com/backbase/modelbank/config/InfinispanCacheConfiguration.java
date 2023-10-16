package com.backbase.modelbank.config;

import com.backbase.dbs.portfolio.client.marketnews.rest.spec.model.NewsEntry;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.starter.embedded.InfinispanCacheConfigurer;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.concurrent.TimeUnit;

@org.springframework.context.annotation.Configuration
public class InfinispanCacheConfiguration {

    public static final String CACHE_NAME = "default-local";
    private static final long MEMORY_MAX_COUNT = 10L;
    private static final long EXPIRATION_MAX_LIFESPAN = 7L;

    @Bean
    public InfinispanCacheConfigurer cacheConfigurer() {
        return manager -> {
            final Configuration ispnConfig = new ConfigurationBuilder()
                    .clustering()
                    .cacheMode(CacheMode.LOCAL)
                    .memory()
                    .maxCount(MEMORY_MAX_COUNT)
                    .whenFull(EvictionStrategy.REMOVE)
                    .expiration()
                    .lifespan(EXPIRATION_MAX_LIFESPAN, TimeUnit.DAYS)
                    .build();

            manager.defineConfiguration(CACHE_NAME, ispnConfig);
        };
    }

    @Bean
    public Cache<String, List<NewsEntry>> getNewsEnteriesCache(EmbeddedCacheManager cacheManager) {
        return cacheManager.getCache(CACHE_NAME);
    }
}
