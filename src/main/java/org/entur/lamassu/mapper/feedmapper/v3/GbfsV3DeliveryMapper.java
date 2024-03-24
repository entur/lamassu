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

package org.entur.lamassu.mapper.feedmapper.v3;

import org.entur.gbfs.loader.v3.GbfsV3Delivery;
import org.entur.gbfs.v3_0_RC2.gbfs.GBFSGbfs;
import org.entur.gbfs.v3_0_RC2.geofencing_zones.GBFSGeofencingZones;
import org.entur.gbfs.v3_0_RC2.station_information.GBFSStationInformation;
import org.entur.gbfs.v3_0_RC2.station_status.GBFSStationStatus;
import org.entur.gbfs.v3_0_RC2.system_alerts.GBFSSystemAlerts;
import org.entur.gbfs.v3_0_RC2.system_information.GBFSSystemInformation;
import org.entur.gbfs.v3_0_RC2.system_pricing_plans.GBFSSystemPricingPlans;
import org.entur.gbfs.v3_0_RC2.system_regions.GBFSSystemRegions;
import org.entur.gbfs.v3_0_RC2.vehicle_status.GBFSVehicleStatus;
import org.entur.gbfs.v3_0_RC2.vehicle_types.GBFSVehicleTypes;
import org.entur.lamassu.mapper.feedmapper.FeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The purpose of this mapper is to prepare GBFS v3 data for the APIs
 *
 * 1. Transform the discovery file (gbfs.json)
 * 2. Make sure IDs are properly codespaced
 * 3. Inject statically configred content
 */
@Component
public class GbfsV3DeliveryMapper {

  private final FeedMapper<GBFSGbfs> discoveryFeedMapper;
  private final FeedMapper<GBFSSystemInformation> systemInformationFeedMapper;
  private final FeedMapper<GBFSSystemAlerts> systemAlertsFeedMapper;
  private final FeedMapper<GBFSSystemRegions> systemRegionsFeedMapper;
  private final FeedMapper<GBFSSystemPricingPlans> systemPricingPlansFeedMapper;
  private final FeedMapper<GBFSVehicleTypes> vehicleTypesFeedMapper;
  private final FeedMapper<GBFSGeofencingZones> geofencingZonesFeedMapper;
  private final FeedMapper<GBFSStationInformation> stationInformationFeedMapper;
  private final FeedMapper<GBFSStationStatus> stationStatusFeedMapper;
  private final FeedMapper<GBFSVehicleStatus> vehicleStatusFeedMapper;

  @Autowired
  public GbfsV3DeliveryMapper(
    FeedMapper<GBFSGbfs> discoveryFeedMapper,
    FeedMapper<GBFSSystemInformation> systemInformationFeedMapper,
    FeedMapper<GBFSSystemAlerts> systemAlertsFeedMapper,
    FeedMapper<GBFSSystemRegions> systemRegionsFeedMapper,
    FeedMapper<GBFSSystemPricingPlans> systemPricingPlansFeedMapper,
    FeedMapper<GBFSVehicleTypes> vehicleTypesFeedMapper,
    FeedMapper<GBFSGeofencingZones> geofencingZonesFeedMapper,
    FeedMapper<GBFSStationInformation> stationInformationFeedMapper,
    FeedMapper<GBFSStationStatus> stationStatusFeedMapper,
    FeedMapper<GBFSVehicleStatus> vehicleStatusFeedMapper
  ) {
    this.discoveryFeedMapper = discoveryFeedMapper;
    this.systemInformationFeedMapper = systemInformationFeedMapper;
    this.systemAlertsFeedMapper = systemAlertsFeedMapper;
    this.systemRegionsFeedMapper = systemRegionsFeedMapper;
    this.systemPricingPlansFeedMapper = systemPricingPlansFeedMapper;
    this.vehicleTypesFeedMapper = vehicleTypesFeedMapper;
    this.geofencingZonesFeedMapper = geofencingZonesFeedMapper;
    this.stationInformationFeedMapper = stationInformationFeedMapper;
    this.stationStatusFeedMapper = stationStatusFeedMapper;
    this.vehicleStatusFeedMapper = vehicleStatusFeedMapper;
  }

  public GbfsV3Delivery mapGbfsDelivery(
    GbfsV3Delivery delivery,
    FeedProvider feedProvider
  ) {
    return new GbfsV3Delivery(
      discoveryFeedMapper.map(delivery.discovery(), feedProvider),
      // Lamassu currently only support producing a single version of GBFS, therefore
      // mapping of the versions file, if it exists, is intentionally skipped.
      null,
      systemInformationFeedMapper.map(delivery.systemInformation(), feedProvider),
      vehicleTypesFeedMapper.map(delivery.vehicleTypes(), feedProvider),
      stationInformationFeedMapper.map(delivery.stationInformation(), feedProvider),
      stationStatusFeedMapper.map(
        delivery.stationStatus(),
        feedProvider
        // TODO Should we continue to support this?
        //stationStatus ->
        //        VehicleTypeCapacityProducer.addToStations(stationStatus, mappedVehicleTypes)
      ),
      vehicleStatusFeedMapper.map(delivery.vehicleStatus(), feedProvider),
      systemRegionsFeedMapper.map(delivery.systemRegions(), feedProvider),
      systemPricingPlansFeedMapper.map(delivery.systemPricingPlans(), feedProvider),
      systemAlertsFeedMapper.map(delivery.systemAlerts(), feedProvider),
      geofencingZonesFeedMapper.map(delivery.geofencingZones(), feedProvider),
      null
    );
  }
}
