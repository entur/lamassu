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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.entur.lamassu.cache.SpatialIndex;
import org.entur.lamassu.cache.SpatialIndexId;
import org.entur.lamassu.model.entities.LocationEntity;
import org.redisson.api.RGeo;
import org.redisson.api.geo.GeoEntry;
import org.redisson.api.geo.GeoOrder;
import org.redisson.api.geo.GeoSearchArgs;
import org.redisson.api.geo.GeoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SpatialIndexImpl<S extends SpatialIndexId, T extends LocationEntity>
  implements SpatialIndex<S, T> {

  private final RGeo<S> spatialIndex;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected SpatialIndexImpl(RGeo<S> spatialIndex) {
    this.spatialIndex = spatialIndex;
  }

  @Override
  public void addAll(Map<S, T> spatialIndexUpdateMap) {
    var positionable = new ArrayList<Map.Entry<S, T>>();
    var missingCoordinates = new ArrayList<String>();

    for (var entry : spatialIndexUpdateMap.entrySet()) {
      var entity = entry.getValue();
      if (entity.getLat() == null || entity.getLon() == null) {
        missingCoordinates.add(entity.getId());
      } else {
        positionable.add(entry);
      }
    }

    if (!missingCoordinates.isEmpty()) {
      // The caller has usually just removed the previous entry for these ids, so dropping
      // them silently loses them from the index until the entity next changes.
      logger.warn(
        "Not adding {} entries to spatialIndex because they have no coordinates: {}",
        missingCoordinates.size(),
        missingCoordinates
      );
    }

    if (positionable.isEmpty()) {
      return;
    }

    try {
      Long added = spatialIndex
        .addAsync(positionable.stream().map(this::map).toArray(GeoEntry[]::new))
        .get();
      logger.debug("Added {} stations", added);
    } catch (RedisException | ExecutionException e) {
      logger.warn("Caught exception while adding entries to spatialIndex", e);
    } catch (InterruptedException e) {
      logger.warn("Interrupted while adding entries to spatialIndex", e);
      Thread.currentThread().interrupt();
    }
  }

  private GeoEntry map(Map.Entry<S, T> entry) {
    var key = entry.getKey();
    var entity = entry.getValue();
    return new GeoEntry(entity.getLon(), entity.getLat(), key);
  }

  /**
   * Removes entries from the spatial index, waiting for the delete to complete.
   *
   * <p>Awaiting is required for correctness: Redisson draws connections from a pool and
   * gives no ordering guarantee between separately issued async commands. A fire-and-forget
   * ZREM can land after the GEOADD issued by a subsequent {@link #addAll}, deleting the
   * entry that was just written. For an ordinary status change the removed and added
   * members are equal, so the entity then disappears from the index while remaining in the
   * entity cache.
   */
  @Override
  public void removeAll(Set<S> ids) {
    try {
      spatialIndex.removeAllAsync(ids).get();
      logger.debug("Removed {} entries from spatialIndex", ids.size());
    } catch (RedisException | ExecutionException e) {
      logger.warn("Caught exception while removing entries from spatialIndex", e);
    } catch (InterruptedException e) {
      logger.warn("Interrupted while removing entries from spatialIndex", e);
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public List<S> radius(
    Double longitude,
    Double latitude,
    Double radius,
    GeoUnit geoUnit,
    GeoOrder geoOrder
  ) {
    return spatialIndex.search(
      GeoSearchArgs.from(longitude, latitude).radius(radius, geoUnit).order(geoOrder)
    );
  }

  @Override
  public Collection<S> getAll() {
    return spatialIndex.readAll();
  }

  public int count() {
    return spatialIndex.size();
  }
}
