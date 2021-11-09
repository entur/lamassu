package org.entur.lamassu.updater;

import org.entur.lamassu.service.GeoSearchService;
import org.junit.Test;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ClusterSingletonServiceTest {

    RLock mockedLock = mock(RLock.class);
    FeedUpdater mockedFeedUpdater = mock(FeedUpdater.class);
    ListenerManager mockedListenerManager = mock(ListenerManager.class);
    GeoSearchService mockedGeoSearchService = mock(GeoSearchService.class);

    @Test
    public void testBecomeLeaderStartsScheduler() throws InterruptedException {
        baseCase();
    }

    @Test
    public void testRenewLeadershipNoop() throws InterruptedException {
        var service = baseCase();
        service.heartbeat();
        verifyNoMoreInteractions(mockedFeedUpdater);
    }

    @Test
    public void testLostLeadershopStopsScheduling() throws InterruptedException {
        var service = baseCase();
        when(mockedLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);
        service.heartbeat();
        verify(mockedFeedUpdater).stop();
    }

    @Test(expected = InterruptedException.class)
    public void testBecomeLeaderThrowsInterruptStopsSchedulingAndRethrows() throws InterruptedException {
        when(mockedLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenThrow(InterruptedException.class);
        var service = new ClusterSingletonService(mockedLock, mockedFeedUpdater, mockedListenerManager, mockedGeoSearchService);
        service.heartbeat();
        verify(mockedFeedUpdater).stop();
    }

    @Test(expected = InterruptedException.class)
    public void testRenewLeadershipThrowsInterruptStopsSchedulingAndRethrows() throws InterruptedException {
        var service = baseCase();
        when(mockedLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenThrow(InterruptedException.class);
        service.heartbeat();
        verify(mockedFeedUpdater).stop();
    }

    private ClusterSingletonService baseCase() throws InterruptedException {
        when(mockedLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        var service = new ClusterSingletonService(mockedLock, mockedFeedUpdater, mockedListenerManager, mockedGeoSearchService);
        service.heartbeat();
        verify(mockedFeedUpdater).start();
        return service;
    }
}
