package org.entur.lamassu.controller;

import graphql.kickstart.tools.GraphQLQueryResolver;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.FeedProvider;
import org.entur.lamassu.model.Operator;
import org.entur.lamassu.model.Vehicle;
import org.entur.lamassu.service.VehiclesNearbyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class GraphQLQueryController implements GraphQLQueryResolver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    VehiclesNearbyService vehiclesNearbyService;

    @Autowired
    FeedProviderConfig feedProviderConfig;

    public Collection<Vehicle> vehicles(
            Double lat,
            Double lon,
            Double range,
            Integer count
    ) {
        logger.info("vehicles called lat={} lon={} range={} count={}", lat, lon, range, count);
        return vehiclesNearbyService.getVehiclesNearby(
                lon,
                lat,
                range,
                count
        );
    }

    public Collection<String> codespaces() {
        return feedProviderConfig.getProviders().stream().map(FeedProvider::getCodespace).collect(Collectors.toSet());
    }

    public Collection<Operator> operators() {
        return feedProviderConfig.getProviders().stream().map(this::mapToOperator).collect(Collectors.toList());
    }

    private Operator mapToOperator(FeedProvider feedProvider) {
        var operator = new Operator();
        operator.setName(feedProvider.getName());
        operator.setCodespace(feedProvider.getCodespace());
        return operator;
    }
}
