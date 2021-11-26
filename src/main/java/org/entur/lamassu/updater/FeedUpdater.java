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
import org.entur.gbfs.v2_2.free_bike_status.GBFSFreeBikeStatus;
import org.entur.gbfs.v2_2.gbfs.GBFS;
import org.entur.gbfs.v2_2.gbfs.GBFSFeedName;
import org.entur.gbfs.v2_2.geofencing_zones.GBFSGeofencingZones;
import org.entur.gbfs.v2_2.station_information.GBFSStationInformation;
import org.entur.gbfs.v2_2.station_status.GBFSStationStatus;
import org.entur.gbfs.v2_2.system_alerts.GBFSSystemAlerts;
import org.entur.gbfs.v2_2.system_calendar.GBFSSystemCalendar;
import org.entur.gbfs.v2_2.system_hours.GBFSSystemHours;
import org.entur.gbfs.v2_2.system_information.GBFSSystemInformation;
import org.entur.gbfs.v2_2.system_pricing_plans.GBFSSystemPricingPlans;
import org.entur.gbfs.v2_2.system_regions.GBFSSystemRegions;
import org.entur.gbfs.v2_2.vehicle_types.GBFSVehicleTypes;
import org.entur.lamassu.cache.GBFSFeedCacheV2;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.mapper.GbfsDeliveryMapper;
import org.entur.lamassu.mapper.feedmapper.VehicleTypeCapacityProducer;
import org.entur.lamassu.mapper.feedmapper.FeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.updater.entityupdater.EntityCachesUpdater;
import org.entur.lamassu.updater.entityupdater.GeofencingZonesUpdater;
import org.entur.lamassu.updater.entityupdater.StationsUpdater;
import org.entur.lamassu.updater.entityupdater.VehiclesUpdater;
import org.entur.lamassu.updater.feedcachesupdater.FeedCachesUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.ForkJoinPool;

@Component
public class FeedUpdater {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FeedProviderConfig feedProviderConfig;
    private final GbfsDeliveryMapper gbfsDeliveryMapper;
    private final FeedCachesUpdater feedCachesUpdater;
    private final EntityCachesUpdater entityCachesUpdater;

    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    private GbfsSubscriptionManager subscriptionManager;
    private ForkJoinPool updaterThreadPool;

    @Autowired
    public FeedUpdater(
            FeedProviderConfig feedProviderConfig,
            GbfsDeliveryMapper gbfsDeliveryMapper,
            FeedCachesUpdater feedCachesUpdater,
            EntityCachesUpdater entityCachesUpdater
    ) {
        this.feedProviderConfig = feedProviderConfig;
        this.gbfsDeliveryMapper = gbfsDeliveryMapper;
        this.feedCachesUpdater = feedCachesUpdater;
        this.entityCachesUpdater = entityCachesUpdater;
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
    }
}
