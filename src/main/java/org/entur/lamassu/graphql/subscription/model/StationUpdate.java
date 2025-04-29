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

import org.entur.lamassu.model.entities.Station;

/**
 * Represents an update to a station entity.
 * Contains information about the type of update and the station itself.
 */
public class StationUpdate {

  private final String stationId;
  private final UpdateType updateType;
  private final Station station;

  /**
   * Creates a new StationUpdate.
   *
   * @param stationId The ID of the station that was updated
   * @param updateType The type of update (CREATE, UPDATE, DELETE)
   * @param station The station entity (null for DELETE updates)
   */
  public StationUpdate(String stationId, UpdateType updateType, Station station) {
    this.stationId = stationId;
    this.updateType = updateType;
    this.station = station;
  }

  /**
   * Gets the ID of the station that was updated.
   *
   * @return The station ID
   */
  public String getStationId() {
    return stationId;
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
   * Gets the station entity.
   * Will be null for DELETE updates.
   *
   * @return The station entity, or null if the station was deleted
   */
  public Station getStation() {
    return station;
  }
}
