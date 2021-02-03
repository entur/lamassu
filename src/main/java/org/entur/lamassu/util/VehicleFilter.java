package org.entur.lamassu.util;

import org.entur.lamassu.cache.SpatialIndexId;
import org.entur.lamassu.service.VehicleFilterParameters;

public class VehicleFilter {
    private VehicleFilter() {}

    public static boolean filterVehicle(SpatialIndexId parsedId, VehicleFilterParameters filters) {
        if (filters.getOperators() != null && !filters.getOperators().contains(parsedId.getOperator())) {
            return false;
        }

        if (filters.getCodespaces() != null && !filters.getCodespaces().contains(parsedId.getCodespace())) {
            return false;
        }

        if (filters.getFormFactors() != null && !filters.getFormFactors().contains(parsedId.getFormFactor())) {
            return false;
        }

        if (filters.getPropulsionTypes() != null && !filters.getPropulsionTypes().contains(parsedId.getPropulsionTypes())) {
            return false;
        }

        if (Boolean.FALSE.equals(filters.getIncludeReserved()) && Boolean.TRUE.equals(parsedId.getReserved())) {
            return false;
        }

        if (Boolean.FALSE.equals(filters.getIncludeDisabled()) && Boolean.TRUE.equals(parsedId.getDisabled())) {
            return false;
        }

        return true;
    }
}
