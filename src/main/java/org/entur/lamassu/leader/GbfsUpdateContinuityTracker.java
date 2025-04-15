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

package org.entur.lamassu.leader;

import org.entur.gbfs.loader.v3.GbfsV3Delivery;
import org.entur.lamassu.cache.UpdateContinuityCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Service responsible for tracking continuity of GBFS updates for stations and vehicles.
 * This ensures that in case updates are missed, delta calculations will start from scratch.
 * Uses Redis for persistence to survive application restarts.
 */
@Component
public class GbfsUpdateContinuityTracker {

  private static Logger logger = LoggerFactory.getLogger(
    GbfsUpdateContinuityTracker.class
  );

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

    if (
      oldDelivery.vehicleStatus() == null ||
      previousBase == null ||
      !previousBase.equals(oldDelivery.vehicleStatus().getLastUpdated())
    ) {
      logger.warn(
        "Vehicle status update does not have continuity for system={}",
        systemId
      );
      return false;
    }

    return true;
  }

  public void updateVehicleUpdateContinuity(String systemId, GbfsV3Delivery delivery) {
    vehicleStatusBases.setLastUpdateTime(
      systemId,
      delivery.vehicleStatus().getLastUpdated()
    );
  }

  public void clearVehicleUpdateContinuity(String systemId) {
    vehicleStatusBases.setLastUpdateTime(systemId, null);
  }

  /**
   * Check if there is continuity in station status updates by comparing timestamps.
   * Returns false if updates have been missed, indicating we need to start delta calculations from scratch.
   */
  public boolean hasStationUpdateContinuity(String systemId, GbfsV3Delivery oldDelivery) {
    var previousBase = stationStatusBases.getLastUpdateTime(systemId);

    if (
      oldDelivery.stationStatus() == null ||
      previousBase == null ||
      !previousBase.equals(oldDelivery.stationStatus().getLastUpdated())
    ) {
      logger.warn(
        "Station status update does not have continuity for system={}",
        systemId
      );
      return false;
    }

    return true;
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

  public void clearStationUpdateContinuity(String systemId) {
    stationStatusBases.setLastUpdateTime(systemId, null);
  }
}
