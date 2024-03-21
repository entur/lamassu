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
import org.entur.lamassu.leader.feedcachesupdater.FeedCachesUpdater;
import org.entur.lamassu.mapper.feedmapper.GbfsDeliveryMapper;
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

@Component
@Profile("leader")
public class FeedUpdater {

  private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
  private static final int SUBSCRIPTION_SETUP_RETRY_DELAY_SECONDS = 60;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final FeedProviderConfig feedProviderConfig;
  private final GbfsDeliveryMapper gbfsDeliveryMapper;
  private final FeedCachesUpdater feedCachesUpdater;
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
    GbfsDeliveryMapper gbfsDeliveryMapper,
    FeedCachesUpdater feedCachesUpdater,
    EntityCachesUpdater entityCachesUpdater,
    RListMultimap<String, ValidationResult> validationResultsCache,
    RBucket<Boolean> cacheReady,
    MetricsService metricsService
  ) {
    this.feedProviderConfig = feedProviderConfig;
    this.gbfsDeliveryMapper = gbfsDeliveryMapper;
    this.feedCachesUpdater = feedCachesUpdater;
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
    var options = new GbfsSubscriptionOptions();
    options.setDiscoveryURI(URI.create(feedProvider.getUrl()));
    options.setLanguageCode(feedProvider.getLanguage());
    if (feedProvider.getAuthentication() != null) {
      options.setRequestAuthenticator(
        feedProvider.getAuthentication().getRequestAuthenticator()
      );
    }
    options.setEnableValidation(enableValidation);

    String id = null;

    if (feedProvider.getVersion() != null && feedProvider.getVersion().startsWith("3")) {
      id =
        subscriptionManager.subscribeV3(
          options,
          gbfsV3Delivery ->
            receiveUpdate(feedProvider, map(gbfsV3Delivery, feedProvider.getLanguage()))
        );
    } else {
      id =
        subscriptionManager.subscribeV2(
          options,
          delivery -> receiveUpdate(feedProvider, delivery)
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
    }
  }

  private GbfsV2Delivery map(GbfsV3Delivery source, String languageCode) {
    return new GbfsV2Delivery(
      GBFSMapper.INSTANCE.map(source.discovery(), languageCode),
      null,
      GBFSMapper.INSTANCE.map(source.systemInformation(), languageCode),
      GBFSMapper.INSTANCE.map(source.vehicleTypes(), languageCode),
      GBFSMapper.INSTANCE.map(source.stationInformation(), languageCode),
      GBFSMapper.INSTANCE.map(source.stationStatus(), languageCode),
      GBFSMapper.INSTANCE.map(source.vehicleStatus(), languageCode),
      null,
      null,
      GBFSMapper.INSTANCE.map(source.systemRegions(), languageCode),
      GBFSMapper.INSTANCE.map(source.systemPricingPlans(), languageCode),
      GBFSMapper.INSTANCE.map(source.systemAlerts(), languageCode),
      GBFSMapper.INSTANCE.map(source.geofencingZones(), languageCode),
      source.validationResult()
    );
  }

  private void receiveUpdate(FeedProvider feedProvider, GbfsV2Delivery delivery) {
    if (enableValidation) {
      if (delivery.validationResult().getSummary().getErrorsCount() > 0) {
        logger.info(
          "Validation errors in feed update for system {}",
          feedProvider.getSystemId()
        );
      }
      updateValidationReportsCache(
        feedProvider.getSystemId(),
        delivery.validationResult()
      );
      metricsService.registerValidationResult(feedProvider, delivery.validationResult());
    }

    var mappedDelivery = gbfsDeliveryMapper.mapGbfsDelivery(delivery, feedProvider);
    var oldDelivery = feedCachesUpdater.updateFeedCaches(feedProvider, mappedDelivery);
    if (Boolean.TRUE.equals(feedProvider.getAggregate())) {
      entityCachesUpdater.updateEntityCaches(feedProvider, mappedDelivery, oldDelivery);
    }
    cacheReady.set(true);
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
        validationResults.remove(0);
      }
    }
  }
}
