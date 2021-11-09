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

import org.entur.gbfs.v2_2.geofencing_zones.GBFSGeofencingZones;
import org.entur.lamassu.cache.GeofencingZonesCache;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.mapper.entitymapper.GeofencingZonesMapper;
import org.entur.lamassu.service.FeedProviderService;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;
import java.util.Map;

@Component
public class GeofencingZonesListenerDelegate implements CacheEntryListenerDelegate<Object, GBFSGeofencingZones> {

    private final GeofencingZonesCache geofencingZonesCache;
    private final FeedProviderService feedProviderService;
    private final GeofencingZonesMapper geofencingZonesMapper;

    public GeofencingZonesListenerDelegate(GeofencingZonesCache geofencingZonesCache, FeedProviderService feedProviderService, GeofencingZonesMapper geofencingZonesMapper) {
        this.geofencingZonesCache = geofencingZonesCache;
        this.feedProviderService = feedProviderService;
        this.geofencingZonesMapper = geofencingZonesMapper;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ?>> iterable) {
        for (var event : iterable)  {
            addOrUpdateGeofencingZones(event);
        }
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ?>> iterable) {
        for (var event : iterable)  {
            addOrUpdateGeofencingZones(event);
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

    private void addOrUpdateGeofencingZones(CacheEntryEvent<? extends String, ?> event) {
        var split = event.getKey().split("_");
        var feedProvider = feedProviderService.getFeedProviderBySystemId(split[split.length - 1]);
        var feed = (GBFSGeofencingZones) event.getValue();
        var mapped = geofencingZonesMapper.map(feed.getData().getGeofencingZones(), feedProvider);
        geofencingZonesCache.updateAll(Map.of(mapped.getId(), mapped));
    }
}
