package org.entur.lamassu.util;

import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;

public class SpatialIndexIdUtil {
    private SpatialIndexIdUtil() {}

    public static String createVehicleSpatialIndexId(Vehicle vehicle, FeedProvider feedProvider) {
        return vehicle.getId()
                + "_" + feedProvider.getCodespace()
                + "_" + feedProvider.getSystemId()
                + "_" + feedProvider.getOperatorId()
                + "_" + vehicle.getVehicleType().getFormFactor()
                + "_" + vehicle.getVehicleType().getPropulsionType()
                + "_" + vehicle.getReserved()
                + "_" + vehicle.getDisabled();
    }

    public static String createStationSpatialIndexId(Station station, FeedProvider feedProvider) {
        return station.getId()
                + "_" + feedProvider.getCodespace()
                + "_" + feedProvider.getSystemId()
                + "_" + feedProvider.getOperatorId();
    }
}
