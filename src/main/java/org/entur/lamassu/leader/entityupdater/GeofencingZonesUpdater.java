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
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.mapper.entitymapper.GeofencingZonesDataMapper;
import org.entur.lamassu.mapper.entitymapper.GeofencingZonesMapper;
import org.entur.lamassu.model.entities.GeofencingZones;
import org.entur.lamassu.model.entities.GeofencingZonesData;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.CacheUtil;
import org.mobilitydata.gbfs.v3_0.geofencing_zones.GBFSGeofencingZones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GeofencingZonesUpdater {

  private final EntityCache<GeofencingZones> geofencingZonesCache;
  private final EntityCache<GeofencingZonesData> geofencingZonesDataCache;
  private final GeofencingZonesMapper geofencingZonesMapper;
  private final GeofencingZonesDataMapper geofencingZonesDataMapper;

  @Autowired
  public GeofencingZonesUpdater(
    EntityCache<GeofencingZones> geofencingZonesCache,
    EntityCache<GeofencingZonesData> geofencingZonesDataCache,
    GeofencingZonesMapper geofencingZonesMapper,
    GeofencingZonesDataMapper geofencingZonesDataMapper
  ) {
    this.geofencingZonesCache = geofencingZonesCache;
    this.geofencingZonesDataCache = geofencingZonesDataCache;
    this.geofencingZonesMapper = geofencingZonesMapper;
    this.geofencingZonesDataMapper = geofencingZonesDataMapper;
  }

  public void update(FeedProvider feedProvider, GBFSGeofencingZones feed) {
    // Update legacy cache for backwards compatibility
    var mapped = geofencingZonesMapper.map(
      feed.getData().getGeofencingZones(),
      feedProvider
    );

    // Update new optimized cache with global rules support
    var mappedData = geofencingZonesDataMapper.map(feed, feedProvider);

    var lastUpdated = feed.getLastUpdated();
    var ttl = feed.getTtl();

    var ttlSeconds = CacheUtil.getTtl(
      (int) Instant.now().getEpochSecond(),
      (int) (lastUpdated.getTime() / 100),
      ttl,
      3600
    );

    // Update legacy cache
    geofencingZonesCache.updateAll(
      Map.of(mapped.getId(), mapped),
      ttlSeconds,
      TimeUnit.SECONDS
    );

    // Update new optimized cache
    if (mappedData != null) {
      geofencingZonesDataCache.updateAll(
        Map.of(mappedData.getId(), mappedData),
        ttlSeconds,
        TimeUnit.SECONDS
      );
    }
  }
}
