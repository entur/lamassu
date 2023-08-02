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

package org.entur.lamassu.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.entur.lamassu.model.entities.LocationEntity;
import org.redisson.api.GeoOrder;
import org.redisson.api.GeoUnit;

public interface SpatialIndex<S extends SpatialIndexId, T extends LocationEntity> {
  void addAll(Map<S, T> spatialIndexUpdateMap);
  void removeAll(Set<S> ids);
  List<S> radius(
    Double longitude,
    Double latitude,
    Double radius,
    GeoUnit geoUnit,
    GeoOrder geoOrder
  );
  Collection<S> getAll();
}
