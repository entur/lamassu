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
import org.entur.lamassu.model.id.DefaultIdValidator;
import org.entur.lamassu.model.id.IdBuilder;
import org.entur.lamassu.model.id.predicate.IdPredicateBuilder;
import org.entur.lamassu.model.provider.FeedProvider;

public class IdMappers {

  public static final String STATION_ID_TYPE = "Station";
  public static final String REGION_ID_TYPE = "Region";
  public static final String ALERT_ID_TYPE = "Alert";
  public static final String PRICING_PLAN_ID_TYPE = "PricingPlan";
  public static final String VEHICLE_TYPE_ID_TYPE = "VehicleType";
  public static final String BIKE_ID_TYPE = "Vehicle";
  public static final String VEHICLE_ID_TYPE = "Vehicle";

  private IdMappers() {}

  public static String mapId(String codespace, String type, String value) {
    // Values that should not be mapped
    if (value == null || value.isBlank()) {
      return value;
    }

    var predicate = IdPredicateBuilder
      .newInstance()
      .withCodespace(codespace)
      .withType(type)
      .build();
    if (predicate.test(value)) {
      return value;
    } else {
      return IdBuilder
        .newInstance()
        .withCodespace(codespace)
        .withType(type)
        .withValue(value)
        .build();
    }
  }

  public static Optional<List<String>> mapIds(
    String codespace,
    String type,
    List<String> values
  ) {
    return Optional
      .ofNullable(values)
      .map(v ->
        v.stream().map(id -> mapId(codespace, type, id)).collect(Collectors.toList())
      );
  }

  public static String mapVehicleTypeId(String vehicleTypeId, FeedProvider feedProvider) {
    if (feedProvider.getVehicleTypes() != null) {
      return IdMappers.mapId(
        feedProvider.getCodespace(),
        IdMappers.VEHICLE_TYPE_ID_TYPE,
        feedProvider.getVehicleTypes().get(0).getVehicleTypeId()
      );
    }

    if (vehicleTypeId == null) {
      return null;
    }

    return IdMappers.mapId(
      feedProvider.getCodespace(),
      IdMappers.VEHICLE_TYPE_ID_TYPE,
      vehicleTypeId
    );
  }

  public static String mapStationId(String stationId, FeedProvider feedProvider) {
    if (stationId == null) {
      return null;
    }

    return IdMappers.mapId(
      feedProvider.getCodespace(),
      IdMappers.STATION_ID_TYPE,
      stationId
    );
  }

  public static String mapPricingPlanId(String pricingPlanId, FeedProvider feedProvider) {
    if (feedProvider.getPricingPlans() != null) {
      return IdMappers.mapId(
        feedProvider.getCodespace(),
        IdMappers.PRICING_PLAN_ID_TYPE,
        feedProvider.getPricingPlans().get(0).getPlanId()
      );
    }

    if (pricingPlanId == null) {
      return null;
    }

    return IdMappers.mapId(
      feedProvider.getCodespace(),
      IdMappers.PRICING_PLAN_ID_TYPE,
      pricingPlanId
    );
  }

  public static String mapRegionId(String codespace, String regionId) {
    if (regionId == null) {
      return null;
    }

    return IdMappers.mapId(codespace, IdMappers.REGION_ID_TYPE, regionId);
  }
}
