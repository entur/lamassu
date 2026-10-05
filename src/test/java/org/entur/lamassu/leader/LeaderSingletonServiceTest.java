package org.entur.lamassu.leader;

import static org.mockito.Mockito.*;

import org.entur.lamassu.metrics.MetricUpdater;
import org.entur.lamassu.service.GeoSearchService;
import org.entur.lamassu.service.SpatialIndexReconciler;
import org.junit.Test;

public class LeaderSingletonServiceTest {

  FeedUpdater mockedFeedUpdater = mock(FeedUpdater.class);
  GeoSearchService mockedGeoSearchService = mock(GeoSearchService.class);
  MetricUpdater mockedMetricUpdater = mock(MetricUpdater.class);
  SpatialIndexReconciler mockedSpatialIndexReconciler = mock(
    SpatialIndexReconciler.class
  );

  @Test
  public void testStartsScheduling() {
    var service = new LeaderSingletonService(
      mockedFeedUpdater,
      mockedGeoSearchService,
      mockedMetricUpdater,
      mockedSpatialIndexReconciler
    );
    service.init();
    verify(mockedFeedUpdater).start();
  }
}
