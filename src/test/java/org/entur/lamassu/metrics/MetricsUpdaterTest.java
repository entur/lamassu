package org.entur.lamassu.metrics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
