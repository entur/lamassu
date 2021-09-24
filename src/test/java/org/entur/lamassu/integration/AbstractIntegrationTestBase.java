package org.entur.lamassu.integration;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.entur.lamassu.TestLamassuApplication;
import org.entur.lamassu.updater.ClusterSingletonService;
import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = TestLamassuApplication.class,
        properties = "scheduling.enabled=false",
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public abstract class AbstractIntegrationTestBase {
    @Autowired
    private ClusterSingletonService clusterSingletonService;

    private final CountDownLatch waiter = new CountDownLatch(1);

    private static MockWebServer mockWebServer;

    @BeforeClass
    public static void setUp() throws IOException {

        mockWebServer = new MockWebServer();

        final Dispatcher dispatcher = new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
                switch (recordedRequest.getPath()) {
                    case "/gbfs":
                        return getMockResponse("gbfs.json");
                    case "/gbfs_versions":
                        return getMockResponse("gbfs_versions.json");
                    case "/vehicle_types":
                        return getMockResponse("vehicle_types.json");
                    case "/station_information":
                        return getMockResponse("station_information.json");
                    case "/station_status":
                        return getMockResponse("station_status.json");
                    case "/system_information":
                        return getMockResponse("system_information.json");
                    case "/free_bike_status":
                        return getMockResponse("free_bike_status.json");
                    case "/system_regions":
                        return getMockResponse("system_regions.json");
                    case "/system_pricing_plans":
                        return getMockResponse("system_pricing_plans.json");
                    case "/system_hours":
                        return getMockResponse("system_hours.json");
                    case "/system_calendar":
                        return getMockResponse("system_calendar.json");
                    case "/system_alerts":
                        return getMockResponse("system_alerts.json");
                    case "/geofencing_zones":
                        return getMockResponse("geofencing_zones.json");
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
        mockWebServer = null;
    }

    @Before
    public void heartbeat() throws InterruptedException {
        if (!clusterSingletonService.isLeader()) {
            clusterSingletonService.heartbeat();
            clusterSingletonService.update();
        }
    }

    private static String getFileFromResource(String fileName) {
        try {
            ClassLoader classLoader = GBFSRestIntegrationTest.class.getClassLoader();
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
