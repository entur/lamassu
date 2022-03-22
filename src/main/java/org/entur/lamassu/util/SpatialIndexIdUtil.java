package org.entur.lamassu.util;

import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;

import static org.entur.lamassu.cache.AbstractSpatialIndexId.SPATIAL_INDEX_ID_SEPARATOR;

public class SpatialIndexIdUtil {
    private SpatialIndexIdUtil() {}

    public static String createVehicleSpatialIndexId(Vehicle vehicle, FeedProvider feedProvider) {
        return vehicle.getId()
                + SPATIAL_INDEX_ID_SEPARATOR + feedProvider.getCodespace()
                + SPATIAL_INDEX_ID_SEPARATOR + feedProvider.getSystemId()
                + SPATIAL_INDEX_ID_SEPARATOR + feedProvider.getOperatorId()
                + SPATIAL_INDEX_ID_SEPARATOR + vehicle.getVehicleType().getFormFactor()
                + SPATIAL_INDEX_ID_SEPARATOR + vehicle.getVehicleType().getPropulsionType()
                + SPATIAL_INDEX_ID_SEPARATOR + vehicle.getReserved()
                + SPATIAL_INDEX_ID_SEPARATOR + vehicle.getDisabled();
    }

    public static String createStationSpatialIndexId(Station station, FeedProvider feedProvider) {
        return station.getId()
                + SPATIAL_INDEX_ID_SEPARATOR + feedProvider.getCodespace()
                + SPATIAL_INDEX_ID_SEPARATOR + feedProvider.getSystemId()
                + SPATIAL_INDEX_ID_SEPARATOR + feedProvider.getOperatorId();
    }
}
