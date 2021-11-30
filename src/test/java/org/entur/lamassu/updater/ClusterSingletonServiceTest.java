package org.entur.lamassu.updater;

import org.entur.lamassu.service.GeoSearchService;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ClusterSingletonServiceTest {

    FeedUpdater mockedFeedUpdater = mock(FeedUpdater.class);
    ListenerManager mockedListenerManager = mock(ListenerManager.class);
    GeoSearchService mockedGeoSearchService = mock(GeoSearchService.class);

    @Test
    public void testStartsScheduling() {
        var service = new ClusterSingletonService(mockedFeedUpdater, mockedListenerManager, mockedGeoSearchService);
        service.init();
        verify(mockedFeedUpdater).start();
    }
}
