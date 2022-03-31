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

package org.entur.lamassu.leader.listener.delegates;

import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.leader.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.service.FeedProviderService;
import org.redisson.api.RMapCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class VehicleListenerDelegate implements CacheEntryListenerDelegate<Vehicle> {
    private final RMapCache<String, Vehicle> cache;
    private final FeedProviderService feedProviderService;
    private final VehicleSpatialIndex spatialIndex;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public VehicleListenerDelegate(
            RMapCache<String, Vehicle> cache,
            FeedProviderService feedProviderService,
            VehicleSpatialIndex spatialIndex
    ) {
        this.cache = cache;
        this.feedProviderService = feedProviderService;
        this.spatialIndex = spatialIndex;
    }

    @Override
    public void onExpired(String name) {
        var split = name.split("_");
        var feedProvider = feedProviderService.getFeedProviderBySystemId(split[split.length - 1]);
        if (feedProvider == null) {
            logger.warn("Feed provider not found on expired vehicle={}. Probably means feed provider was removed.", name);
        } else {
            var ids = spatialIndex.getAll()
                    .stream()
                    .map(VehicleSpatialIndexId::fromString)
                    .filter(Objects::nonNull)
                    .filter(id -> id.getId().equals(split[0]))
                    .map(VehicleSpatialIndexId::toString)
                    .collect(Collectors.toSet());
            spatialIndex.removeAll(ids);
        }
    }
}
