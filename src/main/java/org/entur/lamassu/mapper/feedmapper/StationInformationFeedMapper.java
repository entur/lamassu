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

package org.entur.lamassu.mapper.feedmapper;

import java.util.List;
import java.util.stream.Collectors;
import org.entur.gbfs.v2_3.station_information.GBFSData;
import org.entur.gbfs.v2_3.station_information.GBFSStation;
import org.entur.gbfs.v2_3.station_information.GBFSStationInformation;
import org.entur.gbfs.v2_3.station_information.GBFSVehicleCapacity;
import org.entur.gbfs.v2_3.station_information.GBFSVehicleTypeCapacity;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StationInformationFeedMapper
  extends AbstractFeedMapper<GBFSStationInformation> {

  @Value("${org.entur.lamassu.targetGbfsVersion:2.2}")
  private String targetGbfsVersion;

  @Override
  public GBFSStationInformation map(
    GBFSStationInformation source,
    FeedProvider feedProvider
  ) {
    if (source == null) {
      return null;
    }

    var mapped = new GBFSStationInformation();
    mapped.setVersion(targetGbfsVersion);
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setTtl(source.getTtl());
    mapped.setData(mapData(source.getData(), feedProvider.getCodespace()));
    return mapped;
  }

  private GBFSData mapData(GBFSData data, String codespace) {
    var mapped = new GBFSData();
    mapped.setStations(mapStations(data.getStations(), codespace));
    return mapped;
  }

  private List<GBFSStation> mapStations(List<GBFSStation> stations, String codespace) {
    return stations
      .stream()
      .map(station -> mapStation(station, codespace))
      .collect(Collectors.toList());
  }

  private GBFSStation mapStation(GBFSStation gbfsStation, String codespace) {
    var mapped = new GBFSStation();
    mapped.setStationId(
      IdMappers.mapId(codespace, IdMappers.STATION_ID_TYPE, gbfsStation.getStationId())
    );
    mapped.setName(gbfsStation.getName());
    mapped.setShortName(gbfsStation.getShortName());
    mapped.setLat(gbfsStation.getLat());
    mapped.setLon(gbfsStation.getLon());
    mapped.setAddress(gbfsStation.getAddress());
    mapped.setCrossStreet(gbfsStation.getCrossStreet());
    mapped.setRegionId(mapRegionId(codespace, gbfsStation.getRegionId()));
    mapped.setPostCode(gbfsStation.getPostCode());
    mapped.setRentalMethods(gbfsStation.getRentalMethods());
    mapped.setIsVirtualStation(gbfsStation.getIsVirtualStation());
    mapped.setStationArea(gbfsStation.getStationArea());
    mapped.setParkingType(gbfsStation.getParkingType());
    mapped.setParkingHoop(gbfsStation.getParkingHoop());
    mapped.setContactPhone(gbfsStation.getContactPhone());
    mapped.setCapacity(gbfsStation.getCapacity());
    mapped.setVehicleCapacity(
      mapVehicleCapacity(gbfsStation.getVehicleCapacity(), codespace)
    );
    mapped.setVehicleTypeCapacity(
      mapVehicleTypeCapacity(gbfsStation.getVehicleTypeCapacity(), codespace)
    );
    mapped.setIsValetStation(gbfsStation.getIsValetStation());
    mapped.setIsChargingStation(gbfsStation.getIsChargingStation());
    mapped.setRentalUris(gbfsStation.getRentalUris());
    return mapped;
  }

  private GBFSVehicleCapacity mapVehicleCapacity(
    GBFSVehicleCapacity vehicleCapacity,
    String codespace
  ) {
    if (vehicleCapacity == null) {
      return null;
    }

    var mapped = new GBFSVehicleCapacity();
    vehicleCapacity
      .getAdditionalProperties()
      .forEach((key, value) ->
        mapped.setAdditionalProperty(
          IdMappers.mapId(codespace, IdMappers.VEHICLE_TYPE_ID_TYPE, key),
          value
        )
      );
    return mapped;
  }

  private GBFSVehicleTypeCapacity mapVehicleTypeCapacity(
    GBFSVehicleTypeCapacity vehicleTypeCapacity,
    String codespace
  ) {
    if (vehicleTypeCapacity == null) {
      return null;
    }

    var mapped = new GBFSVehicleTypeCapacity();
    vehicleTypeCapacity
      .getAdditionalProperties()
      .forEach((key, value) ->
        mapped.setAdditionalProperty(
          IdMappers.mapId(codespace, IdMappers.VEHICLE_TYPE_ID_TYPE, key),
          value
        )
      );
    return mapped;
  }

  private String mapRegionId(String codespace, String regionId) {
    if (regionId == null) {
      return null;
    }

    return IdMappers.mapId(codespace, IdMappers.REGION_ID_TYPE, regionId);
  }
}
