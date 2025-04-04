package org.entur.lamassu.model.subscription;

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
