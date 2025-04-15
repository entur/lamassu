/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.leader;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import org.entur.gbfs.GbfsSubscriptionManager;
import org.entur.gbfs.GbfsSubscriptionOptions;
import org.entur.gbfs.loader.v2.GbfsV2Delivery;
import org.entur.gbfs.loader.v3.GbfsV3Delivery;
import org.entur.gbfs.validation.model.ValidationResult;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.leader.entityupdater.EntityCachesUpdater;
import org.entur.lamassu.leader.feedcachesupdater.V2FeedCachesUpdater;
import org.entur.lamassu.leader.feedcachesupdater.V3FeedCachesUpdater;
import org.entur.lamassu.mapper.feedmapper.GbfsFeedVersionMappers;
import org.entur.lamassu.mapper.feedmapper.v2.GbfsV2DeliveryMapper;
import org.entur.lamassu.mapper.feedmapper.v3.GbfsV3DeliveryMapper;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.provider.FeedProvider;
import org.redisson.api.RBucket;
import org.redisson.api.RListMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * This class subscribes to all GBFS feeds and dispatches updates to feed cache updaters
 * and entity updaters
 */
@Component
@Profile("leader")
public class FeedUpdater {

  private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
  private static final int SUBSCRIPTION_SETUP_RETRY_DELAY_SECONDS = 60;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final FeedProviderConfig feedProviderConfig;
  private final GbfsV2DeliveryMapper gbfsV2DeliveryMapper;
  private final GbfsV3DeliveryMapper gbfsV3DeliveryMapper;
  private final V2FeedCachesUpdater v2FeedCachesUpdater;
  private final V3FeedCachesUpdater v3FeedCachesUpdater;
  private final EntityCachesUpdater entityCachesUpdater;
  private final RBucket<Boolean> cacheReady;
  private final SubscriptionRegistry subscriptionRegistry;
  private GbfsSubscriptionManager subscriptionManager;
  private ForkJoinPool updaterThreadPool;
  private final RListMultimap<String, ValidationResult> validationResultsCache;
  private final CacheCleanupService cacheCleanupService;

  @Value("${org.entur.lamassu.enableValidation:false}")
  private boolean enableValidation;

  @Value("${org.entur.lamassu.maxValidationResultsPerSystem:10}")
  private Integer maxValidationResultsPerSystem;

  private MetricsService metricsService;

  @Autowired
  public FeedUpdater(
    FeedProviderConfig feedProviderConfig,
    GbfsV2DeliveryMapper gbfsV2DeliveryMapper,
    GbfsV3DeliveryMapper gbfsV3DeliveryMapper,
    V2FeedCachesUpdater v2FeedCachesUpdater,
    V3FeedCachesUpdater v3FeedCachesUpdater,
    EntityCachesUpdater entityCachesUpdater,
    RListMultimap<String, ValidationResult> validationResultsCache,
    RBucket<Boolean> cacheReady,
    MetricsService metricsService,
    CacheCleanupService cacheCleanupService,
    SubscriptionRegistry subscriptionRegistry
  ) {
    this.feedProviderConfig = feedProviderConfig;
    this.gbfsV2DeliveryMapper = gbfsV2DeliveryMapper;
    this.gbfsV3DeliveryMapper = gbfsV3DeliveryMapper;
    this.v2FeedCachesUpdater = v2FeedCachesUpdater;
    this.v3FeedCachesUpdater = v3FeedCachesUpdater;
    this.entityCachesUpdater = entityCachesUpdater;
    this.validationResultsCache = validationResultsCache;
    this.cacheReady = cacheReady;
    this.metricsService = metricsService;
    this.cacheCleanupService = cacheCleanupService;
    this.subscriptionRegistry = subscriptionRegistry;
  }

  public void start() {
    cacheCleanupService.clearCache();
    subscriptionRegistry.clear(); // Clear any existing subscription registrations
    updaterThreadPool =
      new ForkJoinPool(
        NUM_CORES * 2,
        ForkJoinPool.defaultForkJoinWorkerThreadFactory,
        (thread, exception) -> logger.warn("Caught exception in ForkJoinPool", exception),
        true
      );
    subscriptionManager = new GbfsSubscriptionManager(updaterThreadPool);
    updaterThreadPool.execute(this::createSubscriptions);
  }

  public void update() {
    subscriptionManager.update();
  }

  public void stop() {
    subscriptionRegistry.clear();
    updaterThreadPool.shutdown();
  }

  private void createSubscriptions() {
    feedProviderConfig
      .getProviders()
      .parallelStream()
      .filter(feedProvider -> Boolean.TRUE.equals(feedProvider.getEnabled()))
      .forEach(this::createSubscription);
  }

  private void createSubscription(FeedProvider feedProvider) {
    var options = new GbfsSubscriptionOptions(
      URI.create(feedProvider.getUrl()),
      feedProvider.getLanguage(),
      null,
      null,
      feedProvider.getAuthentication() != null
        ? feedProvider.getAuthentication().getRequestAuthenticator()
        : null,
      null,
      enableValidation
    );

    var interceptor = new LoggingSubscriptionUpdateInterceptor(feedProvider);

    String id;

    if (feedProvider.getVersion() != null && feedProvider.getVersion().startsWith("3")) {
      id =
        subscriptionManager.subscribeV3(
          options,
          gbfsV3Delivery -> {
            registerV3Validation(feedProvider, gbfsV3Delivery);
            receiveV2Update(
              feedProvider,
              GbfsFeedVersionMappers.map(gbfsV3Delivery, feedProvider.getLanguage())
            );
            receiveV3Update(feedProvider, gbfsV3Delivery);
            cacheReady.set(true);
          },
          interceptor
        );
    } else {
      id =
        subscriptionManager.subscribeV2(
          options,
          gbfsV2Delivery -> {
            registerV2Validation(feedProvider, gbfsV2Delivery);
            receiveV2Update(feedProvider, gbfsV2Delivery);
            receiveV3Update(
              feedProvider,
              GbfsFeedVersionMappers.map(gbfsV2Delivery, feedProvider.getLanguage())
            );
            cacheReady.set(true);
          },
          interceptor
        );
    }

    if (id == null) {
      logger.warn(
        "Failed to setup subscription, trying again in {} seconds - systemId={}",
        SUBSCRIPTION_SETUP_RETRY_DELAY_SECONDS,
        feedProvider.getSystemId()
      );
      metricsService.registerSubscriptionSetup(feedProvider, false);
      CompletableFuture
        .delayedExecutor(SUBSCRIPTION_SETUP_RETRY_DELAY_SECONDS, TimeUnit.SECONDS)
        .execute(() -> updaterThreadPool.execute(() -> createSubscription(feedProvider)));
    } else {
      logger.info("Setup subscription complete systemId={}", feedProvider.getSystemId());
      metricsService.registerSubscriptionSetup(feedProvider, true);
      // Register the subscription in the registry
      subscriptionRegistry.registerSubscription(feedProvider.getSystemId(), id);
      // after registration, immediately update the feed
      subscriptionManager.update(id);
    }
  }

  private void registerV2Validation(
    FeedProvider feedProvider,
    GbfsV2Delivery gbfsV2Delivery
  ) {
    if (enableValidation) {
      if (gbfsV2Delivery.validationResult().summary().errorsCount() > 0) {
        logger.info(
          "Validation errors in feed update for system {}",
          feedProvider.getSystemId()
        );
      }
      updateValidationReportsCache(
        feedProvider.getSystemId(),
        gbfsV2Delivery.validationResult()
      );
      metricsService.registerValidationResult(
        feedProvider,
        gbfsV2Delivery.validationResult()
      );
    }
  }

  private void registerV3Validation(
    FeedProvider feedProvider,
    GbfsV3Delivery gbfsV3Delivery
  ) {
    if (enableValidation) {
      if (gbfsV3Delivery.validationResult().summary().errorsCount() > 0) {
        logger.info(
          "Validation errors in feed update for system {}",
          feedProvider.getSystemId()
        );
      }
      updateValidationReportsCache(
        feedProvider.getSystemId(),
        gbfsV3Delivery.validationResult()
      );
      metricsService.registerValidationResult(
        feedProvider,
        gbfsV3Delivery.validationResult()
      );
    }
  }

  private void updateValidationReportsCache(
    String systemId,
    ValidationResult validationResult
  ) {
    var validationResults = validationResultsCache.get(systemId);
    var mostRecent = validationResults.get(validationResults.size() - 1);

    if (validationResult.sameAs(mostRecent)) {
      validationResults.fastSet(validationResults.size() - 1, validationResult);
    } else {
      validationResults.add(validationResult);

      // Configurable maximum history?
      if (validationResults.size() > maxValidationResultsPerSystem) {
        validationResults.removeFirst();
      }
    }
  }

  private void receiveV2Update(FeedProvider feedProvider, GbfsV2Delivery gbfsV2Delivery) {
    var mappedDelivery = gbfsV2DeliveryMapper.mapGbfsDelivery(
      gbfsV2Delivery,
      feedProvider
    );
    v2FeedCachesUpdater.updateFeedCaches(feedProvider, mappedDelivery);
  }

  private void receiveV3Update(FeedProvider feedProvider, GbfsV3Delivery gbfsV3Delivery) {
    var mappedDelivery = gbfsV3DeliveryMapper.mapGbfsDelivery(
      gbfsV3Delivery,
      feedProvider
    );
    var oldDelivery = v3FeedCachesUpdater.updateFeedCaches(feedProvider, mappedDelivery);
    if (Boolean.TRUE.equals(feedProvider.getAggregate())) {
      entityCachesUpdater.updateEntityCaches(feedProvider, mappedDelivery, oldDelivery);
    }
  }

  /**
   * Stops a subscription for a feed provider.
   * If the feed provider doesn't have an active subscription, this is a no-op.
   *
   * @param feedProvider The feed provider to stop the subscription for
   * @return true if the subscription was stopped or wasn't running, false if there was an error
   */
  public boolean stopSubscription(FeedProvider feedProvider) {
    // Disable the feed provider
    feedProvider.setEnabled(false);

    // Check if a subscription exists
    String subscriptionId = subscriptionRegistry.getSubscriptionIdBySystemId(
      feedProvider.getSystemId()
    );
    if (subscriptionId == null) {
      // No subscription to stop
      subscriptionRegistry.updateSubscriptionStatus(
        feedProvider.getSystemId(),
        SubscriptionStatus.STOPPED
      );
      return true;
    }

    // Update status to STOPPING
    subscriptionRegistry.updateSubscriptionStatus(
      feedProvider.getSystemId(),
      SubscriptionStatus.STOPPING
    );

    try {
      // Unsubscribe from the feed
      subscriptionManager.unsubscribe(subscriptionId);

      // Remove from registry
      subscriptionRegistry.removeSubscription(feedProvider.getSystemId());

      // Clean up any cached data
      cacheCleanupService.clearCacheForSystem(feedProvider.getSystemId());

      // Update status
      subscriptionRegistry.updateSubscriptionStatus(
        feedProvider.getSystemId(),
        SubscriptionStatus.STOPPED
      );

      logger.info("Stopped subscription for systemId={}", feedProvider.getSystemId());
      return true;
    } catch (Exception e) {
      logger.error(
        "Error stopping subscription for systemId={}",
        feedProvider.getSystemId(),
        e
      );
      // Set status back to STARTED if there was an error
      subscriptionRegistry.updateSubscriptionStatus(
        feedProvider.getSystemId(),
        SubscriptionStatus.STARTED
      );
      return false;
    }
  }

  /**
   * Starts a subscription for a feed provider.
   * If the feed provider already has an active subscription, this is a no-op.
   *
   * @param feedProvider The feed provider to start the subscription for
   * @return true if the subscription was started or is already running, false otherwise
   */
  public boolean startSubscription(FeedProvider feedProvider) {
    // Check if a subscription already exists
    if (subscriptionRegistry.hasSubscription(feedProvider.getSystemId())) {
      return true; // Already has a subscription
    }

    // Enable the feed provider
    feedProvider.setEnabled(true);

    // Update status to STARTING
    subscriptionRegistry.updateSubscriptionStatus(
      feedProvider.getSystemId(),
      SubscriptionStatus.STARTING
    );

    // Create the subscription in a separate thread
    updaterThreadPool.execute(() -> {
      try {
        createSubscription(feedProvider);
        // If we get here, the subscription was created successfully
      } catch (Exception e) {
        // If there was an error, update the status
        subscriptionRegistry.updateSubscriptionStatus(
          feedProvider.getSystemId(),
          SubscriptionStatus.STOPPED
        );
        logger.error(
          "Failed to start subscription for systemId={}",
          feedProvider.getSystemId(),
          e
        );
      }
    });

    return true;
  }

  /**
   * Restarts a subscription for a feed provider.
   * This will stop the subscription if it exists and then start a new one.
   *
   * @param feedProvider The feed provider to restart the subscription for
   * @return true if the subscription was restarted, false otherwise
   */
  public boolean restartSubscription(FeedProvider feedProvider) {
    // Make sure the feed provider is enabled
    feedProvider.setEnabled(true);

    // Update status to STARTING
    subscriptionRegistry.updateSubscriptionStatus(
      feedProvider.getSystemId(),
      SubscriptionStatus.STARTING
    );

    // Stop any existing subscription
    String subscriptionId = subscriptionRegistry.getSubscriptionIdBySystemId(
      feedProvider.getSystemId()
    );
    if (subscriptionId != null) {
      try {
        subscriptionManager.unsubscribe(subscriptionId);
        subscriptionRegistry.removeSubscription(feedProvider.getSystemId());
      } catch (Exception e) {
        logger.warn(
          "Error unsubscribing during restart for systemId={}",
          feedProvider.getSystemId(),
          e
        );
        // Continue with restart even if unsubscribe fails
      }
    }

    // Create a new subscription in a separate thread
    updaterThreadPool.execute(() -> {
      try {
        createSubscription(feedProvider);
        logger.info("Restarted subscription for systemId={}", feedProvider.getSystemId());
      } catch (Exception e) {
        subscriptionRegistry.updateSubscriptionStatus(
          feedProvider.getSystemId(),
          SubscriptionStatus.STOPPED
        );
        logger.error(
          "Failed to restart subscription for systemId={}",
          feedProvider.getSystemId(),
          e
        );
      }
    });

    return true;
  }

  /**
   * Gets the subscription registry.
   *
   * @return The subscription registry
   */
  public SubscriptionRegistry getSubscriptionRegistry() {
    return subscriptionRegistry;
  }
}
