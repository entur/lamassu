package org.entur.lamassu.leader;

import static org.mockito.Mockito.*;

import org.entur.lamassu.service.GeoSearchService;
import org.junit.Test;

public class LeaderSingletonServiceTest {

  FeedUpdater mockedFeedUpdater = mock(FeedUpdater.class);
  GeoSearchService mockedGeoSearchService = mock(GeoSearchService.class);

  @Test
  public void testStartsScheduling() {
    var service = new LeaderSingletonService(mockedFeedUpdater, mockedGeoSearchService);
    service.init();
    verify(mockedFeedUpdater).start();
  }
}
