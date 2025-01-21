package org.entur.lamassu.leader.entityupdater;

import org.entur.gbfs.loader.v3.GbfsV3Delivery;
import org.entur.lamassu.cache.UpdateContinuityCache;
import org.springframework.stereotype.Component;

/**
 * Service responsible for tracking continuity of GBFS updates for stations and vehicles.
 * This ensures that in case updates are missed, delta calculations will start from scratch.
 * Uses Redis for persistence to survive application restarts.
 */
@Component
public class GbfsUpdateContinuityTracker {

  private final UpdateContinuityCache vehicleStatusBases;
  private final UpdateContinuityCache stationStatusBases;

  public GbfsUpdateContinuityTracker(
    UpdateContinuityCache vehicleUpdateContinuityCache,
    UpdateContinuityCache stationUpdateContinuityCache
  ) {
    this.vehicleStatusBases = vehicleUpdateContinuityCache;
    this.stationStatusBases = stationUpdateContinuityCache;
  }

  /**
   * Check if there is continuity in vehicle status updates by comparing timestamps.
   * Returns false if updates have been missed, indicating we need to start delta calculations from scratch.
   */
  public boolean hasVehicleUpdateContinuity(String systemId, GbfsV3Delivery oldDelivery) {
    var previousBase = vehicleStatusBases.getLastUpdateTime(systemId);

    if (oldDelivery.vehicleStatus() == null || previousBase == null) {
      return false;
    }

    return previousBase.equals(oldDelivery.vehicleStatus().getLastUpdated());
  }

  public void updateVehicleUpdateContinuity(String systemId, GbfsV3Delivery delivery) {
    vehicleStatusBases.setLastUpdateTime(
      systemId,
      delivery.vehicleStatus().getLastUpdated()
    );
  }

  /**
   * Check if there is continuity in station status updates by comparing timestamps.
   * Returns false if updates have been missed, indicating we need to start delta calculations from scratch.
   */
  public boolean hasStationUpdateContinuity(String systemId, GbfsV3Delivery oldDelivery) {
    var previousBase = stationStatusBases.getLastUpdateTime(systemId);

    if (oldDelivery.stationStatus() == null || previousBase == null) {
      return false;
    }

    return previousBase.equals(oldDelivery.stationStatus().getLastUpdated());
  }

  public void updateStationUpdateContinuity(
    String systemId,
    GbfsV3Delivery nextDelivery
  ) {
    stationStatusBases.setLastUpdateTime(
      systemId,
      nextDelivery.stationStatus().getLastUpdated()
    );
  }
}
