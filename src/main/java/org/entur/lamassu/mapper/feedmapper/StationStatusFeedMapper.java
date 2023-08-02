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
import java.util.Optional;
import java.util.stream.Collectors;
import org.entur.gbfs.v2_3.station_status.GBFSData;
import org.entur.gbfs.v2_3.station_status.GBFSStation;
import org.entur.gbfs.v2_3.station_status.GBFSStationStatus;
import org.entur.gbfs.v2_3.station_status.GBFSVehicleDocksAvailable;
import org.entur.gbfs.v2_3.station_status.GBFSVehicleTypesAvailable;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StationStatusFeedMapper extends AbstractFeedMapper<GBFSStationStatus> {

  @Value("${org.entur.lamassu.targetGbfsVersion:2.2}")
  private String targetGbfsVersion;

  @Override
  public GBFSStationStatus map(GBFSStationStatus source, FeedProvider feedProvider) {
    if (source == null) {
      return null;
    }

    var mapped = new GBFSStationStatus();
    mapped.setVersion(targetGbfsVersion);
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setTtl(source.getTtl());
    mapped.setData(mapData(source.getData(), feedProvider));
    return mapped;
  }

  private GBFSData mapData(GBFSData data, FeedProvider feedProvider) {
    var mapped = new GBFSData();
    mapped.setStations(
      data
        .getStations()
        .stream()
        .map(station -> mapStation(station, feedProvider))
        .collect(Collectors.toList())
    );
    return mapped;
  }

  private GBFSStation mapStation(GBFSStation station, FeedProvider feedProvider) {
    var mapped = new GBFSStation();
    mapped.setStationId(
      IdMappers.mapId(
        feedProvider.getCodespace(),
        IdMappers.STATION_ID_TYPE,
        station.getStationId()
      )
    );
    mapped.setNumBikesAvailable(station.getNumBikesAvailable());
    mapped.setVehicleTypesAvailable(
      mapVehicleTypesAvailable(station.getVehicleTypesAvailable(), feedProvider)
        .orElse(null)
    );
    mapped.setNumBikesDisabled(station.getNumBikesDisabled());
    mapped.setNumDocksAvailable(station.getNumDocksAvailable());
    mapped.setVehicleDocksAvailable(
      mapVehicleDocksAvailable(station.getVehicleDocksAvailable(), feedProvider)
        .orElse(null)
    );
    mapped.setNumDocksDisabled(station.getNumDocksDisabled());
    mapped.setIsInstalled(station.getIsInstalled());
    mapped.setIsRenting(station.getIsRenting());
    mapped.setIsReturning(station.getIsReturning());
    mapped.setLastReported(station.getLastReported());
    return mapped;
  }

  private Optional<List<GBFSVehicleTypesAvailable>> mapVehicleTypesAvailable(
    List<GBFSVehicleTypesAvailable> vehicleTypesAvailable,
    FeedProvider feedProvider
  ) {
    return Optional
      .ofNullable(vehicleTypesAvailable)
      .map(vtsa ->
        vtsa
          .stream()
          .map(vta -> {
            var mapped = new GBFSVehicleTypesAvailable();
            mapped.setVehicleTypeId(
              IdMappers.mapId(
                feedProvider.getCodespace(),
                IdMappers.VEHICLE_TYPE_ID_TYPE,
                vta.getVehicleTypeId()
              )
            );
            mapped.setCount(vta.getCount());
            return mapped;
          })
          .collect(Collectors.toList())
      );
  }

  private Optional<List<GBFSVehicleDocksAvailable>> mapVehicleDocksAvailable(
    List<GBFSVehicleDocksAvailable> vehicleDocksAvailable,
    FeedProvider feedProvider
  ) {
    return Optional
      .ofNullable(vehicleDocksAvailable)
      .map(vdsa ->
        vdsa
          .stream()
          .map(vda -> {
            var mapped = new GBFSVehicleDocksAvailable();
            mapped.setVehicleTypeIds(
              vda
                .getVehicleTypeIds()
                .stream()
                .map(id ->
                  IdMappers.mapId(
                    feedProvider.getCodespace(),
                    IdMappers.VEHICLE_TYPE_ID_TYPE,
                    id
                  )
                )
                .collect(Collectors.toList())
            );
            mapped.setCount(vda.getCount());
            return mapped;
          })
          .collect(Collectors.toList())
      );
  }
}
