package org.entur.lamassu.controller;

import graphql.GraphqlErrorException;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.entur.lamassu.cache.StationCache;
import org.entur.lamassu.model.entities.Operator;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.discovery.FeedProvider;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.FilterParameters;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.GeoSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GraphQLQueryController implements GraphQLQueryResolver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GeoSearchService geoSearchService;
    private final FeedProviderService feedProviderService;
    private final StationCache stationCache;

    @Autowired
    public GraphQLQueryController(GeoSearchService geoSearchService, FeedProviderService feedProviderService, StationCache stationCache) {
        this.geoSearchService = geoSearchService;
        this.feedProviderService = feedProviderService;
        this.stationCache = stationCache;
    }

    public Collection<String> getCodespaces() {
        return feedProviderService.getFeedProviders().stream().map(FeedProvider::getCodespace).collect(Collectors.toSet());
    }

    public Collection<String> getSystems() {
        return feedProviderService.getFeedProviders().stream().map(FeedProvider::getSystemId).collect(Collectors.toSet());
    }

    public Collection<Operator> getOperators() {
        return feedProviderService.getOperators();
    }

    public Collection<Vehicle> getVehicles(
            Double lat,
            Double lon,
            Double range,
            Integer count,
            List<String> codespaces,
            List<String> systems,
            List<String> operators,
            List<FormFactor> formFactors,
            List<PropulsionType> propulsionTypes,
            boolean includeReserved,
            boolean includeDisabled
    ) {
        validateCodespaces(codespaces);
        validateSystems(systems);
        validateOperators(operators);

        var queryParams = new RangeQueryParameters();
        queryParams.setLat(lat);
        queryParams.setLon(lon);
        queryParams.setRange(range);
        queryParams.setCount(count);

        var filterParams = new VehicleFilterParameters();
        filterParams.setCodespaces(codespaces);
        filterParams.setSystems(systems);
        filterParams.setOperators(operators);
        filterParams.setFormFactors(formFactors);
        filterParams.setPropulsionTypes(propulsionTypes);
        filterParams.setIncludeReserved(includeReserved);
        filterParams.setIncludeDisabled(includeDisabled);

        logger.debug("getVehicles called query={} filter={}", queryParams, filterParams);

        return geoSearchService.getVehiclesNearby(queryParams, filterParams);
    }

    public Collection<Station> getStations(
            Double lat,
            Double lon,
            Double range,
            Integer count,
            List<String> codespaces,
            List<String> systems,
            List<String> operators
    ) {
        validateCodespaces(codespaces);
        validateSystems(systems);
        validateOperators(operators);

        var queryParams = new RangeQueryParameters();
        queryParams.setLat(lat);
        queryParams.setLon(lon);
        queryParams.setRange(range);
        queryParams.setCount(count);

        var filterParams = new FilterParameters();
        filterParams.setCodespaces(codespaces);
        filterParams.setSystems(systems);
        filterParams.setOperators(operators);

        logger.debug("getStations called query={} filter={}", queryParams, filterParams);

        return geoSearchService.getStationsNearby(queryParams, filterParams);
    }

    public Collection<Station> getStationsById(
        List<String> ids
    ) {
        logger.debug("getStationsByIds called ids={}", ids);
        return stationCache.getAll(new HashSet<>(ids));
    }

    private void validateCodespaces(List<String> codespaces) {
        if (codespaces != null) {
            var validCodespaces = getCodespaces();
            validate(codespaces, validCodespaces, "Unknown codespace(s)");
        }
    }

    private void validateSystems(List<String> systems) {
        if (systems != null) {
            var validSystems = getSystems();
            validate(systems, validSystems, "Unknown system(s)");
        }
    }

    private void validateOperators(List<String> operators) {
        if (operators != null) {
            var validOperators = getOperators().stream().map(Operator::getId).collect(Collectors.toList());
            validate(operators, validOperators, "Unknown operator(s)");
        }
    }

    private void validate(Collection<String> input, Collection<String> valid, String message) {
        if (!valid.containsAll(input)) {
            throw new GraphqlErrorException.Builder().message(message).build();
        }
    }
}
