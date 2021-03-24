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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class StationStatusListenerDelegate implements CacheEntryListenerDelegate<GBFSBase, StationStatus> {
    private final GBFSFeedCache feedCache;
    private final StationCache stationCache;
    private final FeedProviderConfig feedProviderConfig;
    private final SystemMapper systemMapper;
    private final PricingPlanMapper pricingPlanMapper;
    private final StationMapper stationMapper;

    @Autowired
    public StationStatusListenerDelegate(GBFSFeedCache feedCache, StationCache stationCache, FeedProviderConfig feedProviderConfig, SystemMapper systemMapper, PricingPlanMapper pricingPlanMapper, StationMapper stationMapper) {
        this.feedCache = feedCache;
        this.stationCache = stationCache;
        this.feedProviderConfig =  feedProviderConfig;
        this.systemMapper = systemMapper;
        this.pricingPlanMapper = pricingPlanMapper;
        this.stationMapper = stationMapper;
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

        stationCache.updateAll(stations);
    }
}
