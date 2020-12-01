package org.entur.lamassu;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.entur.lamassu.config.TestRedisConfiguration;
import org.entur.lamassu.updater.ClusterSingletonService;
import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestRedisConfiguration.class, properties = "scheduling.enabled=false")
@AutoConfigureMockMvc
public class ApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClusterSingletonService clusterSingletonService;

    private final CountDownLatch waiter = new CountDownLatch(1);

    private static final MockWebServer mockWebServer = new MockWebServer();

    @BeforeClass
    public static void setUp() throws IOException, URISyntaxException, InterruptedException {
        final Dispatcher dispatcher = new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
                switch (recordedRequest.getPath()) {
                    case "/gbfs":
                        return getMockResponse("gbfs.json");
                    case "/vehicle_types":
                        return getMockResponse("vehicle_types.json");
                    case "/system_information":
                        return getMockResponse("system_information.json");
                    case "/free_bike_status":
                        return getMockResponse("free_bike_status.json");
                    case "/system_regions":
                        return getMockResponse("system_regions.json");
                    case "/system_pricing_plans":
                        return getMockResponse("system_pricing_plans.json");
                }

                return new MockResponse().setResponseCode(404);
            }
        };

        mockWebServer.start(8888);
        mockWebServer.setDispatcher(dispatcher);
    }

    @NotNull
    private static MockResponse getMockResponse(String file) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(getFileFromResource(file));
    }

    @AfterClass
    public static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void test() throws Exception {
        clusterSingletonService.heartbeat();

        // How can we wait until we know cache has been populated?
        waiter.await(1000, TimeUnit.MILLISECONDS);

        mockMvc.perform(get("/gbfs/tst/atlantis/rover/gbfs")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.last_updated").value(1606727710));

        mockMvc.perform(get("/gbfs/tst/atlantis/rover/system_information")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.system_id").value("TST:System:Test"));

        mockMvc.perform(get("/gbfs/tst/atlantis/rover/vehicle_types")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.vehicle_types[0].vehicle_type_id").value("YBO:VehicleType:Scooter"));

        mockMvc.perform(get("/gbfs/tst/atlantis/rover/free_bike_status")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bikes[0].bike_id").value("TST:Scooter:1234"));

        mockMvc.perform(get("/gbfs/tst/atlantis/rover/system_regions")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.regions[0].region_id").value("TST:Region:Sahara"));

        mockMvc.perform(get("/gbfs/tst/atlantis/rover/system_pricing_plans")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plans[0].plan_id").value("TST:PricingPlan:Basic"));
    }

    private static String getFileFromResource(String fileName) {
        try {
            ClassLoader classLoader = ApplicationTest.class.getClassLoader();
            URL resource = classLoader.getResource(fileName);
            if (resource == null) {
                throw new IllegalArgumentException("file not found! " + fileName);
            } else {
                var file = new File(resource.toURI());
                return Files.readString(Path.of(file.getPath()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
