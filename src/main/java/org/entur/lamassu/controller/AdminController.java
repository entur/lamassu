package org.entur.lamassu.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.entur.lamassu.cache.CacheManagementService;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.leader.FeedUpdater;
import org.entur.lamassu.leader.SubscriptionStatus;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.GeoSearchService;
import org.redisson.api.RFuture;
import org.redisson.api.RMapCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(
  name = "org.entur.lamassu.enable-admin-endpoints",
  havingValue = "true"
)
public class AdminController {

  private final GeoSearchService geoSearchService;
  private final FeedProviderConfig feedProviderConfig;
  private final FeedUpdater feedUpdater;
  private final RMapCache<String, Vehicle> vehicleCache;
  private final CacheManagementService cacheManagementService;

  public AdminController(
    GeoSearchService geoSearchService,
    RMapCache<String, Vehicle> vehicleCache,
    FeedProviderConfig feedProviderConfig,
    FeedUpdater feedUpdater,
    CacheManagementService cacheManagementService
  ) {
    this.geoSearchService = geoSearchService;
    this.vehicleCache = vehicleCache;
    this.feedProviderConfig = feedProviderConfig;
    this.feedUpdater = feedUpdater;
    this.cacheManagementService = cacheManagementService;
  }

  @GetMapping("/cache_keys")
  public Collection<String> getCacheKeys() {
    return cacheManagementService.getCacheKeys();
  }

  @PostMapping("/clear_vehicle_cache")
  public Integer clearVehicleCache() {
    var keys = vehicleCache.keySet();
    keys.forEach(vehicleCache::remove);
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
    return cacheManagementService.clearAllCaches();
  }

  @PostMapping("/clear_old_cache")
  public List<String> clearOldCache() {
    return cacheManagementService.clearOldCaches();
  }

  /**
   * Feed provider management endpoints
   */

  @GetMapping("/feed-providers")
  public ResponseEntity<List<FeedProvider>> getAllFeedProviders() {
    return ResponseEntity.ok(feedProviderConfig.getProviders());
  }

  @GetMapping("/feed-providers/{systemId}")
  public ResponseEntity<FeedProvider> getFeedProviderBySystemId(
    @PathVariable String systemId
  ) {
    FeedProvider provider = feedProviderConfig.getProviderBySystemId(systemId);
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

    boolean success = feedProviderConfig.addProvider(feedProvider);
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

    boolean success = feedProviderConfig.updateProvider(feedProvider);
    if (!success) {
      return ResponseEntity.notFound().build(); // Provider with this systemId not found
    }

    return ResponseEntity.ok(feedProvider);
  }

  @DeleteMapping("/feed-providers/{systemId}")
  public ResponseEntity<Void> deleteFeedProvider(@PathVariable String systemId) {
    boolean success = feedProviderConfig.deleteProvider(systemId);
    if (!success) {
      return ResponseEntity.notFound().build(); // Provider with this systemId not found
    }

    return ResponseEntity.noContent().build();
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
    FeedProvider provider = feedProviderConfig.getProviderBySystemId(systemId);
    if (provider == null) {
      return ResponseEntity.notFound().build();
    }

    // Update the enabled status
    provider.setEnabled(enabled);

    // If disabling, also stop any active subscription
    if (Boolean.FALSE.equals(enabled)) {
      feedUpdater.stopSubscription(provider);
    }

    // Update the provider in Redis
    feedProviderConfig.updateProvider(provider);

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
    FeedProvider provider = feedProviderConfig.getProviderBySystemId(systemId);
    if (provider == null) {
      return ResponseEntity.notFound().build();
    }

    boolean success = feedUpdater.startSubscription(provider);
    if (success) {
      // Update the provider in Redis with the new enabled status
      feedProviderConfig.updateProvider(provider);
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
    FeedProvider provider = feedProviderConfig.getProviderBySystemId(systemId);
    if (provider == null) {
      return ResponseEntity.notFound().build();
    }

    boolean success = feedUpdater.stopSubscription(provider);
    if (success) {
      // Update the provider in Redis with the new enabled status
      feedProviderConfig.updateProvider(provider);
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
    FeedProvider provider = feedProviderConfig.getProviderBySystemId(systemId);
    if (provider == null) {
      return ResponseEntity.notFound().build();
    }

    boolean success = feedUpdater.restartSubscription(provider);
    if (success) {
      // Update the provider in Redis with the new enabled status
      feedProviderConfig.updateProvider(provider);
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

  /**
   * Bulk operations endpoints
   */

  /**
   * Starts subscriptions for multiple feed providers.
   *
   * @param systemIds List of system IDs
   * @return Map of system IDs to success/error status
   */
  @PostMapping("/feed-providers/bulk/start")
  public ResponseEntity<Map<String, String>> bulkStartSubscriptions(
    @RequestBody List<String> systemIds
  ) {
    Map<String, String> results = new java.util.HashMap<>();

    for (String systemId : systemIds) {
      FeedProvider provider = feedProviderConfig.getProviderBySystemId(systemId);
      if (provider == null) {
        results.put(systemId, "NOT_FOUND");
        continue;
      }

      boolean success = feedUpdater.startSubscription(provider);
      if (success) {
        feedProviderConfig.updateProvider(provider);
        results.put(systemId, "SUCCESS");
      } else {
        results.put(systemId, "FAILED");
      }
    }

    return ResponseEntity.ok(results);
  }

  /**
   * Stops subscriptions for multiple feed providers.
   *
   * @param systemIds List of system IDs
   * @return Map of system IDs to success/error status
   */
  @PostMapping("/feed-providers/bulk/stop")
  public ResponseEntity<Map<String, String>> bulkStopSubscriptions(
    @RequestBody List<String> systemIds
  ) {
    Map<String, String> results = new java.util.HashMap<>();

    for (String systemId : systemIds) {
      FeedProvider provider = feedProviderConfig.getProviderBySystemId(systemId);
      if (provider == null) {
        results.put(systemId, "NOT_FOUND");
        continue;
      }

      boolean success = feedUpdater.stopSubscription(provider);
      if (success) {
        feedProviderConfig.updateProvider(provider);
        results.put(systemId, "SUCCESS");
      } else {
        results.put(systemId, "FAILED");
      }
    }

    return ResponseEntity.ok(results);
  }

  /**
   * Restarts subscriptions for multiple feed providers.
   *
   * @param systemIds List of system IDs
   * @return Map of system IDs to success/error status
   */
  @PostMapping("/feed-providers/bulk/restart")
  public ResponseEntity<Map<String, String>> bulkRestartSubscriptions(
    @RequestBody List<String> systemIds
  ) {
    Map<String, String> results = new java.util.HashMap<>();

    for (String systemId : systemIds) {
      FeedProvider provider = feedProviderConfig.getProviderBySystemId(systemId);
      if (provider == null) {
        results.put(systemId, "NOT_FOUND");
        continue;
      }

      boolean success = feedUpdater.restartSubscription(provider);
      if (success) {
        feedProviderConfig.updateProvider(provider);
        results.put(systemId, "SUCCESS");
      } else {
        results.put(systemId, "FAILED");
      }
    }

    return ResponseEntity.ok(results);
  }

  /**
   * Enables or disables multiple feed providers.
   *
   * @param request Object containing systemIds list and enabled boolean
   * @return Map of system IDs to success/error status
   */
  @PostMapping("/feed-providers/bulk/set-enabled")
  public ResponseEntity<Map<String, String>> bulkSetEnabled(
    @RequestBody BulkSetEnabledRequest request
  ) {
    Map<String, String> results = new java.util.HashMap<>();

    for (String systemId : request.systemIds()) {
      FeedProvider provider = feedProviderConfig.getProviderBySystemId(systemId);
      if (provider == null) {
        results.put(systemId, "NOT_FOUND");
        continue;
      }

      provider.setEnabled(request.enabled());

      // If disabling, also stop any active subscription
      if (Boolean.FALSE.equals(request.enabled())) {
        feedUpdater.stopSubscription(provider);
      }

      feedProviderConfig.updateProvider(provider);
      results.put(systemId, "SUCCESS");
    }

    return ResponseEntity.ok(results);
  }

  /**
   * Request object for bulk set-enabled operation
   */
  public record BulkSetEnabledRequest(List<String> systemIds, Boolean enabled) {}
}
