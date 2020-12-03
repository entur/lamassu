package org.entur.lamassu.config.cache;

import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.redisson.Redisson;
import org.redisson.api.RGeo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

@Configuration
public class RedissonCacheConfig {
    public static final String GBFS_FEED_CACHE_KEY = "gbfsCache";
    public static final String VEHICLE_CACHE_KEY = "vehicleCache";
    public static final String SPATIAL_INDEX_KEY = "spatialIndex";
    public static final String FEED_UPDATE_SCHEDULER_LOCK = "leader";

    private final Config config;

    public RedissonCacheConfig(RedisProperties redisProperties) {
        config = new Config();
        config.setCodec(JsonJacksonCodec.INSTANCE);

        String address = String.format(
                "redis://%s:%s",
                redisProperties.getHost(),
                redisProperties.getPort()
        );
        config.useSingleServer()
                .setAddress(address);
    }

    @Bean
    public Config redissonConfig() {
        return config;
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(Config redissonConfig) {
        return Redisson.create(redissonConfig);
    }

    @Bean
    public RLock feedUpdateSchedulerLock(RedissonClient redissonClient) {
        return redissonClient.getLock(FEED_UPDATE_SCHEDULER_LOCK);
    }

    @Bean
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {
        return new RedissonConnectionFactory(redisson);
    }

    @Bean
    public Cache<String, GBFSBase> feedCache(Config redissonConfig) {
        var feedCacheConfig = new MutableConfiguration<String, GBFSBase>();
        feedCacheConfig.setExpiryPolicyFactory(new FeedCacheExpiryPolicyFactory());
        var redissonFeedCacheConfig = RedissonConfiguration.fromConfig(redissonConfig, feedCacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(GBFS_FEED_CACHE_KEY, redissonFeedCacheConfig);
    }

    @Bean
    public Cache<String, FreeBikeStatus.Bike> vehicleCache(Config redissonConfig) {
        var vehicleCacheConfig = new MutableConfiguration<String, FreeBikeStatus.Bike>();
        vehicleCacheConfig.setExpiryPolicyFactory(new VehicleCacheExpiryPolicyFactory());
        var redissonFeedCacheConfig = RedissonConfiguration.fromConfig(redissonConfig, vehicleCacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(VEHICLE_CACHE_KEY, redissonFeedCacheConfig);
    }

    @Bean
    public RGeo<String> spatialIndex(RedissonClient redissonClient) {
        return redissonClient.getGeo(SPATIAL_INDEX_KEY);
    }
}
