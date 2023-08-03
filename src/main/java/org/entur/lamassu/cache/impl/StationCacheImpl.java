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

import org.entur.lamassu.cache.StationCache;
import org.entur.lamassu.model.entities.Station;
import org.redisson.api.RMapCache;
import org.springframework.stereotype.Component;

@Component
public class StationCacheImpl extends EntityCacheImpl<Station> implements StationCache {

  protected StationCacheImpl(RMapCache<String, Station> cache) {
    super(cache);
  }
}
