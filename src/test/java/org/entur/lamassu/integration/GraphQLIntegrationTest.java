package org.entur.lamassu.integration;

import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import org.entur.lamassu.TestLamassuApplication;
import org.entur.lamassu.config.cache.RedissonCacheConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = TestLamassuApplication.class,
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
