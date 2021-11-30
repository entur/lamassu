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

package org.entur.lamassu.updater;

import org.entur.gbfs.GbfsDelivery;
import org.entur.gbfs.GbfsSubscriptionManager;
import org.entur.gbfs.GbfsSubscriptionOptions;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.mapper.feedmapper.GbfsDeliveryMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.updater.entityupdater.EntityCachesUpdater;
import org.entur.lamassu.updater.feedcachesupdater.FeedCachesUpdater;
import org.redisson.api.RBucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.ForkJoinPool;

@Component
@Profile("leader")
public class FeedUpdater {
    private final FeedProviderConfig feedProviderConfig;
    private final GbfsDeliveryMapper gbfsDeliveryMapper;
    private final FeedCachesUpdater feedCachesUpdater;
    private final EntityCachesUpdater entityCachesUpdater;
    private final RBucket<Boolean> cacheReady;

    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    private GbfsSubscriptionManager subscriptionManager;
    private ForkJoinPool updaterThreadPool;

    @Autowired
    public FeedUpdater(
            FeedProviderConfig feedProviderConfig,
            GbfsDeliveryMapper gbfsDeliveryMapper,
            FeedCachesUpdater feedCachesUpdater,
            EntityCachesUpdater entityCachesUpdater,
            RBucket<Boolean> cacheReady
    ) {
        this.feedProviderConfig = feedProviderConfig;
        this.gbfsDeliveryMapper = gbfsDeliveryMapper;
        this.feedCachesUpdater = feedCachesUpdater;
        this.entityCachesUpdater = entityCachesUpdater;
        this.cacheReady = cacheReady;
    }

    public void start() {
        updaterThreadPool = new ForkJoinPool(NUM_CORES * 2);
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
        subscriptionManager.subscribe(options, delivery -> receiveUpdate(feedProvider, delivery));
    }

    private void receiveUpdate(FeedProvider feedProvider, GbfsDelivery delivery) {
        var mappedDelivery = gbfsDeliveryMapper.mapGbfsDelivery(delivery, feedProvider);
        var oldDelivery =  feedCachesUpdater.updateFeedCaches(feedProvider, mappedDelivery);
        entityCachesUpdater.updateEntityCaches(feedProvider, mappedDelivery, oldDelivery);
        cacheReady.set(true);
    }
}
