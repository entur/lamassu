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
import org.entur.gbfs.mapper.GBFSMapper;
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
  private GbfsSubscriptionManager subscriptionManager;
  private ForkJoinPool updaterThreadPool;
  private final RListMultimap<String, ValidationResult> validationResultsCache;

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
    MetricsService metricsService
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
  }

  public void start() {
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
    updaterThreadPool.shutdown();
  }

  private void createSubscriptions() {
    feedProviderConfig.getProviders().parallelStream().forEach(this::createSubscription);
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
}
