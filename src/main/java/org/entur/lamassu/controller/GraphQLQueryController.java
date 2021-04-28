package org.entur.lamassu.controller;

import graphql.GraphqlErrorException;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.entur.lamassu.cache.StationCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.discovery.FeedProvider;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Vehicle;
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
    private final FeedProviderConfig feedProviderConfig;
    private final StationCache stationCache;

    @Autowired
    public GraphQLQueryController(GeoSearchService geoSearchService, FeedProviderConfig feedProviderConfig, StationCache stationCache) {
        this.geoSearchService = geoSearchService;
        this.feedProviderConfig = feedProviderConfig;
        this.stationCache = stationCache;
    }

    public Collection<String> getCodespaces() {
        return feedProviderConfig.getProviders().stream().map(FeedProvider::getCodespace).collect(Collectors.toSet());
    }

    public Collection<Vehicle> getVehicles(
            Double lat,
            Double lon,
            Double range,
            Integer count,
            List<String> codespaces,
            List<FormFactor> formFactors,
            List<PropulsionType> propulsionTypes,
            boolean includeReserved,
            boolean includeDisabled
    ) {
        validateCodespaces(codespaces);

        var queryParams = new RangeQueryParameters();
        queryParams.setLat(lat);
        queryParams.setLon(lon);
        queryParams.setRange(range);
        queryParams.setCount(count);

        var filterParams = new VehicleFilterParameters();
        filterParams.setCodespaces(codespaces);
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
            List<String> codespaces
    ) {
        validateCodespaces(codespaces);

        var queryParams = new RangeQueryParameters();
        queryParams.setLat(lat);
        queryParams.setLon(lon);
        queryParams.setRange(range);
        queryParams.setCount(count);

        var filterParams = new FilterParameters();
        filterParams.setCodespaces(codespaces);

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
            if(!validCodespaces.containsAll(codespaces)) {
                throw new GraphqlErrorException.Builder().message("Unknown codespace(s)").build();
            }
        }
    }
}
