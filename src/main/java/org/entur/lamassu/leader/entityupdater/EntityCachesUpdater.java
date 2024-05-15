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

import org.entur.gbfs.loader.v2.GbfsV2Delivery;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityCachesUpdater {

  private final VehiclesUpdater vehiclesUpdater;
  private final StationsUpdater stationsUpdater;
  private final GeofencingZonesUpdater geofencingZonesUpdater;

  @Autowired
  public EntityCachesUpdater(
    VehiclesUpdater vehiclesUpdater,
    StationsUpdater stationsUpdater,
    GeofencingZonesUpdater geofencingZonesUpdater
  ) {
    this.vehiclesUpdater = vehiclesUpdater;
    this.stationsUpdater = stationsUpdater;
    this.geofencingZonesUpdater = geofencingZonesUpdater;
  }

  public void updateEntityCaches(
    FeedProvider feedProvider,
    GbfsV2Delivery delivery,
    GbfsV2Delivery oldDelivery
  ) {
    if (canUpdateVehicles(delivery, feedProvider)) {
      vehiclesUpdater.addOrUpdateVehicles(feedProvider, delivery, oldDelivery);
    }

    if (canUpdateStations(delivery, feedProvider)) {
      stationsUpdater.addOrUpdateStations(feedProvider, delivery, oldDelivery);
    }

    if (
      delivery.geofencingZones() != null &&
      (
        feedProvider.getExcludeFeeds() == null ||
        !feedProvider.getExcludeFeeds().contains(GBFSFeedName.GeofencingZones)
      )
    ) {
      geofencingZonesUpdater.addOrUpdateGeofencingZones(
        feedProvider,
        delivery.geofencingZones()
      );
    }
  }

  private boolean canUpdateVehicles(GbfsV2Delivery delivery, FeedProvider feedProvider) {
    if (
      feedProvider.getExcludeFeeds() != null &&
      feedProvider.getExcludeFeeds().contains(GBFSFeedName.FreeBikeStatus)
    ) {
      return false;
    }

    return (
      delivery.freeBikeStatus() != null &&
      delivery.freeBikeStatus().getData() != null &&
      delivery.systemInformation() != null &&
      delivery.systemInformation().getData() != null &&
      delivery.vehicleTypes() != null &&
      delivery.vehicleTypes().getData() != null &&
      delivery.systemPricingPlans() != null &&
      delivery.systemPricingPlans().getData() != null
    );
  }

  private boolean canUpdateStations(GbfsV2Delivery delivery, FeedProvider feedProvider) {
    if (
      feedProvider.getExcludeFeeds() != null &&
      (
        feedProvider.getExcludeFeeds().contains(GBFSFeedName.StationInformation) ||
        feedProvider.getExcludeFeeds().contains(GBFSFeedName.StationStatus)
      )
    ) {
      return false;
    }

    return (
      delivery.stationStatus() != null &&
      delivery.stationInformation() != null &&
      delivery.stationStatus().getData() != null &&
      delivery.stationInformation().getData() != null &&
      delivery.systemInformation() != null &&
      delivery.systemInformation().getData() != null &&
      delivery.vehicleTypes() != null &&
      delivery.vehicleTypes().getData() != null &&
      delivery.systemPricingPlans() != null &&
      delivery.systemPricingPlans().getData() != null
    );
  }
}
