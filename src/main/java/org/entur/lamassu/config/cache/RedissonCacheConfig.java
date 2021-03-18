package org.entur.lamassu.config.cache;

import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.PricingSegment;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.RentalApp;
import org.entur.lamassu.model.entities.RentalApps;
import org.entur.lamassu.model.entities.RentalUris;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.entur.lamassu.model.gbfs.v2_1.GBFSVersions;
import org.entur.lamassu.model.gbfs.v2_1.GeofencingZones;
import org.entur.lamassu.model.gbfs.v2_1.MultiPolygon;
import org.entur.lamassu.model.gbfs.v2_1.StationInformation;
import org.entur.lamassu.model.gbfs.v2_1.StationStatus;
import org.entur.lamassu.model.gbfs.v2_1.SystemAlerts;
import org.entur.lamassu.model.gbfs.v2_1.SystemCalendar;
import org.entur.lamassu.model.gbfs.v2_1.SystemHours;
import org.entur.lamassu.model.gbfs.v2_1.SystemInformation;
import org.entur.lamassu.model.gbfs.v2_1.SystemPricingPlans;
import org.entur.lamassu.model.gbfs.v2_1.SystemRegions;
import org.entur.lamassu.model.gbfs.v2_1.VehicleTypes;
import org.redisson.Redisson;
import org.redisson.api.RGeo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.codec.KryoCodec;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Configuration
public class RedissonCacheConfig {
    public static final String GBFS_FEED_CACHE_KEY = "gbfsCache";
    public static final String VEHICLE_CACHE_KEY = "vehicleCache";
    public static final String VEHICLE_TYPE_CACHE_KEY = "vehicleTypeCache";
    public static final String PRICING_PLAN_CACHE_KEY = "pricingPlanCache";
    public static final String SYSTEM_CACHE_KEY = "systemCache";
    public static final String VEHICLE_SPATIAL_INDEX_KEY = "vehicleSpatialIndex";
    public static final String FEED_UPDATE_SCHEDULER_LOCK = "leader";

    private final RedisProperties redisProperties;

    public RedissonCacheConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    public Config redissonConfig() {
        Config redissonConfig = new Config();

        var codec = new KryoCodec(List.of(
            FreeBikeStatus.class,
            FreeBikeStatus.Data.class,
            FreeBikeStatus.Bike.class,
            GBFS.class,
            GBFS.Data.class,
            GBFS.GBFSFeed.class,
            GBFSFeedName.class,
            GBFSVersions.class,
            GBFSVersions.Data.class,
            GBFSVersions.VersionDetail.class,
            GeofencingZones.class,
            GeofencingZones.Data.class,
            GeofencingZones.Feature.class,
            GeofencingZones.FeatureCollection.class,
            GeofencingZones.Properties.class,
            GeofencingZones.Rule.class,
            MultiPolygon.class,
            org.entur.lamassu.model.gbfs.v2_1.RentalUris.class,
            StationInformation.class,
            StationInformation.Data.class,
            StationInformation.Station.class,
            StationInformation.RentalMethod.class,
            StationStatus.class,
            StationStatus.Data.class,
            StationStatus.Station.class,
            StationStatus.VehicleDockAvailability.class,
            StationStatus.VehicleTypeAvailability.class,
            SystemAlerts.class,
            SystemAlerts.Data.class,
            SystemAlerts.Alert.class,
            SystemAlerts.AlertTime.class,
            SystemAlerts.AlertType.class,
            SystemCalendar.class,
            SystemCalendar.Calendar.class,
            SystemCalendar.Data.class,
            SystemHours.class,
            SystemHours.Data.class,
            SystemHours.RentalHour.class,
            SystemHours.UserType.class,
            SystemHours.WeekDay.class,
            SystemInformation.class,
            SystemInformation.Data.class,
            SystemInformation.RentalApp.class,
            SystemInformation.RentalApps.class,
            SystemPricingPlans.class,
            SystemPricingPlans.Data.class,
            SystemPricingPlans.PricingSegment.class,
            SystemPricingPlans.Plan.class,
            SystemRegions.class,
            SystemRegions.Data.class,
            SystemRegions.Region.class,
            VehicleTypes.class,
            VehicleTypes.Data.class,
            VehicleTypes.VehicleType.class,
            VehicleTypes.PropulsionType.class,
            VehicleTypes.FormFactor.class,
            HashMap.class,
            LinkedHashMap.class,
            ArrayList.class,
            Vehicle.class,
            PricingPlan.class,
            PricingSegment.class,
            VehicleType.class,
            FormFactor.class,
            PropulsionType.class,
            System.class,
            RentalApps.class,
            RentalApp.class,
            RentalUris.class,
            ArrayList.class
        ));

        redissonConfig.setCodec(codec);

        String address = String.format(
                "redis://%s:%s",
                redisProperties.getHost(),
                redisProperties.getPort()
        );
        redissonConfig.useSingleServer()
                .setAddress(address);
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
        cacheConfig.setExpiryPolicyFactory((Factory<ExpiryPolicy>) () -> new CustomExpiryPolicy(Duration.ONE_MINUTE, null, Duration.ONE_MINUTE));
        var redissonCacheConfig = RedissonConfiguration.fromConfig(redissonConfig, cacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(VEHICLE_CACHE_KEY, redissonCacheConfig);
    }

    @Bean
    public Cache<String, VehicleType> vehicleTypeCache(Config redissonConfig) {
        var cacheConfig = new MutableConfiguration<String, VehicleType>();
        cacheConfig.setExpiryPolicyFactory((Factory<ExpiryPolicy>) () -> new CustomExpiryPolicy(Duration.ONE_MINUTE, null, Duration.ONE_MINUTE));
        var redissonCacheConfig = RedissonConfiguration.fromConfig(redissonConfig, cacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(VEHICLE_TYPE_CACHE_KEY, redissonCacheConfig);
    }

    @Bean
    public Cache<String, PricingPlan> pricingPlanCache(Config redissonConfig) {
        var cacheConfig = new MutableConfiguration<String, PricingPlan>();
        cacheConfig.setExpiryPolicyFactory((Factory<ExpiryPolicy>) () -> new CustomExpiryPolicy(Duration.ONE_MINUTE, null, Duration.ONE_MINUTE));
        var redissonCacheConfig = RedissonConfiguration.fromConfig(redissonConfig, cacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(PRICING_PLAN_CACHE_KEY, redissonCacheConfig);
    }

    @Bean
    public Cache<String, System> systemCache(Config redissonConfig) {
        var cacheConfig = new MutableConfiguration<String, System>();
        cacheConfig.setExpiryPolicyFactory((Factory<ExpiryPolicy>) () -> new CustomExpiryPolicy(Duration.ONE_MINUTE, null, Duration.ONE_MINUTE));
        var redissonCacheConfig = RedissonConfiguration.fromConfig(redissonConfig, cacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache(SYSTEM_CACHE_KEY, redissonCacheConfig);
    }

    @Bean
    public RGeo<String> spatialIndex(RedissonClient redissonClient) {
        return redissonClient.getGeo(VEHICLE_SPATIAL_INDEX_KEY);
    }
}
