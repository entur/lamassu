package org.entur.lamassu.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.entur.lamassu.config.feedprovider.FeedProviderConfigFile;
import org.entur.lamassu.config.feedprovider.FeedProviderConfigRedis;
import org.entur.lamassu.config.project.LamassuProjectInfoConfiguration;
import org.entur.lamassu.leader.FeedUpdater;
import org.entur.lamassu.leader.SubscriptionStatus;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.GeoSearchService;
import org.redisson.api.RFuture;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Profile("leader")
public class AdminController {

  private final RedissonClient redissonClient;
  private final GeoSearchService geoSearchService;
  private final FeedProviderConfigRedis feedProviderConfigRedis;
  private final FeedProviderConfigFile feedProviderConfigFile;
  private final FeedUpdater feedUpdater;
  RMapCache<String, Vehicle> vehicleCache;

  private final String serializationVersion;

  public AdminController(
    RedissonClient redissonClient,
    GeoSearchService geoSearchService,
    RMapCache<String, Vehicle> vehicleCache,
    FeedProviderConfigRedis feedProviderConfigRedis,
    FeedProviderConfigFile feedProviderConfigFile,
    FeedUpdater feedUpdater,
    LamassuProjectInfoConfiguration lamassuProjectInfoConfiguration
  ) {
    this.redissonClient = redissonClient;
    this.geoSearchService = geoSearchService;
    this.vehicleCache = vehicleCache;
    this.feedProviderConfigRedis = feedProviderConfigRedis;
    this.feedProviderConfigFile = feedProviderConfigFile;
    this.feedUpdater = feedUpdater;
    this.serializationVersion = lamassuProjectInfoConfiguration.getSerializationVersion();
  }

  @GetMapping("/cache_keys")
  public Collection<String> getCacheKeys() {
    return StreamSupport
      .stream(redissonClient.getKeys().getKeys().spliterator(), false)
      .collect(Collectors.toList());
  }

  @PostMapping("/clear_vehicle_cache")
  public Integer clearVehicleCache() {
    var keys = vehicleCache.keySet();
    keys.forEach(key -> vehicleCache.remove(key));
    return keys.size();
  }

  @GetMapping("/vehicle_orphans")
  public Collection<String> getOrphans() {
    return geoSearchService.getVehicleSpatialIndexOrphans();
  }

  @DeleteMapping("/vehicle_orphans")
  public Collection<String> clearOrphans() {
    return geoSearchService.removeVehicleSpatialIndexOrphans();
  }

  @PostMapping("/clear_db")
  public RFuture<Void> clearDb() {
    return redissonClient.getKeys().flushdbParallelAsync();
  }

  @PostMapping("/clear_old_cache")
  public List<String> clearOldCache() {
    var keys = redissonClient.getKeys();
    List<String> deletedKeys = new ArrayList<>();
    keys
      .getKeys()
      .forEach(key -> {
        if (!key.endsWith("_" + serializationVersion)) {
          keys.delete(key);
          deletedKeys.add(key);
        }
      });
    return deletedKeys;
  }

  /**
   * Feed provider management endpoints
   */

  @GetMapping("/feed-providers")
  public ResponseEntity<List<FeedProvider>> getAllFeedProviders() {
    return ResponseEntity.ok(feedProviderConfigRedis.getProviders());
  }

  @GetMapping("/feed-providers/{systemId}")
  public ResponseEntity<FeedProvider> getFeedProviderBySystemId(
    @PathVariable String systemId
  ) {
    FeedProvider provider = feedProviderConfigRedis.getProviderBySystemId(systemId);
    if (provider == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(provider);
  }

  @PostMapping("/feed-providers")
  public ResponseEntity<FeedProvider> createFeedProvider(
    @RequestBody FeedProvider feedProvider
  ) {
    // Validate the feed provider
    if (feedProvider.getSystemId() == null || feedProvider.getSystemId().isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    boolean success = feedProviderConfigRedis.addProvider(feedProvider);
    if (!success) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Provider with this systemId already exists
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(feedProvider);
  }

  @PutMapping("/feed-providers/{systemId}")
  public ResponseEntity<FeedProvider> updateFeedProvider(
    @PathVariable String systemId,
    @RequestBody FeedProvider feedProvider
  ) {
    // Ensure the systemId in the path matches the one in the body
    if (!systemId.equals(feedProvider.getSystemId())) {
      return ResponseEntity.badRequest().build();
    }

    boolean success = feedProviderConfigRedis.updateProvider(feedProvider);
    if (!success) {
      return ResponseEntity.notFound().build(); // Provider with this systemId not found
    }

    return ResponseEntity.ok(feedProvider);
  }

  @DeleteMapping("/feed-providers/{systemId}")
  public ResponseEntity<Void> deleteFeedProvider(@PathVariable String systemId) {
    boolean success = feedProviderConfigRedis.deleteProvider(systemId);
    if (!success) {
      return ResponseEntity.notFound().build(); // Provider with this systemId not found
    }

    return ResponseEntity.noContent().build();
  }

  /**
   * Utility endpoint to migrate feed providers from file to Redis.
   * This is useful when transitioning from file-based to Redis-based configuration.
   *
   * @return The number of feed providers migrated
   */
  @PostMapping("/feed-providers/migrate-from-file")
  public ResponseEntity<Integer> migrateFeedProvidersFromFile() {
    List<FeedProvider> providers = feedProviderConfigFile.getProviders();
    if (providers == null) {
      providers = new ArrayList<>();
    }

    boolean success = feedProviderConfigRedis.saveProviders(providers);

    if (!success) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.ok(providers.size());
  }

  /**
   * Enables or disables a feed provider.
   * This affects whether a subscription will be started automatically when the app restarts.
   *
   * @param systemId The system ID of the feed provider
   * @param enabled Whether the feed provider should be enabled
   * @return ResponseEntity with success or error status
   */
  @PostMapping("/feed-providers/{systemId}/set-enabled")
  public ResponseEntity<Void> setFeedProviderEnabled(
    @PathVariable String systemId,
    @RequestParam Boolean enabled
  ) {
    FeedProvider provider = feedProviderConfigRedis.getProviderBySystemId(systemId);
    if (provider == null) {
      return ResponseEntity.notFound().build();
    }

    // Update the enabled status
    provider.setEnabled(enabled);

    // If disabling, also stop any active subscription
    if (!enabled) {
      feedUpdater.stopSubscription(provider);
    }

    // Update the provider in Redis
    feedProviderConfigRedis.updateProvider(provider);

    return ResponseEntity.ok().build();
  }

  /**
   * Subscription management endpoints
   */

  /**
   * Starts a subscription for a feed provider.
   *
   * @param systemId The system ID of the feed provider
   * @return ResponseEntity with success or error status
   */
  @PostMapping("/feed-providers/{systemId}/start")
  public ResponseEntity<Void> startSubscription(@PathVariable String systemId) {
    FeedProvider provider = feedProviderConfigRedis.getProviderBySystemId(systemId);
    if (provider == null) {
      return ResponseEntity.notFound().build();
    }

    boolean success = feedUpdater.startSubscription(provider);
    if (success) {
      // Update the provider in Redis with the new enabled status
      feedProviderConfigRedis.updateProvider(provider);
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Stops a subscription for a feed provider.
   *
   * @param systemId The system ID of the feed provider
   * @return ResponseEntity with success or error status
   */
  @PostMapping("/feed-providers/{systemId}/stop")
  public ResponseEntity<Void> stopSubscription(@PathVariable String systemId) {
    FeedProvider provider = feedProviderConfigRedis.getProviderBySystemId(systemId);
    if (provider == null) {
      return ResponseEntity.notFound().build();
    }

    boolean success = feedUpdater.stopSubscription(provider);
    if (success) {
      // Update the provider in Redis with the new enabled status
      feedProviderConfigRedis.updateProvider(provider);
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Restarts a subscription for a feed provider.
   *
   * @param systemId The system ID of the feed provider
   * @return ResponseEntity with success or error status
   */
  @PostMapping("/feed-providers/{systemId}/restart")
  public ResponseEntity<Void> restartSubscription(@PathVariable String systemId) {
    FeedProvider provider = feedProviderConfigRedis.getProviderBySystemId(systemId);
    if (provider == null) {
      return ResponseEntity.notFound().build();
    }

    boolean success = feedUpdater.restartSubscription(provider);
    if (success) {
      // Update the provider in Redis with the new enabled status
      feedProviderConfigRedis.updateProvider(provider);
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Gets the current status of all feed provider subscriptions.
   *
   * @return Map of system IDs to subscription statuses
   */
  @GetMapping("/feed-providers/subscription-statuses")
  public ResponseEntity<Map<String, SubscriptionStatus>> getSubscriptionStatuses() {
    Map<String, SubscriptionStatus> statuses = feedUpdater
      .getSubscriptionRegistry()
      .getAllSubscriptionStatuses();
    return ResponseEntity.ok(statuses);
  }

  /**
   * Gets the current status of a specific feed provider subscription.
   *
   * @param systemId The system ID of the feed provider
   * @return The subscription status
   */
  @GetMapping("/feed-providers/{systemId}/subscription-status")
  public ResponseEntity<SubscriptionStatus> getSubscriptionStatus(
    @PathVariable String systemId
  ) {
    SubscriptionStatus status = feedUpdater
      .getSubscriptionRegistry()
      .getSubscriptionStatusBySystemId(systemId);
    return ResponseEntity.ok(status);
  }
}
