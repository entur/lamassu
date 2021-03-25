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

package org.entur.lamassu.listener.delegates;

import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.cache.StationCache;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.mapper.PricingPlanMapper;
import org.entur.lamassu.mapper.StationMapper;
import org.entur.lamassu.mapper.SystemMapper;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.entur.lamassu.model.gbfs.v2_1.StationInformation;
import org.entur.lamassu.model.gbfs.v2_1.StationStatus;
import org.entur.lamassu.model.gbfs.v2_1.SystemInformation;
import org.entur.lamassu.model.gbfs.v2_1.SystemPricingPlans;
import org.entur.lamassu.util.SpatialIndexIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class StationStatusListenerDelegate implements CacheEntryListenerDelegate<GBFSBase, StationStatus> {
    private final GBFSFeedCache feedCache;
    private final StationCache stationCache;
    private final FeedProviderConfig feedProviderConfig;
    private final SystemMapper systemMapper;
    private final PricingPlanMapper pricingPlanMapper;
    private final StationMapper stationMapper;
    private final StationSpatialIndex spatialIndex;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public StationStatusListenerDelegate(GBFSFeedCache feedCache, StationCache stationCache, FeedProviderConfig feedProviderConfig, SystemMapper systemMapper, PricingPlanMapper pricingPlanMapper, StationMapper stationMapper, StationSpatialIndex spatialIndex) {
        this.feedCache = feedCache;
        this.stationCache = stationCache;
        this.feedProviderConfig =  feedProviderConfig;
        this.systemMapper = systemMapper;
        this.pricingPlanMapper = pricingPlanMapper;
        this.stationMapper = stationMapper;
        this.spatialIndex = spatialIndex;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) {
        for (var event : iterable)  {
            addOrUpdateStation(event);
        }
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) {
        for (var event : iterable)  {
            addOrUpdateStation(event);
        }
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) {
        // noop
    }

    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) {
        // noop
    }

    private void addOrUpdateStation(CacheEntryEvent<? extends String, ? extends GBFSBase> event) {
        var split = event.getKey().split("_");
        var feedProvider = feedProviderConfig.get(split[split.length - 1]);

        var systemInformationFeed = (SystemInformation) feedCache.find(GBFSFeedName.SYSTEM_INFORMATION, feedProvider);
        var pricingPlansFeed = (SystemPricingPlans) feedCache.find(GBFSFeedName.SYSTEM_PRICING_PLANS, feedProvider);

        var stationInformationFeed = (StationInformation) feedCache.find(GBFSFeedName.STATION_INFORMATION, feedProvider);
        var stationStatusFeed = (StationStatus) event.getValue();

        var stationIds = stationStatusFeed.getData().getStations().stream()
                .map(StationStatus.Station::getStationId)
                .collect(Collectors.toSet());

        Set<String> stationIdsToRemove;

        // Note: This conditional will never be true due to a suspected bug in redisson:
        // https://github.com/redisson/redisson/issues/3511
        if (event.isOldValueAvailable()) {
            var oldStationStatusFeed = (StationStatus) event.getOldValue();
            stationIdsToRemove = oldStationStatusFeed.getData().getStations().stream()
                    .map(StationStatus.Station::getStationId).collect(Collectors.toSet());
            stationIdsToRemove.removeAll(stationIds);
            logger.debug("Found {} stationIds to remove from old station_status feed", stationIdsToRemove.size());

            // Add station ids that are staged for removal to the set of stations ids that will be used to
            // fetch current stations from cache
            stationIds.addAll(stationIdsToRemove);
        } else {

            // In order to avoid stale stations hanging around, as a workaround, remove all stations for this provider.
            stationIdsToRemove = new HashSet<>(stationIds);
            logger.debug("Old station_status feed was not available. As a workaround, removing all stations for this provider.");
        }

        var originalStations = stationCache.getAllAsMap(stationIds);

        var system = systemMapper.mapSystem(systemInformationFeed.getData());
        var pricingPlans = pricingPlansFeed.getData().getPlans().stream()
                .map(pricingPlanMapper::mapPricingPlan)
                .collect(Collectors.toList());

        var stations = stationStatusFeed.getData().getStations().stream()
                .map(station -> stationMapper.mapStation(
                        system,
                        pricingPlans,
                        Objects.requireNonNull(stationInformationFeed.getData().getStations().stream()
                                .filter(s -> {
                                    var match = s.getStationId().equals(station.getStationId());
                                    return match;
                                }).findFirst().orElse(null)),
                        station)
                ).collect(Collectors.toMap(Station::getId, s->s));

        Set<String> spatialIndicesToRemove = new java.util.HashSet<>(Set.of());
        Map<String, Station> spatialIndexUpdateMap = new java.util.HashMap<>(Map.of());

        stations.forEach((key, station) -> {
            var spatialIndexId = SpatialIndexIdUtil.createStationSpatialIndexId(station, feedProvider);
            var previousStation = originalStations.get(key);

            if (previousStation != null) {
                var oldSpatialIndexId = SpatialIndexIdUtil.createStationSpatialIndexId(previousStation, feedProvider);
                if (!oldSpatialIndexId.equalsIgnoreCase(spatialIndexId)) {
                    spatialIndicesToRemove.add(oldSpatialIndexId);
                }
            }
            spatialIndexUpdateMap.put(spatialIndexId, station);
        });

        if (!spatialIndicesToRemove.isEmpty()) {
            logger.debug("Removing {} stale entries in spatial index", spatialIndicesToRemove.size());
            spatialIndex.removeAll(spatialIndicesToRemove);
        }

        if (!stationIdsToRemove.isEmpty()) {
            logger.debug("Removing {} stations from station cache", stationIdsToRemove.size());
            stationCache.removeAll(stationIdsToRemove);
        }

        if (!stations.isEmpty()) {
            logger.debug("Adding/updating {} stations in station cache", stations.size());
            stationCache.updateAll(stations);
        }

        if (!spatialIndexUpdateMap.isEmpty()) {
            logger.debug("Updating {} entries in spatial index", spatialIndexUpdateMap.size());
            spatialIndex.addAll(spatialIndexUpdateMap);
        }
    }
}
