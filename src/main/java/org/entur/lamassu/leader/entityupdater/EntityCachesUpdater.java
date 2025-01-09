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

import org.entur.gbfs.loader.v3.GbfsV3Delivery;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityCachesUpdater {

  private final SystemUpdater systemUpdater;
  private final VehicleTypesUpdater vehicleTypesUpdater;
  private final PricingPlansUpdater pricingPlansUpdater;
  private final RegionsUpdater regionsUpdater;
  private final VehiclesUpdater vehiclesUpdater;
  private final StationsUpdater stationsUpdater;
  private final GeofencingZonesUpdater geofencingZonesUpdater;

  @Autowired
  public EntityCachesUpdater(
    SystemUpdater systemUpdater,
    VehicleTypesUpdater vehicleTypesUpdater,
    PricingPlansUpdater pricingPlansUpdater,
    RegionsUpdater regionsUpdater,
    VehiclesUpdater vehiclesUpdater,
    StationsUpdater stationsUpdater,
    GeofencingZonesUpdater geofencingZonesUpdater
  ) {
    this.systemUpdater = systemUpdater;
    this.vehicleTypesUpdater = vehicleTypesUpdater;
    this.pricingPlansUpdater = pricingPlansUpdater;
    this.regionsUpdater = regionsUpdater;
    this.vehiclesUpdater = vehiclesUpdater;
    this.stationsUpdater = stationsUpdater;
    this.geofencingZonesUpdater = geofencingZonesUpdater;
  }

  public void updateEntityCaches(
    FeedProvider feedProvider,
    GbfsV3Delivery delivery,
    GbfsV3Delivery oldDelivery
  ) {
    if (canUpdateSystem(delivery, feedProvider)) {
      systemUpdater.update(delivery.systemInformation(), feedProvider);
    }

    if (canUpdateVehicleTypes(delivery, feedProvider)) {
      vehicleTypesUpdater.update(delivery.vehicleTypes(), feedProvider);
    }

    if (canUpdatePricingPlans(delivery, feedProvider)) {
      pricingPlansUpdater.update(delivery.systemPricingPlans());
    }

    if (canUpdateRegions(delivery, feedProvider)) {
      regionsUpdater.updateRegions(delivery.systemRegions(), feedProvider.getLanguage());
    }

    if (canUpdateVehicles(delivery, feedProvider)) {
      vehiclesUpdater.addOrUpdateVehicles(feedProvider, delivery, oldDelivery);
    }

    if (canUpdateStations(delivery, feedProvider)) {
      stationsUpdater.addOrUpdateStations(feedProvider, delivery, oldDelivery);
    }

    if (canUpdateGeofencingZones(delivery, feedProvider)) {
      geofencingZonesUpdater.addOrUpdateGeofencingZones(
        feedProvider,
        delivery.geofencingZones()
      );
    }
  }

  private boolean canUpdateSystem(GbfsV3Delivery delivery, FeedProvider feedProvider) {
    if (exclude(feedProvider, GBFSFeedName.SystemInformation)) {
      return false;
    }
    return delivery.systemInformation() != null;
  }

  private boolean canUpdateVehicleTypes(
    GbfsV3Delivery delivery,
    FeedProvider feedProvider
  ) {
    if (exclude(feedProvider, GBFSFeedName.VehicleTypes)) {
      return false;
    }
    return delivery.vehicleTypes() != null;
  }

  private boolean canUpdatePricingPlans(
    GbfsV3Delivery delivery,
    FeedProvider feedProvider
  ) {
    if (exclude(feedProvider, GBFSFeedName.SystemPricingPlans)) {
      return false;
    }
    return delivery.systemPricingPlans() != null;
  }

  private boolean canUpdateRegions(GbfsV3Delivery delivery, FeedProvider feedProvider) {
    if (exclude(feedProvider, GBFSFeedName.SystemRegions)) {
      return false;
    }
    return delivery.systemRegions() != null;
  }

  private boolean canUpdateGeofencingZones(
    GbfsV3Delivery delivery,
    FeedProvider feedProvider
  ) {
    if (exclude(feedProvider, GBFSFeedName.GeofencingZones)) {
      return false;
    }

    return delivery.geofencingZones() != null;
  }

  private boolean canUpdateVehicles(GbfsV3Delivery delivery, FeedProvider feedProvider) {
    if (exclude(feedProvider, GBFSFeedName.FreeBikeStatus)) {
      return false;
    }

    return (
      delivery.vehicleStatus() != null &&
      delivery.vehicleStatus().getData() != null &&
      delivery.systemInformation() != null &&
      delivery.systemInformation().getData() != null &&
      delivery.vehicleTypes() != null &&
      delivery.vehicleTypes().getData() != null &&
      delivery.systemPricingPlans() != null &&
      delivery.systemPricingPlans().getData() != null
    );
  }

  private boolean canUpdateStations(GbfsV3Delivery delivery, FeedProvider feedProvider) {
    if (
      exclude(feedProvider, GBFSFeedName.StationInformation) ||
      exclude(feedProvider, GBFSFeedName.StationStatus)
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

  private boolean exclude(FeedProvider feedProvider, GBFSFeedName feedName) {
    return (
      feedProvider.getExcludeFeeds() != null &&
      feedProvider.getExcludeFeeds().contains(feedName)
    );
  }
}
