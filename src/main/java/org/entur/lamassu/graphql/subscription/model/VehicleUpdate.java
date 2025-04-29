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

package org.entur.lamassu.graphql.subscription.model;

import org.entur.lamassu.model.entities.Vehicle;

/**
 * Represents an update to a vehicle entity.
 * Contains information about the type of update and the vehicle itself.
 */
public class VehicleUpdate {

  private final String vehicleId;
  private final UpdateType updateType;
  private final Vehicle vehicle;

  /**
   * Creates a new VehicleUpdate.
   *
   * @param vehicleId The ID of the vehicle that was updated
   * @param updateType The type of update (CREATE, UPDATE, DELETE)
   * @param vehicle The vehicle entity (null for DELETE updates)
   */
  public VehicleUpdate(String vehicleId, UpdateType updateType, Vehicle vehicle) {
    this.vehicleId = vehicleId;
    this.updateType = updateType;
    this.vehicle = vehicle;
  }

  /**
   * Gets the ID of the vehicle that was updated.
   *
   * @return The vehicle ID
   */
  public String getVehicleId() {
    return vehicleId;
  }

  /**
   * Gets the type of update that occurred.
   *
   * @return The update type
   */
  public UpdateType getUpdateType() {
    return updateType;
  }

  /**
   * Gets the vehicle entity.
   * Will be null for DELETE updates.
   *
   * @return The vehicle entity, or null if the vehicle was deleted
   */
  public Vehicle getVehicle() {
    return vehicle;
  }
}
