package org.entur.lamassu.graphql.subscription;

import java.util.Collection;

import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.subscription.UpdateType;
import org.entur.lamassu.model.subscription.VehicleUpdate;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.GeoSearchService;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.VehicleFilterParameters;

/**
 * Subscription handler for vehicle updates.
 * Filters vehicle updates based on subscription parameters.
 */
public class VehicleSubscriptionHandler extends EntitySubscriptionHandler<Vehicle, VehicleUpdate> {

    private final VehicleFilterParameters filterParams;
    private final GeoSearchService geoSearchService;
    private final BoundingBoxQueryParameters boundingBoxParams;
    private final RangeQueryParameters rangeParams;

    /**
     * Creates a new VehicleSubscriptionHandler with bounding box parameters.
     *
     * @param filterParams The filter parameters
     * @param geoSearchService The geo search service
     * @param boundingBoxParams The bounding box parameters
     */
    public VehicleSubscriptionHandler(
            VehicleFilterParameters filterParams,
            GeoSearchService geoSearchService,
            BoundingBoxQueryParameters boundingBoxParams) {
        super();
        this.filterParams = filterParams;
        this.geoSearchService = geoSearchService;
        this.boundingBoxParams = boundingBoxParams;
        this.rangeParams = null;
    }

    /**
     * Creates a new VehicleSubscriptionHandler with range parameters.
     *
     * @param filterParams The filter parameters
     * @param geoSearchService The geo search service
     * @param rangeParams The range parameters
     */
    public VehicleSubscriptionHandler(
            VehicleFilterParameters filterParams,
            GeoSearchService geoSearchService,
            RangeQueryParameters rangeParams) {
        super();
        this.filterParams = filterParams;
        this.geoSearchService = geoSearchService;
        this.boundingBoxParams = null;
        this.rangeParams = rangeParams;
    }

    @Override
    protected boolean matchesFilter(Vehicle vehicle) {
        // Check if vehicle matches filter parameters
        if (filterParams.getCodespaces() != null && !filterParams.getCodespaces().isEmpty() 
                && !filterParams.getCodespaces().contains(vehicle.getSystem().getId().split(":")[0])) {
            return false;
        }

        if (filterParams.getSystems() != null && !filterParams.getSystems().isEmpty() 
                && !filterParams.getSystems().contains(vehicle.getSystem().getId())) {
            return false;
        }

        if (filterParams.getOperators() != null && !filterParams.getOperators().isEmpty() 
                && !filterParams.getOperators().contains(vehicle.getSystem().getOperator().getId())) {
            return false;
        }

        if (filterParams.getFormFactors() != null && !filterParams.getFormFactors().isEmpty() 
                && !filterParams.getFormFactors().contains(vehicle.getVehicleType().getFormFactor())) {
            return false;
        }

        if (filterParams.getPropulsionTypes() != null && !filterParams.getPropulsionTypes().isEmpty() 
                && !filterParams.getPropulsionTypes().contains(vehicle.getVehicleType().getPropulsionType())) {
            return false;
        }

        if (!filterParams.getIncludeReserved() && vehicle.getReserved()) {
            return false;
        }

        if (!filterParams.getIncludeDisabled() && vehicle.getDisabled()) {
            return false;
        }

        // Check if vehicle is within geographic bounds
        if (rangeParams != null) {
            double distance = calculateDistance(
                    rangeParams.getLat(), rangeParams.getLon(),
                    vehicle.getLat(), vehicle.getLon());
            return distance <= rangeParams.getRange();
        } else if (boundingBoxParams != null) {
            return vehicle.getLat() >= boundingBoxParams.getMinimumLatitude() &&
                   vehicle.getLat() <= boundingBoxParams.getMaximumLatitude() &&
                   vehicle.getLon() >= boundingBoxParams.getMinimumLongitude() &&
                   vehicle.getLon() <= boundingBoxParams.getMaximumLongitude();
        }

        return true;
    }

    @Override
    protected VehicleUpdate createUpdate(String id, Vehicle vehicle, UpdateType updateType) {
        return new VehicleUpdate(id, updateType, vehicle);
    }

    @Override
    public void initialize() {
        // Send initial data matching the subscription
        Collection<Vehicle> initialVehicles;
        if (rangeParams != null) {
            initialVehicles = geoSearchService.getVehiclesWithinRange(rangeParams, filterParams);
        } else {
            initialVehicles = geoSearchService.getVehiclesInBoundingBox(boundingBoxParams, filterParams);
        }

        for (Vehicle vehicle : initialVehicles) {
            sink.tryEmitNext(new VehicleUpdate(vehicle.getId(), UpdateType.CREATE, vehicle));
        }
    }

    /**
     * Calculates the distance between two points using the Haversine formula.
     *
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return The distance in meters
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
