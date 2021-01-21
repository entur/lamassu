package org.entur.lamassu.controller;

import graphql.kickstart.tools.GraphQLQueryResolver;
import org.entur.lamassu.model.Vehicle;
import org.entur.lamassu.service.VehiclesNearbyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class GraphQLQueryController implements GraphQLQueryResolver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    VehiclesNearbyService vehiclesNearbyService;

    public Collection<Vehicle> vehicles(
            Double lat,
            Double lon,
            Double range,
            Integer count
    ) {
        logger.info("vehicles called lat={} lon={} count={} range={}", lat, lon, count, range);
        return vehiclesNearbyService.getVehiclesNearby(
                lon,
                lat,
                range,
                count
        );
    }
}
