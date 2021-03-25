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

package org.entur.lamassu.cache.impl;

import io.lettuce.core.RedisException;
import org.entur.lamassu.cache.SpatialIndex;
import org.entur.lamassu.model.entities.LocationEntity;
import org.redisson.api.GeoEntry;
import org.redisson.api.GeoOrder;
import org.redisson.api.GeoUnit;
import org.redisson.api.RGeo;
import org.redisson.api.geo.GeoSearchArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class SpatialIndexImpl<T extends LocationEntity> implements SpatialIndex<T> {
    private final RGeo<String> spatialIndex;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected SpatialIndexImpl(RGeo<String> spatialIndex) {
        this.spatialIndex = spatialIndex;
    }

    @Override
    public void addAll(Map<String, T> spatialIndexUpdateMap) {
        try {
            spatialIndex.addAsync(spatialIndexUpdateMap.entrySet().stream()
                    .map(this::map).toArray(GeoEntry[]::new));
        } catch (RedisException e) {
            logger.warn("Caught exception while adding entries to spatialIndex", e);
        }
    }

    private GeoEntry map(Map.Entry<String, T> entry) {
        var key = entry.getKey();
        var entity = entry.getValue();
        return new GeoEntry(entity.getLon(), entity.getLat(), key);
    }

    @Override
    public void remove(String id) {
        spatialIndex.removeAsync(id);
    }

    @Override
    public void removeAll(Set<String> ids) {
        spatialIndex.removeAllAsync(ids);
    }

    @Override
    public List<String> radius(Double longitude, Double latitude, Double radius, GeoUnit geoUnit, GeoOrder geoOrder) {
        return spatialIndex.search(GeoSearchArgs.from(longitude, latitude).radius(radius, geoUnit).order(geoOrder));
    }
}
