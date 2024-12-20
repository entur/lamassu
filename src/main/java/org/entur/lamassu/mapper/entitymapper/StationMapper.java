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

package org.entur.lamassu.mapper.entitymapper;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.entur.lamassu.model.entities.MultiPolygon;
import org.entur.lamassu.model.entities.ParkingType;
import org.entur.lamassu.model.entities.RentalMethod;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.VehicleDocksAvailability;
import org.entur.lamassu.model.entities.VehicleDocksCapacity;
import org.entur.lamassu.model.entities.VehicleTypeAvailability;
import org.entur.lamassu.model.entities.VehicleTypeCapacity;
import org.entur.lamassu.model.entities.VehicleTypesCapacity;
import org.jetbrains.annotations.NotNull;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSName;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSShortName;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStation;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSVehicleDocksCapacity;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSVehicleTypesCapacity;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSVehicleDocksAvailable;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSVehicleTypesAvailable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StationMapper {

  private final TranslationMapper translationMapper;
  private final RentalUrisMapper rentalUrisMapper;

  @Autowired
  public StationMapper(
    TranslationMapper translationMapper,
    RentalUrisMapper rentalUrisMapper
  ) {
    this.translationMapper = translationMapper;
    this.rentalUrisMapper = rentalUrisMapper;
  }

  public Station mapStation(
    GBFSStation stationInformation,
    org.mobilitydata.gbfs.v3_0.station_status.GBFSStation stationStatus,
    String systemId,
    String language
  ) {
    var station = new Station();
    station.setId(stationStatus.getStationId());
    station.setName(
      translationMapper.mapSingleTranslation(
        language,
        stationInformation
          .getName()
          .stream()
          .filter(name -> name.getLanguage().equals(language))
          .map(GBFSName::getText)
          .findFirst()
          .orElse(null)
      )
    );
    station.setShortName(
      translationMapper.mapSingleTranslation(
        language,
        Optional
          .ofNullable(stationInformation.getShortName())
          .orElse(Collections.emptyList())
          .stream()
          .filter(shortName -> shortName.getLanguage().equals(language))
          .map(GBFSShortName::getText)
          .findFirst()
          .orElse(null)
      )
    );
    station.setLat(stationInformation.getLat());
    station.setLon(stationInformation.getLon());
    station.setAddress(stationInformation.getAddress());
    station.setCrossStreet(stationInformation.getCrossStreet());
    station.setRegionId(stationInformation.getRegionId());
    station.setPostCode(stationInformation.getPostCode());
    station.setRentalMethods(mapRentalMethods(stationInformation.getRentalMethods()));
    station.setVirtualStation(stationInformation.getIsVirtualStation());
    station.setStationArea(mapStationArea(stationInformation.getStationArea()));
    station.setParkingType(mapParkingType(stationInformation.getParkingType()));
    station.setParkingHoop(stationInformation.getParkingHoop());
    station.setContactPhone(stationInformation.getContactPhone());
    station.setCapacity(
      stationInformation.getCapacity() != null ? stationInformation.getCapacity() : null
    );

    station.setVehicleCapacity(
      stationInformation.getVehicleTypesCapacity() != null
        ? mapVehicleCapacities(stationInformation.getVehicleTypesCapacity())
        : null
    );
    station.setVehicleTypesCapacity(
      stationInformation.getVehicleTypesCapacity() != null
        ? mapVehicleTypesCapacity(stationInformation.getVehicleTypesCapacity())
        : null
    );

    station.setVehicleTypeCapacity(
      stationInformation.getVehicleDocksCapacity() != null
        ? mapVehicleTypeCapacities(stationInformation.getVehicleDocksCapacity())
        : null
    );
    station.setVehicleDocksCapacity(
      stationInformation.getVehicleDocksCapacity() != null
        ? mapVehicleDocksCapacityToVehicleTypeCapacity(
          stationInformation.getVehicleDocksCapacity()
        )
        : null
    );

    station.setValetStation(stationInformation.getIsValetStation());
    station.setChargingStation(stationInformation.getIsChargingStation());
    station.setRentalUris(
      rentalUrisMapper.mapRentalUris(stationInformation.getRentalUris())
    );
    station.setNumBikesAvailable(
      stationStatus.getNumVehiclesAvailable() != null
        ? stationStatus.getNumVehiclesAvailable()
        : null
    );
    station.setNumVehiclesAvailable(
      stationStatus.getNumVehiclesAvailable() != null
        ? stationStatus.getNumVehiclesAvailable()
        : null
    );
    station.setVehicleTypesAvailable(
      stationStatus.getVehicleTypesAvailable() != null
        ? mapVehicleTypesAvailable(stationStatus.getVehicleTypesAvailable())
        : null
    );
    station.setNumBikesDisabled(
      stationStatus.getNumVehiclesDisabled() != null
        ? stationStatus.getNumVehiclesDisabled()
        : null
    );
    station.setNumVehiclesDisabled(
      stationStatus.getNumVehiclesDisabled() != null
        ? stationStatus.getNumVehiclesDisabled()
        : null
    );
    station.setNumDocksAvailable(
      stationStatus.getNumDocksAvailable() != null
        ? stationStatus.getNumDocksAvailable()
        : null
    );
    station.setVehicleDocksAvailable(
      stationStatus.getVehicleDocksAvailable() != null
        ? mapVehicleDocksAvailable(stationStatus.getVehicleDocksAvailable())
        : null
    );
    station.setNumDocksDisabled(station.getNumDocksDisabled());
    station.setInstalled(stationStatus.getIsInstalled());
    station.setRenting(stationStatus.getIsRenting());
    station.setReturning(stationStatus.getIsReturning());
    station.setLastReported(
      stationStatus.getLastReported() != null
        ? stationStatus.getLastReported().getTime() / 1000
        : null
    );
    station.setSystemId(systemId);
    station.setStationAreaPolylineEncodedMultiPolygon(
      station.getStationArea() != null
        ? station
          .getStationArea()
          .getCoordinates()
          .stream()
          .map(polygon ->
            polygon
              .stream()
              .map(ring ->
                ring
                  .stream()
                  .map(coords -> Point.fromLngLat(coords.get(0), coords.get(1)))
                  .toList()
              )
              .map(ring -> PolylineUtils.encode(ring, 6))
              .toList()
          )
          .toList()
        : Collections.emptyList()
    );

    return station;
  }

  private ParkingType mapParkingType(GBFSStation.ParkingType parkingType) {
    if (parkingType == null) {
      return null;
    }

    return ParkingType.valueOf(parkingType.value().toUpperCase());
  }

  private MultiPolygon mapStationArea(org.geojson.MultiPolygon stationArea) {
    if (stationArea == null) {
      return null;
    }

    return MultiPolygon.fromGeoJson(stationArea);
  }

  private List<RentalMethod> mapRentalMethods(
    List<org.mobilitydata.gbfs.v3_0.station_information.RentalMethod> rentalMethods
  ) {
    return Optional
      .ofNullable(rentalMethods)
      .map(values ->
        values
          .stream()
          .filter(Objects::nonNull)
          .map(rentalMethod -> RentalMethod.valueOf(rentalMethod.name().toUpperCase()))
          .toList()
      )
      .orElse(null);
  }

  private List<VehicleTypeCapacity> mapVehicleCapacities(
    List<GBFSVehicleTypesCapacity> vehicleCapacityList
  ) {
    return vehicleCapacityList
      .stream()
      .map(StationMapper::mapVehicleTypeCapacity)
      .toList();
  }

  private static @NotNull VehicleTypeCapacity mapVehicleTypeCapacity(
    GBFSVehicleTypesCapacity vehicleCapacity
  ) {
    var mapped = new VehicleTypeCapacity();
    mapped.setVehicleTypeId(vehicleCapacity.getVehicleTypeIds().getFirst());
    mapped.setCount(vehicleCapacity.getCount());
    return mapped;
  }

  private List<VehicleTypesCapacity> mapVehicleTypesCapacity(
    List<GBFSVehicleTypesCapacity> vehicleTypesCapacity
  ) {
    return vehicleTypesCapacity.stream().map(this::mapVehicleTypesCapacity).toList();
  }

  private VehicleTypesCapacity mapVehicleTypesCapacity(
    GBFSVehicleTypesCapacity vehicleTypesCapacity
  ) {
    var mapped = new VehicleTypesCapacity();
    mapped.setVehicleTypeIds(vehicleTypesCapacity.getVehicleTypeIds());
    mapped.setCount(vehicleTypesCapacity.getCount());
    return mapped;
  }

  private List<VehicleTypeCapacity> mapVehicleTypeCapacities(
    List<GBFSVehicleDocksCapacity> vehicleCapacity
  ) {
    return vehicleCapacity
      .stream()
      .map(this::mapVehicleDocksCapacityToVehicleTypeCapacity)
      .toList();
  }

  private VehicleTypeCapacity mapVehicleDocksCapacityToVehicleTypeCapacity(
    GBFSVehicleDocksCapacity vehicleDocksCapacity
  ) {
    var mapped = new VehicleTypeCapacity();
    mapped.setVehicleTypeId(vehicleDocksCapacity.getVehicleTypeIds().getFirst());
    mapped.setCount(vehicleDocksCapacity.getCount());
    return mapped;
  }

  private List<VehicleDocksCapacity> mapVehicleDocksCapacityToVehicleTypeCapacity(
    List<GBFSVehicleDocksCapacity> vehicleDocksCapacity
  ) {
    return vehicleDocksCapacity.stream().map(this::mapVehicleDocksCapacity).toList();
  }

  private VehicleDocksCapacity mapVehicleDocksCapacity(
    GBFSVehicleDocksCapacity vehicleDocksCapacity
  ) {
    var mapped = new VehicleDocksCapacity();
    mapped.setVehicleTypeIds(vehicleDocksCapacity.getVehicleTypeIds());
    mapped.setCount(vehicleDocksCapacity.getCount());
    return mapped;
  }

  private List<VehicleTypeAvailability> mapVehicleTypesAvailable(
    List<GBFSVehicleTypesAvailable> vehicleTypesAvailable
  ) {
    return vehicleTypesAvailable.stream().map(this::mapVehicleTypeAvailability).toList();
  }

  private VehicleTypeAvailability mapVehicleTypeAvailability(
    GBFSVehicleTypesAvailable vehicleTypeAvailability
  ) {
    var mapped = new VehicleTypeAvailability();
    mapped.setVehicleTypeId(vehicleTypeAvailability.getVehicleTypeId());
    mapped.setCount(vehicleTypeAvailability.getCount());
    return mapped;
  }

  private List<VehicleDocksAvailability> mapVehicleDocksAvailable(
    List<GBFSVehicleDocksAvailable> vehicleDocksAvailable
  ) {
    return vehicleDocksAvailable.stream().map(this::mapVehicleDocksAvailability).toList();
  }

  private VehicleDocksAvailability mapVehicleDocksAvailability(
    GBFSVehicleDocksAvailable vehicleDocksAvailability
  ) {
    var mapped = new VehicleDocksAvailability();
    mapped.setVehicleTypeIds(vehicleDocksAvailability.getVehicleTypeIds());
    mapped.setCount(vehicleDocksAvailability.getCount());
    return mapped;
  }
}
