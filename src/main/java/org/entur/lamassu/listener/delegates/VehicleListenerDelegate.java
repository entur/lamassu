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

import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.util.SpatialIndexIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;
import java.util.HashSet;
import java.util.Set;

@Component
public class VehicleListenerDelegate implements CacheEntryListenerDelegate<Vehicle, Vehicle> {
    private final FeedProviderService feedProviderService;
    private final VehicleSpatialIndex spatialIndex;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public VehicleListenerDelegate(
            FeedProviderService feedProviderService,
            VehicleSpatialIndex spatialIndex
    ) {
        this.feedProviderService = feedProviderService;
        this.spatialIndex = spatialIndex;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends Vehicle>> iterable) {
        // noop
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends Vehicle>> iterable) {
        // noop
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends Vehicle>> iterable) {
        // noop
    }

    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends Vehicle>> iterable) {
        var ids = new HashSet<String>(Set.of());

        for (CacheEntryEvent<? extends String, ? extends Vehicle> entry : iterable) {
            var split = entry.getKey().split("_");
            var feedProvider = feedProviderService.getFeedProviderBySystemSlug(split[split.length - 1]);
            var vehicle = entry.getValue();
            var id = SpatialIndexIdUtil.createVehicleSpatialIndexId(vehicle, feedProvider);
            ids.add(id);
        }

        spatialIndex.removeAll(ids);
        logger.debug("Removed {} entries from spatial index", ids.size());
    }
}
