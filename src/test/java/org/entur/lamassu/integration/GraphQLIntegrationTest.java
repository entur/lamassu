package org.entur.lamassu.integration;

import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;

import org.entur.lamassu.config.TestRedisConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = TestRedisConfiguration.class,
        properties = "scheduling.enabled=false",
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class GraphQLIntegrationTest extends AbstractIntegrationTestBase {
    @Autowired
    private GraphQLTestTemplate graphQLTestTemplate;


    @Test
    public void testVehiclesQuery() throws IOException {
        GraphQLResponse response = graphQLTestTemplate.postForResource("vehicles_query.graphql");
        assertNotNull(response);
        assertEquals("TST:Scooter:1234", response.get("$.data.vehicles[0].id"));

    }


//    @Test
//    public void testGraphQLVehiclesQuery() throws Exception {
//        mockMvc.perform(post("/graphql")
//                .content(getFileFromResource("vehicles_query.graphql"))
//                .contentType("application/json"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.vehicles[0].id").value("TST:Scooter:1234"));
//    }
}
