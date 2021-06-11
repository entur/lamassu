package org.entur.lamassu.config.cache;

import org.entur.lamassu.model.entities.GeofencingZones;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.entities.Vehicle;
import org.redisson.Redisson;
import org.redisson.api.RGeo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.codec.Kryo5Codec;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;

@Configuration
public class RedissonCacheConfig {
    public static final String GBFS_FEED_CACHE_KEY = "gbfsCache";
    public static final String VEHICLE_CACHE_KEY = "vehicleCache";
    public static final String VEHICLE_TYPE_CACHE_KEY = "vehicleTypeCache";
    public static final String PRICING_PLAN_CACHE_KEY = "pricingPlanCache";
    public static final String SYSTEM_CACHE_KEY = "systemCache";
    public static final String STATION_CACHE_KEY = "stationCache";
    public static final String GEOFENCING_ZONES_CACHE_KEY = "geofencingZonesCache";
    public static final String VEHICLE_SPATIAL_INDEX_KEY = "vehicleSpatialIndex";
    public static final String STATION_SPATIAL_INDEX_KEY = "stationSpatialIndex";
    public static final String FEED_UPDATE_SCHEDULER_LOCK = "leader";

    private final Config redissonConfig;

    public RedissonCacheConfig(RedisProperties redisProperties) {
        redissonConfig = new Config();

        var codec = new Kryo5Codec(this.getClass().getClassLoader());

        redissonConfig.setCodec(codec);

        var address = String.format(
                "redis://%s:%s",
                redisProperties.getHost(),
                redisProperties.getPort()
        );
        redissonConfig.useSingleServer()
                .setAddress(address);
    }

    @Bean
    public Config redissonConfig() {
        return redissonConfig;
    }

    @Bean(destroyMethod = "shutdown")
    @Profile("!test")
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
        var cacheConfig = new MutableConfiguration<String, GBFSBase>();
        cacheConfig.setExpiryPolicyFactory((Factory<ExpiryPolicy>) () -> new CustomExpiryPolicy(Duration.ONE_DAY, null, Duration.ONE_DAY));
        var redissonCacheConfig = RedissonConfiguration.fromConfig(redissonConfig, cacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(GBFS_FEED_CACHE_KEY, redissonCacheConfig);
    }

    @Bean
    public Cache<String, Vehicle> vehicleCache(Config redissonConfig) {
        var cacheConfig = new MutableConfiguration<String, Vehicle>();
        cacheConfig.setExpiryPolicyFactory((Factory<ExpiryPolicy>) () -> new CustomExpiryPolicy(Duration.FIVE_MINUTES, null, Duration.FIVE_MINUTES));
        var redissonCacheConfig = RedissonConfiguration.fromConfig(redissonConfig, cacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(VEHICLE_CACHE_KEY, redissonCacheConfig);
    }

    @Bean
    public Cache<String, VehicleType> vehicleTypeCache(Config redissonConfig) {
        var cacheConfig = new MutableConfiguration<String, VehicleType>();
        cacheConfig.setExpiryPolicyFactory((Factory<ExpiryPolicy>) () -> new CustomExpiryPolicy(Duration.ONE_DAY, null, Duration.ONE_DAY));
        var redissonCacheConfig = RedissonConfiguration.fromConfig(redissonConfig, cacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(VEHICLE_TYPE_CACHE_KEY, redissonCacheConfig);
    }

    @Bean
    public Cache<String, PricingPlan> pricingPlanCache(Config redissonConfig) {
        var cacheConfig = new MutableConfiguration<String, PricingPlan>();
        cacheConfig.setExpiryPolicyFactory((Factory<ExpiryPolicy>) () -> new CustomExpiryPolicy(Duration.ONE_DAY, null, Duration.ONE_DAY));
        var redissonCacheConfig = RedissonConfiguration.fromConfig(redissonConfig, cacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(PRICING_PLAN_CACHE_KEY, redissonCacheConfig);
    }

    @Bean
    public Cache<String, System> systemCache(Config redissonConfig) {
        var cacheConfig = new MutableConfiguration<String, System>();
        cacheConfig.setExpiryPolicyFactory((Factory<ExpiryPolicy>) () -> new CustomExpiryPolicy(Duration.ONE_DAY, null, Duration.ONE_DAY));
        var redissonCacheConfig = RedissonConfiguration.fromConfig(redissonConfig, cacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(SYSTEM_CACHE_KEY, redissonCacheConfig);
    }

    @Bean
    public Cache<String, Station> stationCache(Config redissonConfig) {
        var cacheConfig = new MutableConfiguration<String, Station>();
        cacheConfig.setExpiryPolicyFactory((Factory<ExpiryPolicy>) () -> new CustomExpiryPolicy(Duration.ONE_DAY, null, Duration.ONE_DAY));
        var redissonCacheConfig = RedissonConfiguration.fromConfig(redissonConfig, cacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(STATION_CACHE_KEY, redissonCacheConfig);
    }

    @Bean
    public Cache<String, GeofencingZones> geofencingZonesCache(Config redissonConfig) {
        var cacheConfig = new MutableConfiguration<String, GeofencingZones>();
        cacheConfig.setExpiryPolicyFactory((Factory<ExpiryPolicy>) () -> new CustomExpiryPolicy(Duration.ONE_DAY, null, Duration.ONE_DAY));
        var redissonCacheConfig = RedissonConfiguration.fromConfig(redissonConfig, cacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(GEOFENCING_ZONES_CACHE_KEY, redissonCacheConfig);
    }

    @Bean
    public RGeo<String> vehicleSpatialIndex(RedissonClient redissonClient) {
        return redissonClient.getGeo(VEHICLE_SPATIAL_INDEX_KEY);
    }

    @Bean
    public RGeo<String> stationSpatialIndex(RedissonClient redissonClient) {
        return redissonClient.getGeo(STATION_SPATIAL_INDEX_KEY);
    }
}
