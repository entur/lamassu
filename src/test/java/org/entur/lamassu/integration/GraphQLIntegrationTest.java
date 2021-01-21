package org.entur.lamassu.integration;

import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphQLIntegrationTest extends AbstractIntegrationTestBase {
    @Autowired
    private GraphQLTestTemplate graphQLTestTemplate;


    @Test
    public void testVehiclesQuery() throws IOException {
        GraphQLResponse response = graphQLTestTemplate.postForResource("vehicles_query.graphql");
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("TST:Scooter:1234", response.get("$.data.vehicles[0].id"));
    }
}
