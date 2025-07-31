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
import org.entur.lamassu.mapper.entitymapper.VehicleTypeMapper;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.CacheUtil;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VehicleTypesUpdater {

  private static final Logger log = LoggerFactory.getLogger(VehicleTypesUpdater.class);

  private final EntityCache<VehicleType> vehicleTypeCache;
  private final VehicleTypeMapper vehicleTypeMapper;

  public VehicleTypesUpdater(
    EntityCache<VehicleType> vehicleTypeCache,
    VehicleTypeMapper vehicleTypeMapper
  ) {
    this.vehicleTypeCache = vehicleTypeCache;
    this.vehicleTypeMapper = vehicleTypeMapper;
  }

  public void update(GBFSVehicleTypes gbfsVehicleTypes, FeedProvider feedProvider) {
    var mapped = gbfsVehicleTypes
      .getData()
      .getVehicleTypes()
      .stream()
      .map(vehicleType ->
        vehicleTypeMapper.mapVehicleType(vehicleType, feedProvider.getLanguage())
      )
      .collect(
        Collectors.toMap(
          VehicleType::getId,
          vehicleType -> vehicleType,
          (existing, duplicate) -> {
            log.warn(
              "Duplicate vehicle type found with ID: {}. Keeping first occurrence.",
              existing.getId()
            );
            return existing;
          }
        )
      );

    var lastUpdated = gbfsVehicleTypes.getLastUpdated();
    var ttl = gbfsVehicleTypes.getTtl();

    vehicleTypeCache.updateAll(
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
