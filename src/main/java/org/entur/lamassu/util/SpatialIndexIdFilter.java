package org.entur.lamassu.util;

import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.cache.VehicleTypeFilter;
import org.entur.lamassu.service.FilterParameters;
import org.entur.lamassu.service.StationFilterParameters;
import org.entur.lamassu.service.VehicleFilterParameters;

import java.util.List;

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

        if (filters.getVehicleTypesAvailable() != null && !vehicleTypesAvailablePredicate(filters.getVehicleTypesAvailable(), parsedId.getVehicleTypesAvailable())) {
            return false;
        }

        return true;
    }

    public static boolean vehicleTypesAvailablePredicate(List<VehicleTypeFilter> filter, List<VehicleTypeFilter> vehicleTypesAvailable) {
        return filter.stream().anyMatch(vehicleTypeFilter -> vehicleTypesAvailable.stream().anyMatch(vta -> {
            if (vta.getFormFactor() != null && vehicleTypeFilter.getFormFactor() != null && !vehicleTypeFilter.getFormFactor().equals(vta.getFormFactor())) {
                return false;
            }

            if (vta.getPropulsionType() != null && vehicleTypeFilter.getPropulsionType() != null && !vehicleTypeFilter.getPropulsionType().equals(vta.getPropulsionType())) {
                return false;
            }

            return true;
        }));
    }

}
