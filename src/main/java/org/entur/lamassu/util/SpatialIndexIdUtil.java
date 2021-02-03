package org.entur.lamassu.util;

import org.entur.lamassu.model.FeedProvider;
import org.entur.lamassu.model.FormFactor;
import org.entur.lamassu.model.ParsedSpatialIndexId;
import org.entur.lamassu.model.PropulsionType;
import org.entur.lamassu.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpatialIndexIdUtil {
    private static final Logger logger = LoggerFactory.getLogger(SpatialIndexIdUtil.class);
    public static String getIndexId(Vehicle vehicle, FeedProvider feedProvider) {
        return vehicle.getId()
                + "_" + feedProvider.getName()
                + "_" + feedProvider.getCodespace()
                + "_" + vehicle.getVehicleType().getFormFactor()
                + "_" + vehicle.getVehicleType().getPropulsionType()
                + "_" + vehicle.getReserved()
                + "_" + vehicle.getDisabled();
    }

    public static ParsedSpatialIndexId parseIndexId(String indexId) {
        try {
            var parsed = new ParsedSpatialIndexId();
            var parts = indexId.split("_");
            parsed.setVehicleId(parts[0]);
            parsed.setOperator(parts[1]);
            parsed.setCodespace(parts[2]);
            parsed.setFormFactor(FormFactor.valueOf(parts[3]));
            parsed.setPropulsionTypes(PropulsionType.valueOf(parts[4]));
            parsed.setReserved(Boolean.parseBoolean(parts[5]));
            parsed.setDisabled(Boolean.parseBoolean(parts[6]));
            return parsed;
        } catch (IndexOutOfBoundsException e) {
            logger.warn("Caught IndexOutOfBoundsException while trying to parse spatial index id {}", indexId, e);
            return null;
        }
    }
}
