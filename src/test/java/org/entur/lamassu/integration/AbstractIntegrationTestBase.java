package org.entur.lamassu.integration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.entur.lamassu.TestLamassuApplication;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.leader.FeedUpdater;
import org.entur.lamassu.leader.LeaderSingletonService;
import org.entur.lamassu.leader.SubscriptionStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles({ "test", "leader" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(
  classes = TestLamassuApplication.class,
  properties = "scheduling.enabled=false",
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public abstract class AbstractIntegrationTestBase {

  @Autowired
  private LeaderSingletonService leaderSingletonService;

  @Autowired
  private FeedProviderConfig feedProviderConfig;

  @Autowired
  private FeedUpdater feedUpdater;

  private static MockWebServer mockWebServer;

  @Configuration
  static class TestConfig {

    @LocalServerPort
    private int port;

    @Bean
    public TestRestTemplate testRestTemplate() {
      return new TestRestTemplate("admin", "admin");
    }
  }

  @BeforeAll
  public static void setUp() throws IOException {
    mockWebServer = new MockWebServer();

    final Dispatcher dispatcher = new Dispatcher() {
      @NotNull
      @Override
      public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
        switch (recordedRequest.getPath()) {
          case "/testatlantis/gbfs":
            return getMockResponse("v2/gbfs.json");
          case "/testatlantis/gbfs_versions":
            return getMockResponse("v2/gbfs_versions.json");
          case "/testatlantis/vehicle_types":
            return getMockResponse("v2/vehicle_types.json");
          case "/testatlantis/station_information":
            return getMockResponse("v2/station_information.json");
          case "/testatlantis/station_status":
            return getMockResponse("v2/station_status.json");
          case "/testatlantis/system_information":
            return getMockResponse("v2/system_information.json");
          case "/testatlantis/free_bike_status":
            return getMockResponse("v2/free_bike_status.json");
          case "/testatlantis/system_regions":
            return getMockResponse("v2/system_regions.json");
          case "/testatlantis/system_pricing_plans":
            return getMockResponse("v2/system_pricing_plans.json");
          case "/testatlantis/system_hours":
            return getMockResponse("v2/system_hours.json");
          case "/testatlantis/system_calendar":
            return getMockResponse("v2/system_calendar.json");
          case "/testatlantis/system_alerts":
            return getMockResponse("v2/system_alerts.json");
          case "/testatlantis/geofencing_zones":
            return getMockResponse("v2/geofencing_zones.json");
          case "/testozon/gbfs":
            return getMockResponse("v3/gbfs.json");
          case "/testozon/gbfs_versions":
            return getMockResponse("v3/gbfs_versions.json");
          case "/testozon/vehicle_types":
            return getMockResponse("v3/vehicle_types.json");
          case "/testozon/station_information":
            return getMockResponse("v3/station_information.json");
          case "/testozon/station_status":
            return getMockResponse("v3/station_status.json");
          case "/testozon/system_information":
            return getMockResponse("v3/system_information.json");
          case "/testozon/vehicle_status":
            return getMockResponse("v3/vehicle_status.json");
          case "/testozon/system_regions":
            return getMockResponse("v3/system_regions.json");
          case "/testozon/system_pricing_plans":
            return getMockResponse("v3/system_pricing_plans.json");
          case "/testozon/system_alerts":
            return getMockResponse("v3/system_alerts.json");
          case "/testozon/geofencing_zones":
            return getMockResponse("v3/geofencing_zones.json");
        }

        return new MockResponse().setResponseCode(404);
      }
    };

    mockWebServer.setDispatcher(dispatcher);
    mockWebServer.start(8888);
  }

  @NotNull
  private static MockResponse getMockResponse(String file) {
    return new MockResponse()
      .setResponseCode(200)
      .setHeader("Content-Type", "application/json")
      .setBody(getFileFromResource(file));
  }

  @AfterAll
  public static void tearDown() throws IOException {
    mockWebServer.shutdown();
    mockWebServer = null;
  }

  @BeforeEach
  public void heartbeat() throws InterruptedException {
    Thread.sleep(1000);
    leaderSingletonService.update();
    Thread.sleep(1000);
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
