package org.entur.lamassu.leader.entityupdater;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.entur.gbfs.loader.v3.GbfsV3Delivery;
import org.springframework.stereotype.Component;

/**
 * Service responsible for tracking continuity of GBFS updates for stations and vehicles.
 * This ensures that in case updates are missed, delta calculations will start from scratch.
 */
@Component
public class GbfsUpdateContinuityTracker {

  private final Map<String, Long> vehicleStatusBases = new ConcurrentHashMap<>();
  private final Map<String, Long> stationStatusBases = new ConcurrentHashMap<>();

  /**
   * Check if there is continuity in vehicle status updates by comparing timestamps.
   * Returns false if updates have been missed, indicating we need to start delta calculations from scratch.
   */
  public boolean hasVehicleUpdateContinuity(
    String systemId,
    GbfsV3Delivery oldDelivery,
    GbfsV3Delivery nextDelivery
  ) {
    var previousBase = vehicleStatusBases.put(
      systemId,
      nextDelivery.vehicleStatus().getLastUpdated().getTime()
    );

    if (oldDelivery.vehicleStatus() == null || previousBase == null) {
      return false;
    }

    return previousBase.equals(oldDelivery.vehicleStatus().getLastUpdated().getTime());
  }

  /**
   * Check if there is continuity in station status updates by comparing timestamps.
   * Returns false if updates have been missed, indicating we need to start delta calculations from scratch.
   */
  public boolean hasStationUpdateContinuity(
    String systemId,
    GbfsV3Delivery oldDelivery,
    GbfsV3Delivery nextDelivery
  ) {
    var previousBase = stationStatusBases.put(
      systemId,
      nextDelivery.stationStatus().getLastUpdated().getTime()
    );

    if (oldDelivery.stationStatus() == null || previousBase == null) {
      return false;
    }

    return previousBase.equals(oldDelivery.stationStatus().getLastUpdated().getTime());
  }
}
