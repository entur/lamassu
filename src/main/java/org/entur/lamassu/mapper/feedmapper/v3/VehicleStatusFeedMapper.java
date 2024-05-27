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

package org.entur.lamassu.mapper.feedmapper.v3;

import static org.entur.lamassu.mapper.feedmapper.IdMappers.mapPricingPlanId;
import static org.entur.lamassu.mapper.feedmapper.IdMappers.mapStationId;
import static org.entur.lamassu.mapper.feedmapper.IdMappers.mapVehicleTypeId;

import java.util.stream.Collectors;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.mapper.feedmapper.IdMappers;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSData;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicleStatus;
import org.springframework.stereotype.Component;

@Component
public class VehicleStatusFeedMapper extends AbstractFeedMapper<GBFSVehicleStatus> {

  private static final String TARGET_GBFS_VERSION = "3.0";

  @Override
  public GBFSVehicleStatus map(GBFSVehicleStatus source, FeedProvider feedProvider) {
    if (source == null) {
      return null;
    }

    var mapped = new GBFSVehicleStatus();
    mapped.setVersion(TARGET_GBFS_VERSION);
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setTtl(source.getTtl());
    mapped.setData(mapData(source.getData(), feedProvider));
    return mapped;
  }

  private GBFSData mapData(GBFSData data, FeedProvider feedProvider) {
    var mapped = new GBFSData();
    mapped.setVehicles(
      data
        .getVehicles()
        .stream()
        .map(vehicle -> mapVehicle(vehicle, feedProvider))
        .collect(Collectors.toList())
    );
    return mapped;
  }

  protected GBFSVehicle mapVehicle(GBFSVehicle vehicle, FeedProvider feedProvider) {
    var mapped = new GBFSVehicle();
    mapped.setVehicleId(
      IdMappers.mapId(
        feedProvider.getCodespace(),
        IdMappers.VEHICLE_ID_TYPE,
        vehicle.getVehicleId()
      )
    );
    mapped.setLat(vehicle.getLat());
    mapped.setLon(vehicle.getLon());
    mapped.setIsReserved(vehicle.getIsReserved());
    mapped.setIsDisabled(vehicle.getIsDisabled());
    mapped.setRentalUris(vehicle.getRentalUris());
    mapped.setVehicleTypeId(mapVehicleTypeId(vehicle.getVehicleTypeId(), feedProvider));
    mapped.setLastReported(vehicle.getLastReported());
    mapped.setCurrentRangeMeters(
      vehicle.getCurrentRangeMeters() != null ? vehicle.getCurrentRangeMeters() : 0
    );
    mapped.setCurrentFuelPercent(vehicle.getCurrentFuelPercent());
    mapped.setStationId(mapStationId(vehicle.getStationId(), feedProvider));
    mapped.setHomeStationId(mapStationId(vehicle.getHomeStationId(), feedProvider));
    mapped.setPricingPlanId(mapPricingPlanId(vehicle.getPricingPlanId(), feedProvider));
    mapped.setVehicleEquipment(vehicle.getVehicleEquipment());
    mapped.setAvailableUntil(vehicle.getAvailableUntil());
    return mapped;
  }
}
