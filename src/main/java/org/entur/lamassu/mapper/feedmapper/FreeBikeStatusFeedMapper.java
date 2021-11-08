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

import org.entur.gbfs.v2_2.free_bike_status.GBFSBike;
import org.entur.gbfs.v2_2.free_bike_status.GBFSData;
import org.entur.gbfs.v2_2.free_bike_status.GBFSFreeBikeStatus;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class FreeBikeStatusFeedMapper implements FeedMapper<GBFSFreeBikeStatus> {
    @Override
    public GBFSFreeBikeStatus map(GBFSFreeBikeStatus source, FeedProvider feedProvider) {
        if (source == null) {
            return null;
        }

        var mapped = new GBFSFreeBikeStatus();
        mapped.setVersion(source.getVersion());
        mapped.setLastUpdated(source.getLastUpdated());
        mapped.setTtl(source.getTtl());
        mapped.setData(mapData(source.getData(), feedProvider));
        return mapped;
    }

    private GBFSData mapData(GBFSData data, FeedProvider feedProvider) {
        var mapped = new GBFSData();
        mapped.setBikes(
                data.getBikes().stream()
                        .map(bike -> mapBike(bike, feedProvider))
                        .collect(Collectors.toList())
        );
        return mapped;
    }

    private GBFSBike mapBike(GBFSBike bike, FeedProvider feedProvider) {
        var mapped = new GBFSBike();
        mapped.setBikeId(IdMappers.mapId(feedProvider.getCodespace(), IdMappers.BIKE_ID_TYPE, bike.getBikeId()));
        mapped.setLat(bike.getLat());
        mapped.setLon(bike.getLon());
        mapped.setIsReserved(bike.getIsReserved());
        mapped.setIsDisabled(bike.getIsDisabled());
        mapped.setRentalUris(bike.getRentalUris());
        mapped.setVehicleTypeId(mapVehicleTypeId(bike.getVehicleTypeId(), feedProvider));
        mapped.setLastReported(bike.getLastReported());
        mapped.setCurrentRangeMeters(bike.getCurrentRangeMeters());
        mapped.setStationId(mapStationId(bike.getStationId(), feedProvider));
        mapped.setPricingPlanId(mapPricingPlanId(bike.getPricingPlanId(), feedProvider));
        return mapped;
    }

    private String mapVehicleTypeId(String vehicleTypeId, FeedProvider feedProvider) {
        if (vehicleTypeId == null) {
            return null;
        }

        return IdMappers.mapId(feedProvider.getCodespace(), IdMappers.VEHICLE_TYPE_ID_TYPE, vehicleTypeId);
    }

    private String mapStationId(String stationId, FeedProvider feedProvider) {
        if (stationId == null) {
            return null;
        }

        return IdMappers.mapId(feedProvider.getCodespace(), IdMappers.STATION_ID_TYPE, stationId);
    }

    private String mapPricingPlanId(String pricingPlanId, FeedProvider feedProvider) {
        if (pricingPlanId == null) {
            return null;
        }

        return IdMappers.mapId(feedProvider.getCodespace(), IdMappers.PRICING_PLAN_ID_TYPE, pricingPlanId);
    }
}
