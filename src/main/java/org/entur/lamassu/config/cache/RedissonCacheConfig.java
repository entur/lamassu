package org.entur.lamassu.config.cache;

import org.entur.lamassu.model.entities.GeofencingZones;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.redisson.Redisson;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RBucket;
import org.redisson.api.RGeo;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.Kryo5Codec;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

@Configuration
public class RedissonCacheConfig {
    public static final String GBFS_FEED_CACHE_KEY = "gbfsFeedCache";
    public static final String VEHICLE_CACHE_KEY = "vehicleCache";
    public static final String STATION_CACHE_KEY = "stationCache";
    public static final String GEOFENCING_ZONES_CACHE_KEY = "geofencingZonesCache";
    public static final String VEHICLE_SPATIAL_INDEX_KEY = "vehicleSpatialIndex";
    public static final String STATION_SPATIAL_INDEX_KEY = "stationSpatialIndex";
    public static final String CACHE_READY_KEY = "cacheReady";

    @Value("${org.entur.lamassu.serializationVersion}")
    private String serializationVersion;

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
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {
        return new RedissonConnectionFactory(redisson);
    }

    @Bean
    public RLocalCachedMap<String, Object> feedCache(RedissonClient redissonClient) {
        var options = LocalCachedMapOptions.<String, Object>defaults()
                .cacheSize(0)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                .timeToLive(1, TimeUnit.DAYS);

        return redissonClient.getLocalCachedMap(GBFS_FEED_CACHE_KEY + "_" + serializationVersion, options);
    }

    @Bean
    public RLocalCachedMap<String, Vehicle> vehicleCache(RedissonClient redissonClient) {
        var options = LocalCachedMapOptions.<String, Vehicle>defaults()
                .cacheSize(0)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                .timeToLive(5, TimeUnit.MINUTES);

        return redissonClient.getLocalCachedMap(VEHICLE_CACHE_KEY + "_" + serializationVersion, options);
    }

    @Bean
    public RLocalCachedMap<String, Station> stationCache(RedissonClient redissonClient) {
        var options = LocalCachedMapOptions.<String, Station>defaults()
                .cacheSize(0)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                .timeToLive(1, TimeUnit.DAYS);

        return redissonClient.getLocalCachedMap(STATION_CACHE_KEY + "_" + serializationVersion, options);
    }

    @Bean
    public RLocalCachedMap<String, GeofencingZones> geofencingZonesCache(RedissonClient redissonClient) {
        var options = LocalCachedMapOptions.<String, GeofencingZones>defaults()
                .cacheSize(0)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                .timeToLive(1, TimeUnit.DAYS);

        return redissonClient.getLocalCachedMap(GEOFENCING_ZONES_CACHE_KEY + "_" + serializationVersion, options);
    }

    @Bean
    public RGeo<String> vehicleSpatialIndex(RedissonClient redissonClient) {
        return redissonClient.getGeo(VEHICLE_SPATIAL_INDEX_KEY + "_" + serializationVersion);
    }

    @Bean
    public RGeo<String> stationSpatialIndex(RedissonClient redissonClient) {
        return redissonClient.getGeo(STATION_SPATIAL_INDEX_KEY + "_" + serializationVersion);
    }

    @Bean
    public RBucket<Boolean> cacheReady(RedissonClient redissonClient) {
        return redissonClient.getBucket(CACHE_READY_KEY + "_" + serializationVersion);
    }
}
