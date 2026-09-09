package org.entur.lamassu.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.DuplicateIdService;
import org.entur.lamassu.service.FeedFreshnessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSGbfs;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStationInformation;
import org.slf4j.LoggerFactory;

public class MetricsUpdaterTest {

  MetricsService mockedMetricsService = mock(MetricsService.class);
  FeedProviderConfig mockedFeedProviderConfig = mock(FeedProviderConfig.class);
  GBFSV3FeedCache mockedFeedCache = mock(GBFSV3FeedCache.class);
  DuplicateIdService mockedDuplicateIdService = mock(DuplicateIdService.class);
  MetricUpdater metricUpdater;
  FeedProvider aFeedProvider = new FeedProvider();

  @BeforeEach
  public void beforeEach() {
    aFeedProvider.setSystemId("TestSystem");
    when(mockedFeedProviderConfig.getProviders()).thenReturn(List.of(aFeedProvider));
    metricUpdater =
      new MetricUpdater(
        mockedMetricsService,
        mockedFeedProviderConfig,
        new FeedFreshnessService(mockedFeedCache, 120),
        mockedDuplicateIdService
      );
  }

  @Test
  public void testRecentlyUpdatedFileIsNotOverdue() {
    GBFSStationInformation gbfsStationInformation = new GBFSStationInformation()
      .withLastUpdated(new Date())
      .withTtl(0);
    when(mockedFeedCache.find(GBFSFeed.Name.STATION_INFORMATION, aFeedProvider))
      .thenReturn(gbfsStationInformation);

    metricUpdater.updateOutdatedFeedMetrics();

    verify(mockedMetricsService).registerOverdueFilesCount(aFeedProvider, 0);
  }

  @Test
  public void testOutdatedFileIsOverdue() {
    GBFSStationInformation gbfsStationInformation = new GBFSStationInformation()
      .withLastUpdated(new Date(0))
      .withTtl(0);
    when(mockedFeedCache.find(GBFSFeed.Name.STATION_INFORMATION, aFeedProvider))
      .thenReturn(gbfsStationInformation);

    metricUpdater.updateOutdatedFeedMetrics();

    verify(mockedMetricsService).registerOverdueFilesCount(aFeedProvider, 1);
  }

  @Test
  public void testOutdatedGbfsFileIsNotCountedAsOverdue() {
    // As long as gbfs is not updated, we don't include it in overdue count
    GBFSGbfs gbfs = new GBFSGbfs().withLastUpdated(new Date(0)).withTtl(0);
    when(mockedFeedCache.find(GBFSFeed.Name.GBFS, aFeedProvider)).thenReturn(gbfs);

    metricUpdater.updateOutdatedFeedMetrics();

    verify(mockedMetricsService).registerOverdueFilesCount(aFeedProvider, 0);
  }

  @Test
  public void testDuplicateIdCountIsRegisteredPerCodespaceAndEntity() {
    when(mockedDuplicateIdService.detect())
      .thenReturn(
        List.of(
          new DuplicateIdService.DuplicateIdReport(
            "YOS",
            DuplicateIdService.ENTITY_VEHICLE_TYPE,
            List.of(
              new DuplicateIdService.DuplicateId(
                "YOS:VehicleType:scooter",
                new TreeSet<>(Set.of("yos_oslo", "yos_bergen"))
              )
            )
          ),
          new DuplicateIdService.DuplicateIdReport(
            "YOS",
            DuplicateIdService.ENTITY_PRICING_PLAN,
            List.of()
          )
        )
      );

    metricUpdater.updateDuplicateIdMetrics();

    verify(mockedMetricsService).registerDuplicateIdCount("YOS", "vehicleType", 1);
    verify(mockedMetricsService).registerDuplicateIdCount("YOS", "pricingPlan", 0);
  }

  @Test
  public void testDetectionFailureDoesNotPropagateOrRegisterAnything() {
    when(mockedDuplicateIdService.detect())
      .thenThrow(new RuntimeException("detection failed"));

    metricUpdater.updateDuplicateIdMetrics();

    verify(mockedMetricsService, never())
      .registerDuplicateIdCount(anyString(), anyString(), anyInt());
  }

  @Test
  public void testFailedRegistrationForOneReportDoesNotPreventOthers() {
    when(mockedDuplicateIdService.detect())
      .thenReturn(
        List.of(
          new DuplicateIdService.DuplicateIdReport(
            "YOS",
            DuplicateIdService.ENTITY_VEHICLE_TYPE,
            List.of()
          ),
          new DuplicateIdService.DuplicateIdReport(
            "YOS",
            DuplicateIdService.ENTITY_PRICING_PLAN,
            List.of()
          )
        )
      );
    doThrow(new RuntimeException("registration failed"))
      .when(mockedMetricsService)
      .registerDuplicateIdCount("YOS", DuplicateIdService.ENTITY_VEHICLE_TYPE, 0);

    metricUpdater.updateDuplicateIdMetrics();

    verify(mockedMetricsService).registerDuplicateIdCount("YOS", "vehicleType", 0);
    verify(mockedMetricsService).registerDuplicateIdCount("YOS", "pricingPlan", 0);
  }

  @Test
  public void testDuplicateIdsAreLoggedOnEntryAndExitOnly() {
    FeedProvider oslo = duplicateProvider("yos_oslo");
    FeedProvider bergen = duplicateProvider("yos_bergen");
    when(mockedFeedProviderConfig.getProviders()).thenReturn(List.of(oslo, bergen));

    DuplicateIdService.DuplicateIdReport dirty = new DuplicateIdService.DuplicateIdReport(
      "YOS",
      DuplicateIdService.ENTITY_VEHICLE_TYPE,
      List.of(
        new DuplicateIdService.DuplicateId(
          "YOS:VehicleType:scooter",
          new TreeSet<>(Set.of("yos_oslo", "yos_bergen"))
        )
      )
    );
    DuplicateIdService.DuplicateIdReport clean = new DuplicateIdService.DuplicateIdReport(
      "YOS",
      DuplicateIdService.ENTITY_VEHICLE_TYPE,
      List.of()
    );

    Logger logger = (Logger) LoggerFactory.getLogger(MetricUpdater.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    Level originalLevel = logger.getLevel();
    // logback.xml sets `org` to WARN, which would swallow the INFO resolution event.
    // Force INFO here so this test asserts the logging code, not the log configuration.
    logger.setLevel(Level.INFO);
    logger.addAppender(appender);

    try {
      when(mockedDuplicateIdService.detect()).thenReturn(List.of(dirty));
      metricUpdater.updateDuplicateIdMetrics();
      metricUpdater.updateDuplicateIdMetrics();

      when(mockedDuplicateIdService.detect()).thenReturn(List.of(clean));
      metricUpdater.updateDuplicateIdMetrics();
      metricUpdater.updateDuplicateIdMetrics();
    } finally {
      logger.detachAppender(appender);
      logger.setLevel(originalLevel);
    }

    List<ILoggingEvent> events = appender.list;
    assertEquals(2, events.size());

    assertEquals(Level.WARN, events.get(0).getLevel());
    String warning = events.get(0).getFormattedMessage();
    assertTrue(warning.contains("codespace=YOS"));
    assertTrue(warning.contains("entity=vehicleType"));
    assertTrue(warning.contains("YOS:VehicleType:scooter"));
    assertTrue(warning.contains("yos_oslo(YOS:Operator:yos_oslo)"));
    assertTrue(warning.contains("yos_bergen(YOS:Operator:yos_bergen)"));
    assertTrue(
      warning.lines().count() == 1,
      "WARN message must be a single line so log aggregators show the detail in the summary row"
    );

    assertEquals(Level.INFO, events.get(1).getLevel());
    assertTrue(events.get(1).getFormattedMessage().contains("resolved"));
  }

  @Test
  public void testDuplicateIdsAreReLoggedWhenClaimingSystemsChange() {
    FeedProvider oslo = duplicateProvider("yos_oslo");
    FeedProvider bergen = duplicateProvider("yos_bergen");
    FeedProvider trondheim = duplicateProvider("yos_trondheim");
    when(mockedFeedProviderConfig.getProviders())
      .thenReturn(List.of(oslo, bergen, trondheim));

    DuplicateIdService.DuplicateIdReport claimedByOsloAndBergen =
      new DuplicateIdService.DuplicateIdReport(
        "YOS",
        DuplicateIdService.ENTITY_VEHICLE_TYPE,
        List.of(
          new DuplicateIdService.DuplicateId(
            "YOS:VehicleType:scooter",
            new TreeSet<>(Set.of("yos_oslo", "yos_bergen"))
          )
        )
      );
    // Same id, same count, but yos_bergen stopped publishing it and yos_trondheim
    // started. The old WARN would now name the wrong operator, so this must
    // re-trigger a fresh WARN even though `current.equals(previous)` would be
    // true if only ids (and not systemIds) were tracked.
    DuplicateIdService.DuplicateIdReport claimedByOsloAndTrondheim =
      new DuplicateIdService.DuplicateIdReport(
        "YOS",
        DuplicateIdService.ENTITY_VEHICLE_TYPE,
        List.of(
          new DuplicateIdService.DuplicateId(
            "YOS:VehicleType:scooter",
            new TreeSet<>(Set.of("yos_oslo", "yos_trondheim"))
          )
        )
      );

    Logger logger = (Logger) LoggerFactory.getLogger(MetricUpdater.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    Level originalLevel = logger.getLevel();
    logger.setLevel(Level.INFO);
    logger.addAppender(appender);

    try {
      when(mockedDuplicateIdService.detect()).thenReturn(List.of(claimedByOsloAndBergen));
      metricUpdater.updateDuplicateIdMetrics();

      when(mockedDuplicateIdService.detect())
        .thenReturn(List.of(claimedByOsloAndTrondheim));
      metricUpdater.updateDuplicateIdMetrics();
    } finally {
      logger.detachAppender(appender);
      logger.setLevel(originalLevel);
    }

    List<ILoggingEvent> events = appender.list;
    assertEquals(2, events.size());

    assertEquals(Level.WARN, events.get(0).getLevel());
    assertTrue(
      events.get(0).getFormattedMessage().contains("yos_bergen(YOS:Operator:yos_bergen)")
    );

    assertEquals(Level.WARN, events.get(1).getLevel());
    String secondWarning = events.get(1).getFormattedMessage();
    assertTrue(secondWarning.contains("yos_oslo(YOS:Operator:yos_oslo)"));
    assertTrue(
      secondWarning.contains("yos_trondheim(YOS:Operator:yos_trondheim)"),
      "second WARN must name the newly claiming system, not the stale one"
    );
  }

  @Test
  public void testDuplicateIdsAreLoggedWhenAProviderHasNoOperatorId() {
    FeedProvider oslo = duplicateProvider("yos_oslo");
    FeedProvider bergen = duplicateProvider("yos_bergen");
    // No operatorId set anywhere in config load path requires one - a provider can
    // legitimately have a null operatorId. That must not blow up describeDuplicates.
    bergen.setOperatorId(null);
    when(mockedFeedProviderConfig.getProviders()).thenReturn(List.of(oslo, bergen));

    DuplicateIdService.DuplicateIdReport dirty = new DuplicateIdService.DuplicateIdReport(
      "YOS",
      DuplicateIdService.ENTITY_VEHICLE_TYPE,
      List.of(
        new DuplicateIdService.DuplicateId(
          "YOS:VehicleType:scooter",
          new TreeSet<>(Set.of("yos_oslo", "yos_bergen"))
        )
      )
    );

    Logger logger = (Logger) LoggerFactory.getLogger(MetricUpdater.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    Level originalLevel = logger.getLevel();
    logger.setLevel(Level.INFO);
    logger.addAppender(appender);

    try {
      when(mockedDuplicateIdService.detect()).thenReturn(List.of(dirty));
      metricUpdater.updateDuplicateIdMetrics();
    } finally {
      logger.detachAppender(appender);
      logger.setLevel(originalLevel);
    }

    List<ILoggingEvent> events = appender.list;
    assertEquals(1, events.size());
    assertEquals(Level.WARN, events.get(0).getLevel());
    String warning = events.get(0).getFormattedMessage();
    assertTrue(warning.contains("yos_oslo(YOS:Operator:yos_oslo)"));
    assertTrue(
      warning.contains("yos_bergen(unknown)"),
      "a provider without an operator id must render as unknown, not throw"
    );
  }

  private static FeedProvider duplicateProvider(String systemId) {
    FeedProvider provider = new FeedProvider();
    provider.setSystemId(systemId);
    provider.setCodespace("YOS");
    provider.setOperatorId("YOS:Operator:" + systemId);
    return provider;
  }
}
