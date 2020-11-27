package org.entur.lamassu.config.redisson;

import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;

import static org.entur.lamassu.config.redisson.RedissonConstants.GBFS_FEED_CACHE_KEY;

@Configuration
public class RedissonCacheConfig {

    @Bean
    public Cache<String, GBFSBase> feedCache(Config config) {
        var feedCacheConfig = new MutableConfiguration<String, GBFSBase>();
        var redissonFeedCacheConfig = RedissonConfiguration.fromConfig(config, feedCacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(GBFS_FEED_CACHE_KEY, redissonFeedCacheConfig);
    }
}
