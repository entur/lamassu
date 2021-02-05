package org.entur.lamassu.controller;

import graphql.GraphqlErrorException;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.feedprovider.FeedProvider;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.Operator;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.entur.lamassu.service.VehicleQueryParameters;
import org.entur.lamassu.service.VehiclesNearbyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GraphQLQueryController implements GraphQLQueryResolver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final VehiclesNearbyService vehiclesNearbyService;
    private final FeedProviderConfig feedProviderConfig;

    @Autowired
    public GraphQLQueryController(VehiclesNearbyService vehiclesNearbyService, FeedProviderConfig feedProviderConfig) {
        this.vehiclesNearbyService = vehiclesNearbyService;
        this.feedProviderConfig = feedProviderConfig;
    }

    public Collection<Vehicle> vehicles(
            Double lat,
            Double lon,
            Double range,
            Integer count,
            List<String> operators,
            List<String> codespaces,
            List<FormFactor> formFactors,
            List<PropulsionType> propulsionTypes,
            Boolean includeReserved,
            Boolean includeDisabled
    ) {
        validateCodespaces(codespaces);
        validateOperators(operators);

        var queryParams = new VehicleQueryParameters();
        queryParams.setLat(lat);
        queryParams.setLon(lon);
        queryParams.setRange(range);
        queryParams.setCount(count);

        var filterParams = new VehicleFilterParameters();
        filterParams.setOperators(operators);
        filterParams.setCodespaces(codespaces);
        filterParams.setFormFactors(formFactors);
        filterParams.setPropulsionTypes(propulsionTypes);
        filterParams.setIncludeReserved(includeReserved);
        filterParams.setIncludeDisabled(includeDisabled);

        logger.debug("vehicles called query={} filter={}", queryParams, filterParams);

        return vehiclesNearbyService.getVehiclesNearby(queryParams, filterParams);
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

    private void validateCodespaces(List<String> codespaces) {
        if (codespaces != null) {
            var validCodespaces = codespaces();
            if(!validCodespaces.containsAll(codespaces)) {
                throw new GraphqlErrorException.Builder().message("Unknown codespace(s)").build();
            }
        }
    }

    private void validateOperators(List<String> operators) {
        if (operators != null) {
            var validOperators = operators();
            if (!validOperators.stream().map(Operator::getName).collect(Collectors.toList()).containsAll(operators)) {
                throw new GraphqlErrorException.Builder().message("Unknown operator(s)").build();
            }
        }

    }
}
