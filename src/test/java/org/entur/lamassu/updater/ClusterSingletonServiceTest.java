package org.entur.lamassu.updater;

import org.junit.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ClusterSingletonServiceTest {

    RedissonClient mockedRedisson = mock(RedissonClient.class);
    RLock mockedLock = mock(RLock.class);
    FeedUpdateScheduler mockedFeedUpdateScheduler = mock(FeedUpdateScheduler.class);

    @Test
    public void testBecomeLeaderStartsScheduler() throws InterruptedException {
        baseCase();
    }

    @Test
    public void testRenewLeadershipNoop() throws InterruptedException {
        var service = baseCase();
        service.heartbeat();
        verifyNoMoreInteractions(mockedFeedUpdateScheduler);
    }

    @Test
    public void testLostLeadershopStopsScheduling() throws InterruptedException {
        var service = baseCase();
        when(mockedLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);
        service.heartbeat();
        verify(mockedFeedUpdateScheduler).stop();
    }

    @Test(expected = InterruptedException.class)
    public void testBecomeLeaderThrowsInterruptStopsSchedulingAndRethrows() throws InterruptedException {
        when(mockedRedisson.getLock("leader")).thenReturn(mockedLock);
        when(mockedLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenThrow(InterruptedException.class);
        var service = new ClusterSingletonService(mockedRedisson, mockedFeedUpdateScheduler);
        service.heartbeat();
        verify(mockedFeedUpdateScheduler).stop();
    }

    @Test(expected = InterruptedException.class)
    public void testRenewLeadershipThrowsInterruptStopsSchedulingAndRethrows() throws InterruptedException {
        var service = baseCase();
        when(mockedLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenThrow(InterruptedException.class);
        service.heartbeat();
        verify(mockedFeedUpdateScheduler).stop();
    }

    private ClusterSingletonService baseCase() throws InterruptedException {
        when(mockedRedisson.getLock("leader")).thenReturn(mockedLock);
        when(mockedLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        var service = new ClusterSingletonService(mockedRedisson, mockedFeedUpdateScheduler);
        service.heartbeat();
        verify(mockedFeedUpdateScheduler).start();
        return service;
    }
}
