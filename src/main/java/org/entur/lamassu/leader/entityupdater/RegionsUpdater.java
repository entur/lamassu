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

package org.entur.lamassu.leader.entityupdater;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.mapper.entitymapper.RegionMapper;
import org.entur.lamassu.model.entities.Region;
import org.entur.lamassu.util.CacheUtil;
import org.mobilitydata.gbfs.v3_0.system_regions.GBFSSystemRegions;
import org.springframework.stereotype.Component;

@Component
public class RegionsUpdater {

  private final EntityCache<Region> regionCache;
  private final RegionMapper regionMapper;

  public RegionsUpdater(EntityCache<Region> regionCache, RegionMapper regionMapper) {
    this.regionCache = regionCache;
    this.regionMapper = regionMapper;
  }

  public void update(GBFSSystemRegions systemRegions, String language) {
    var mapped = systemRegions
      .getData()
      .getRegions()
      .stream()
      .map(region -> regionMapper.mapRegion(region, language))
      .collect(Collectors.toMap(Region::getId, region -> region));

    var lastUpdated = systemRegions.getLastUpdated();
    var ttl = systemRegions.getTtl();

    regionCache.updateAll(
      mapped,
      CacheUtil.getTtl(
        (int) Instant.now().getEpochSecond(),
        (int) (lastUpdated.getTime() / 100),
        ttl,
        86400
      ),
      TimeUnit.SECONDS
    );
  }
}
