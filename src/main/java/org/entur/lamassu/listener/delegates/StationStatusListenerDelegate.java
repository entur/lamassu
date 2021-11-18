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

import org.entur.gbfs.v2_2.gbfs.GBFSFeedName;
import org.entur.gbfs.v2_2.station_information.GBFSData;
import org.entur.gbfs.v2_2.station_information.GBFSStationInformation;
import org.entur.gbfs.v2_2.station_status.GBFSStation;
import org.entur.gbfs.v2_2.station_status.GBFSStationStatus;
import org.entur.gbfs.v2_2.system_information.GBFSSystemInformation;
import org.entur.gbfs.v2_2.system_pricing_plans.GBFSSystemPricingPlans;
import org.entur.gbfs.v2_2.system_regions.GBFSSystemRegions;
import org.entur.gbfs.v2_2.vehicle_types.GBFSVehicleTypes;
import org.entur.lamassu.cache.GBFSFeedCacheV2;
import org.entur.lamassu.cache.StationCache;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.mapper.entitymapper.PricingPlanMapper;
import org.entur.lamassu.mapper.entitymapper.StationMapper;
import org.entur.lamassu.mapper.entitymapper.SystemMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.util.SpatialIndexIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class StationStatusListenerDelegate implements CacheEntryListenerDelegate<Object, GBFSStationStatus> {
    private final GBFSFeedCacheV2 feedCache;
    private final StationCache stationCache;
    private final FeedProviderService feedProviderService;
    private final StationSpatialIndex spatialIndex;
    private final SystemMapper systemMapper;
    private final PricingPlanMapper pricingPlanMapper;
    private final StationMapper stationMapper;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public StationStatusListenerDelegate(
            GBFSFeedCacheV2 feedCache,
            StationCache stationCache,
            FeedProviderService feedProviderService,
            StationSpatialIndex spatialIndex,
            SystemMapper systemMapper,
            PricingPlanMapper pricingPlanMapper,
            StationMapper stationMapper
    ) {
        this.feedCache = feedCache;
        this.stationCache = stationCache;
        this.feedProviderService =  feedProviderService;
        this.spatialIndex = spatialIndex;
        this.systemMapper = systemMapper;
        this.pricingPlanMapper = pricingPlanMapper;
        this.stationMapper = stationMapper;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ?>> iterable) {
        for (var event : iterable)  {
            addOrUpdateStation(event);
        }
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ?>> iterable) {
        for (var event : iterable)  {
            addOrUpdateStation(event);
        }
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends String, ?>> iterable) {
        // noop
    }

    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends String, ?>> iterable) {
        // noop
    }

    private void addOrUpdateStation(CacheEntryEvent<? extends String, ?> event) {
        var split = event.getKey().split("_");
        var feedProvider = feedProviderService.getFeedProviderBySystemId(split[split.length - 1]);

        var systemInformationFeed = (GBFSSystemInformation) feedCache.find(GBFSFeedName.SystemInformation, feedProvider);
        var pricingPlansFeed = (GBFSSystemPricingPlans) feedCache.find(GBFSFeedName.SystemPricingPlans, feedProvider);
        var vehicleTypesFeed = (GBFSVehicleTypes) feedCache.find(GBFSFeedName.VehicleTypes, feedProvider);
        var systemRegionsFeed = (GBFSSystemRegions) feedCache.find(GBFSFeedName.SystemRegions, feedProvider);

        var stationInformationFeed = (GBFSStationInformation) feedCache.find(GBFSFeedName.StationInformation, feedProvider);
        var stationStatusFeed = (GBFSStationStatus) event.getValue();

        if (stationInformationFeed.getData() == null) {
            logger.warn("stationInformationFeed has no data! provider={} feed={}", feedProvider, stationInformationFeed);
            return;
        }

        if (stationStatusFeed.getData() == null) {
            logger.warn("stationStatusFeed has no data! provider={} feed={}", feedProvider, stationStatusFeed);
            return;
        }

        if (vehicleTypesFeed.getData() == null) {
            logger.warn("vehicleTypesFeed has no data! provider={} feed={}", feedProvider, vehicleTypesFeed);
            return;
        }

        var stationIds = stationStatusFeed.getData().getStations().stream()
                .map(GBFSStation::getStationId)
                .collect(Collectors.toSet());

        Set<String> stationIdsToRemove = null;

        if (event.isOldValueAvailable()) {
            var oldStationStatusFeed = (GBFSStationStatus) event.getOldValue();
            if (oldStationStatusFeed.getData() != null) {
                stationIdsToRemove = oldStationStatusFeed.getData().getStations().stream()
                        .map(GBFSStation::getStationId).collect(Collectors.toSet());
                stationIdsToRemove.removeAll(stationIds);
                logger.debug("Found {} stationIds to remove from old station_status feed", stationIdsToRemove.size());

                // Add station ids that are staged for removal to the set of stations ids that will be used to
                // fetch current stations from cache
                stationIds.addAll(stationIdsToRemove);
            }
        }

        if (stationIdsToRemove == null) {
            stationIdsToRemove = new HashSet<>(stationIds);
            logger.info("Old station_status feed was not available or had no data. As a workaround, removing all stations for provider {}", feedProvider.getSystemId());
        }

        var originalStations = stationCache.getAllAsMap(stationIds);

        var system = getSystem(feedProvider, systemInformationFeed);

        if (system == null) {
            logger.warn("no system information provider={} feed={}", feedProvider, systemInformationFeed);
            return;
        }

        var pricingPlans = getPricingPlans(feedProvider, pricingPlansFeed);

        if (pricingPlans.isEmpty()) {
            logger.warn("no pricing plans provider={} feed={}", feedProvider, pricingPlansFeed);
            return;
        }

        var stationInfo = Optional.ofNullable(stationInformationFeed)
                .map(GBFSStationInformation::getData)
                .map(GBFSData::getStations)
                .orElse(List.of())
                .stream().collect(Collectors.toMap(org.entur.gbfs.v2_2.station_information.GBFSStation::getStationId, s -> s));

        var stations = stationStatusFeed.getData().getStations().stream()
                .filter(s -> {
                    if (stationInfo.get(s.getStationId()) == null) {
                        logger.warn("Skipping station due to missing station information feed for provider={} stationId={}", feedProvider, s.getStationId());
                        return false;
                    }
                    return true;
                })
                // Filter out virtual stations until we have a use case for this, and graphql API supports filtering on it
                .filter(s -> !Optional.ofNullable(stationInfo.get(s.getStationId()).getIsVirtualStation()).orElse(false))
                .map(station -> stationMapper.mapStation(
                        system,
                        pricingPlans,
                        stationInfo.get(station.getStationId()),
                        station,
                        vehicleTypesFeed,
                        systemRegionsFeed,
                        feedProvider.getLanguage())
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

    private List<PricingPlan> getPricingPlans(FeedProvider feedProvider, GBFSSystemPricingPlans pricingPlansFeed) {
        if (pricingPlansFeed == null) {
            logger.warn("Missing pricing plans feed for provider {}", feedProvider);
            return List.of();
        }

        if (pricingPlansFeed.getData() == null) {
            logger.warn("Missing pricing plans data for provider={} feed={}", feedProvider, pricingPlansFeed);
            return List.of();
        }

        return pricingPlansFeed.getData().getPlans().stream()
                .map(pricingPlan -> pricingPlanMapper.mapPricingPlan(pricingPlan, feedProvider.getLanguage()))
                .collect(Collectors.toList());
    }

    private org.entur.lamassu.model.entities.System getSystem(FeedProvider feedProvider, GBFSSystemInformation systemInformationFeed) {
        if (systemInformationFeed == null) {
            logger.warn("Missing system information feed for provider {}", feedProvider);
            return null;
        }

        if (systemInformationFeed.getData() == null) {
            logger.warn("Missing system information data for provider={} feed={}", feedProvider, systemInformationFeed);
            return null;
        }

        return systemMapper.mapSystem(systemInformationFeed.getData(), feedProvider);
    }
}
