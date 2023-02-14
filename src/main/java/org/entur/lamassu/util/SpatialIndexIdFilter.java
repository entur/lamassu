package org.entur.lamassu.util;

import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.service.StationFilterParameters;
import org.entur.lamassu.service.VehicleFilterParameters;

public class SpatialIndexIdFilter {
    private SpatialIndexIdFilter() {}

    public static boolean filterVehicle(VehicleSpatialIndexId parsedId, VehicleFilterParameters filters) {
        if (filters.getCodespaces() != null && !filters.getCodespaces().contains(parsedId.getCodespace())) {
            return false;
        }

        if (filters.getSystems() != null && !filters.getSystems().contains(parsedId.getSystemId())) {
            return false;
        }

        if (filters.getOperators() != null && !filters.getOperators().contains(parsedId.getOperatorId())) {
            return false;
        }

        if (filters.getFormFactors() != null && !filters.getFormFactors().contains(parsedId.getFormFactor())) {
            return false;
        }

        if (filters.getPropulsionTypes() != null && !filters.getPropulsionTypes().contains(parsedId.getPropulsionType())) {
            return false;
        }

        if (!filters.getIncludeReserved() && parsedId.getReserved()) {
            return false;
        }

        if (!filters.getIncludeDisabled() && parsedId.getDisabled()) {
            return false;
        }

        return true;
    }

    public static boolean filterStation(StationSpatialIndexId parsedId, StationFilterParameters filters) {
        if (filters.getCodespaces() != null && !filters.getCodespaces().contains(parsedId.getCodespace())) {
            return false;
        }

        if (filters.getSystems() != null && !filters.getSystems().contains(parsedId.getSystemId())) {
            return false;
        }

        if (filters.getOperators() != null && !filters.getOperators().contains(parsedId.getOperatorId())) {
            return false;
        }

        if (filters.getAvailableFormFactors() != null && filters.getAvailableFormFactors().stream().noneMatch(formFactor -> parsedId.getAvailableFormFactors().contains(formFactor))) {
            return false;
        }

        if (filters.getAvailablePropulsionTypes() != null && filters.getAvailablePropulsionTypes().stream().noneMatch(propulsionType -> parsedId.getAvailablePropulsionTypes().contains(propulsionType))) {
            return false;
        }

        if (filters.getIncludeVirtualStations() != null && !filters.getIncludeVirtualStations() && parsedId.getVirtualStation() != null && parsedId.getVirtualStation()) {
            return false;
        }

        return true;
    }

}
