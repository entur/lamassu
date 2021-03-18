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
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.util.SpatialIndexIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;

@Component
public class VehicleListenerDelegate implements CacheEntryListenerDelegate<Vehicle, Vehicle> {
    private final FeedProviderConfig feedProviderConfig;
    private final VehicleSpatialIndex spatialIndex;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public VehicleListenerDelegate(
            FeedProviderConfig feedProviderConfig,
            VehicleSpatialIndex spatialIndex
    ) {
        this.feedProviderConfig = feedProviderConfig;
        this.spatialIndex = spatialIndex;
    }

    @Override
    public void onCreated(CacheEntryEvent<? extends String, Vehicle> event) {
        // noop
    }

    @Override
    public void onUpdated(CacheEntryEvent<? extends String, Vehicle> event) {
        // noop
    }

    @Override
    public void onRemoved(CacheEntryEvent<? extends String, Vehicle> event) {
        // noop
    }

    @Override
    public void onExpired(CacheEntryEvent<? extends String, Vehicle> event) {
        var split = event.getKey().split("_");
        var feedProvider = feedProviderConfig.get(split[split.length - 1]);
        var vehicle = event.getValue();
        var id = SpatialIndexIdUtil.createSpatialIndexId(vehicle, feedProvider);
        spatialIndex.remove(id);
        logger.debug("Removed entry from spatial index with key {}", id);
    }
}
