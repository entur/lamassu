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

import org.entur.gbfs.GbfsDelivery;
import org.entur.gbfs.GbfsSubscriptionManager;
import org.entur.gbfs.GbfsSubscriptionOptions;
import org.entur.gbfs.validation.model.ValidationResult;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.leader.entityupdater.EntityCachesUpdater;
import org.entur.lamassu.leader.feedcachesupdater.FeedCachesUpdater;
import org.entur.lamassu.mapper.feedmapper.GbfsDeliveryMapper;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.provider.FeedProvider;
import org.redisson.api.RBucket;
import org.redisson.api.RMapCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Component
@Profile("leader")
public class FeedUpdater {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FeedProviderConfig feedProviderConfig;
    private final GbfsDeliveryMapper gbfsDeliveryMapper;
    private final FeedCachesUpdater feedCachesUpdater;
    private final EntityCachesUpdater entityCachesUpdater;
    private final RBucket<Boolean> cacheReady;

    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    private GbfsSubscriptionManager subscriptionManager;
    private ForkJoinPool updaterThreadPool;

    private final RMapCache<String, ValidationResult> validationResultCache;

    @Value("${org.entur.lamassu.enableValidation:false}")
    private boolean enableValidation;

    private MetricsService metricsService;

    @Autowired
    public FeedUpdater(
            FeedProviderConfig feedProviderConfig,
            GbfsDeliveryMapper gbfsDeliveryMapper,
            FeedCachesUpdater feedCachesUpdater,
            EntityCachesUpdater entityCachesUpdater,
            RMapCache<String, ValidationResult> validationResultCache,
            RBucket<Boolean> cacheReady,
            MetricsService metricsService
    ) {
        this.feedProviderConfig = feedProviderConfig;
        this.gbfsDeliveryMapper = gbfsDeliveryMapper;
        this.feedCachesUpdater = feedCachesUpdater;
        this.entityCachesUpdater = entityCachesUpdater;
        this.validationResultCache = validationResultCache;
        this.cacheReady = cacheReady;
        this.metricsService = metricsService;
    }

    public void start() {
        updaterThreadPool = new ForkJoinPool(
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
            options.setRequestAuthenticator(feedProvider.getAuthentication().getRequestAuthenticator());
        }
        options.setEnableValidation(enableValidation);
        String id = subscriptionManager.subscribe(options, delivery -> receiveUpdate(feedProvider, delivery));

        if (id == null) {
            logger.warn("Failed to setup subscription, trying again in 5 seconds - systemId={}", feedProvider.getSystemId());
            metricsService.registerSubscriptionSetup(feedProvider, false);
            CompletableFuture.delayedExecutor(60, TimeUnit.SECONDS).execute(() -> updaterThreadPool.execute(() -> createSubscription(feedProvider)));
        } else {
            logger.info("Setup subscription complete systemId={}", feedProvider.getSystemId());
            metricsService.registerSubscriptionSetup(feedProvider, true);
        }
    }

    private void receiveUpdate(FeedProvider feedProvider, GbfsDelivery delivery) {
        if (enableValidation) {
            if (delivery.getValidationResult().getSummary().getErrorsCount() > 0) {
                logger.info("Validation errors in feed update for system {}", feedProvider.getSystemId());
            }
            validationResultCache.put(feedProvider.getSystemId(), delivery.getValidationResult());
            metricsService.registerValidationResult(feedProvider, delivery.getValidationResult());
        }

        var mappedDelivery = gbfsDeliveryMapper.mapGbfsDelivery(delivery, feedProvider);
        var oldDelivery =  feedCachesUpdater.updateFeedCaches(feedProvider, mappedDelivery);
        entityCachesUpdater.updateEntityCaches(feedProvider, mappedDelivery, oldDelivery);
        cacheReady.set(true);
    }
}
