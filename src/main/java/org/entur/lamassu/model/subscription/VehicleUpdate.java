package org.entur.lamassu.model.subscription;

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
