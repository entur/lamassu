package org.entur.lamassu.util;

import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;

public class SpatialIndexIdUtil {
    private SpatialIndexIdUtil() {}

    public static VehicleSpatialIndexId createVehicleSpatialIndexId(Vehicle vehicle, FeedProvider feedProvider) {
        var id = new VehicleSpatialIndexId();
        id.setId(vehicle.getId());
        id.setCodespace(feedProvider.getCodespace());
        id.setSystemId(feedProvider.getSystemId());
        id.setOperatorId(feedProvider.getOperatorId());
        id.setFormFactor(vehicle.getVehicleType().getFormFactor());
        id.setPropulsionType(vehicle.getVehicleType().getPropulsionType());
        id.setReserved(vehicle.getReserved());
        id.setDisabled(vehicle.getDisabled());
        return id;
    }

    public static StationSpatialIndexId createStationSpatialIndexId(Station station, FeedProvider feedProvider) {
        var id = new StationSpatialIndexId();
        id.setId(station.getId());
        id.setCodespace(feedProvider.getCodespace());
        id.setSystemId(feedProvider.getSystemId());
        id.setOperatorId(feedProvider.getOperatorId());
        return id;
    }
}
