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

package org.entur.lamassu.mapper.feedmapper.v2;

import static org.entur.lamassu.mapper.feedmapper.IdMappers.mapPricingPlanId;
import static org.entur.lamassu.mapper.feedmapper.IdMappers.mapStationId;
import static org.entur.lamassu.mapper.feedmapper.IdMappers.mapVehicleTypeId;

import java.util.stream.Collectors;
import org.entur.gbfs.v2_3.free_bike_status.GBFSBike;
import org.entur.gbfs.v2_3.free_bike_status.GBFSData;
import org.entur.gbfs.v2_3.free_bike_status.GBFSFreeBikeStatus;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.mapper.feedmapper.IdMappers;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FreeBikeStatusFeedMapper extends AbstractFeedMapper<GBFSFreeBikeStatus> {

  @Value("${org.entur.lamassu.targetGbfsVersion:2.2}")
  private String targetGbfsVersion;

  @Override
  public GBFSFreeBikeStatus map(GBFSFreeBikeStatus source, FeedProvider feedProvider) {
    if (source == null) {
      return null;
    }

    var mapped = new GBFSFreeBikeStatus();
    mapped.setVersion(targetGbfsVersion);
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setTtl(source.getTtl());
    mapped.setData(mapData(source.getData(), feedProvider));
    return mapped;
  }

  private GBFSData mapData(GBFSData data, FeedProvider feedProvider) {
    var mapped = new GBFSData();
    mapped.setBikes(
      data
        .getBikes()
        .stream()
        .map(bike -> mapBike(bike, feedProvider))
        .collect(Collectors.toList())
    );
    return mapped;
  }

  protected GBFSBike mapBike(GBFSBike bike, FeedProvider feedProvider) {
    var mapped = new GBFSBike();
    mapped.setBikeId(
      IdMappers.mapId(
        feedProvider.getCodespace(),
        IdMappers.BIKE_ID_TYPE,
        bike.getBikeId()
      )
    );
    mapped.setLat(bike.getLat());
    mapped.setLon(bike.getLon());
    mapped.setIsReserved(bike.getIsReserved());
    mapped.setIsDisabled(bike.getIsDisabled());
    mapped.setRentalUris(bike.getRentalUris());
    mapped.setVehicleTypeId(mapVehicleTypeId(bike.getVehicleTypeId(), feedProvider));
    mapped.setLastReported(bike.getLastReported());
    mapped.setCurrentRangeMeters(
      bike.getCurrentRangeMeters() != null ? bike.getCurrentRangeMeters() : 0
    );
    mapped.setCurrentFuelPercent(bike.getCurrentFuelPercent());
    mapped.setStationId(mapStationId(bike.getStationId(), feedProvider));
    mapped.setHomeStationId(mapStationId(bike.getHomeStationId(), feedProvider));
    mapped.setPricingPlanId(mapPricingPlanId(bike.getPricingPlanId(), feedProvider));
    mapped.setVehicleEquipment(bike.getVehicleEquipment());
    mapped.setAvailableUntil(bike.getAvailableUntil());
    return mapped;
  }
}
