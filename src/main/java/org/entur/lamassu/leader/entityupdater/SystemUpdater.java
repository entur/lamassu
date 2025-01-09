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
import org.entur.lamassu.mapper.entitymapper.SystemMapper;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.CacheUtil;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSSystemInformation;
import org.springframework.stereotype.Component;

@Component
public class SystemUpdater {

  private final EntityCache<System> systemCache;
  private final SystemMapper systemMapper;

  public SystemUpdater(EntityCache<System> systemCache, SystemMapper systemMapper) {
    this.systemCache = systemCache;
    this.systemMapper = systemMapper;
  }

  public void update(GBFSSystemInformation systemInformation, FeedProvider feedProvider) {
    var mapped = systemMapper.mapSystem(systemInformation.getData(), feedProvider);

    var lastUpdated = systemInformation.getLastUpdated();
    var ttl = systemInformation.getTtl();

    systemCache.updateAll(
      Map.of(mapped.getId(), mapped),
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
