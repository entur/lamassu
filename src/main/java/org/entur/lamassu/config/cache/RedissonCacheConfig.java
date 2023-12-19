package org.entur.lamassu.config.cache;

import java.util.Set;
import org.entur.gbfs.validation.model.ValidationResult;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.config.project.LamassuProjectInfoConfiguration;
import org.entur.lamassu.model.entities.GeofencingZones;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RGeo;
import org.redisson.api.RListMultimap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.codec.Kryo5Codec;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class RedissonCacheConfig {

  public static final String GBFS_FEED_CACHE_KEY = "gbfsFeedCache";
  public static final String VEHICLE_CACHE_KEY = "vehicleCache";
  public static final String STATION_CACHE_KEY = "stationCache";
  public static final String GEOFENCING_ZONES_CACHE_KEY = "geofencingZonesCache";
  public static final String VEHICLE_SPATIAL_INDEX_KEY = "vehicleSpatialIndex";
  public static final String STATION_SPATIAL_INDEX_KEY = "stationSpatialIndex";
  public static final String VALIDATION_REPORTS_CACHE_KEY = "validationReportsCache";
  public static final String CACHE_READY_KEY = "cacheReady";

  private final String serializationVersion;
  private final Config redissonConfig;

  public RedissonCacheConfig(
    @Value("${org.entur.lamassu.redis.master.host}") String masterHost,
    @Value("${org.entur.lamassu.redis.master.port}") String masterPort,
    @Value("${org.entur.lamassu.redis.slave.enabled:false}") boolean slaveEnabled,
    @Value("${org.entur.lamassu.redis.slave.host:na}") String slaveHost,
    @Value("${org.entur.lamassu.redis.slave.port:na}") String slavePort,
    LamassuProjectInfoConfiguration lamassuProjectInfoConfiguration
  ) {
    serializationVersion = lamassuProjectInfoConfiguration.getSerializationVersion();

    redissonConfig = new Config();

    var codec = new Kryo5Codec(this.getClass().getClassLoader());

    redissonConfig.setCodec(codec);

    var masterAddress = String.format("redis://%s:%s", masterHost, masterPort);

    if (slaveEnabled) {
      var slaveAddress = String.format("redis://%s:%s", slaveHost, slavePort);

      redissonConfig
        .useMasterSlaveServers()
        .setMasterAddress(masterAddress)
        .setSlaveAddresses(Set.of(slaveAddress));
    } else {
      redissonConfig.useSingleServer().setAddress(masterAddress);
    }
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
  public RMapCache<String, Object> feedCache(RedissonClient redissonClient) {
    return redissonClient.getMapCache(GBFS_FEED_CACHE_KEY + "_" + serializationVersion);
  }

  @Bean
  public RMapCache<String, Vehicle> vehicleCache(RedissonClient redissonClient) {
    return redissonClient.getMapCache(VEHICLE_CACHE_KEY + "_" + serializationVersion);
  }

  @Bean
  public RMapCache<String, Station> stationCache(RedissonClient redissonClient) {
    return redissonClient.getMapCache(STATION_CACHE_KEY + "_" + serializationVersion);
  }

  @Bean
  public RMapCache<String, GeofencingZones> geofencingZonesCache(
    RedissonClient redissonClient
  ) {
    return redissonClient.getMapCache(
      GEOFENCING_ZONES_CACHE_KEY + "_" + serializationVersion
    );
  }

  @Bean
  public RGeo<VehicleSpatialIndexId> vehicleSpatialIndex(RedissonClient redissonClient) {
    return redissonClient.getGeo(VEHICLE_SPATIAL_INDEX_KEY + "_" + serializationVersion);
  }

  @Bean
  public RGeo<StationSpatialIndexId> stationSpatialIndex(RedissonClient redissonClient) {
    return redissonClient.getGeo(STATION_SPATIAL_INDEX_KEY + "_" + serializationVersion);
  }

  @Bean
  public RListMultimap<String, ValidationResult> validationResultsCache(
    RedissonClient redissonClient
  ) {
    return redissonClient.getListMultimap(
      VALIDATION_REPORTS_CACHE_KEY + "_" + serializationVersion
    );
  }

  @Bean
  public RBucket<Boolean> cacheReady(RedissonClient redissonClient) {
    return redissonClient.getBucket(CACHE_READY_KEY + "_" + serializationVersion);
  }
}
