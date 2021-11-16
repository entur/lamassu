package org.entur.lamassu.integration;

import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphQLIntegrationTest extends AbstractIntegrationTestBase {

    @Autowired
    private GraphQLTestTemplate graphQLTestTemplate;

    @Test
    public void testVehiclesQuery() throws IOException {
        GraphQLResponse response = graphQLTestTemplate.postForResource("vehicles_query_with_disabled.graphql");
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("TST:Vehicle:1234", response.get("$.data.vehicles[0].id"));
        assertEquals("Test", response.get("$.data.vehicles[0].system.name.translation[0].value"));
    }

    @Test
    public void testVehicleQueryWithoutDisabled() throws IOException {
        GraphQLResponse response = graphQLTestTemplate.postForResource("vehicles_query_without_disabled.graphql");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.get("$.data.vehicles", List.class).isEmpty());
    }

    @Test
    public void testStationsQuery() throws IOException {
        GraphQLResponse response = graphQLTestTemplate.postForResource("stations_query.graphql");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("TST:Station:2", response.get("$.data.stations[0].id"));
        assertEquals("Cooler bikes", response.get("$.data.stations[0].name.translation[0].value"));
        assertEquals("https://rentmybikes.com", response.get("$.data.stations[0].rentalUris.web"));
    }

    @Test
    public void testStationsByIdQuery() throws IOException {
        GraphQLResponse response = graphQLTestTemplate.postForResource("stations_by_id_query.graphql");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("TST:Station:1", response.get("$.data.stationsById[0].id"));
        assertEquals("2", response.get("$.data.stationsById[0].vehicleDocksAvailable[0].count"));
    }

    @Test
    public void testGeofencingZones() throws IOException {
        GraphQLResponse response = graphQLTestTemplate.postForResource("geofencing_zones_query.graphql");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testatlantis", response.get("$.data.geofencingZones[0].systemId"));
    }
 }
