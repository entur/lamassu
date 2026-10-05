package org.entur.lamassu.cache.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.entur.lamassu.model.entities.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RMapCache;
import org.redisson.misc.CompletableFutureWrapper;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class EntityCacheImplTest {

  @Mock
  private RMapCache<String, Station> cache;

  /**
   * removeAll must not return before Redis has acknowledged the removal, so that a caller
   * which removes and then writes cannot have the pending removal overtake the write and
   * delete an entity that was just re-created.
   */
  @Test
  void removeAllWaitsForTheRemovalToComplete() throws Exception {
    var pendingRemoval = new CompletableFuture<Long>();
    var removalCompleted = new AtomicBoolean(false);

    when(cache.fastRemoveAsync(any(String[].class)))
      .thenReturn(new CompletableFutureWrapper<>(pendingRemoval));

    var completer = new Thread(() -> {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
      removalCompleted.set(true);
      pendingRemoval.complete(1L);
    });

    var entityCache = new StationCacheImpl(cache);

    completer.start();
    entityCache.removeAll(Set.of("station-1"));
    // Sample the flag at the moment removeAll returns, before joining the completer.
    var completedBeforeReturn = removalCompleted.get();
    completer.join();

    assertTrue(
      completedBeforeReturn,
      "removeAll returned before the removal had completed"
    );
  }

  /**
   * A removal that fails used to be discarded with nothing logged, leaving the entity in the
   * cache and served in the feeds after it should have been deleted. That silence is what
   * made the equivalent spatial index defect expensive to diagnose.
   */
  @Test
  void removeAllWarnsWhenTheRemovalFails() {
    var appender = new ListAppender<ILoggingEvent>();
    appender.start();
    var logger = (Logger) LoggerFactory.getLogger(StationCacheImpl.class);
    logger.addAppender(appender);

    try {
      var failedRemoval = new CompletableFuture<Long>();
      failedRemoval.completeExceptionally(new RuntimeException("redis is down"));

      when(cache.fastRemoveAsync(any(String[].class)))
        .thenReturn(new CompletableFutureWrapper<>(failedRemoval));

      new StationCacheImpl(cache).removeAll(Set.of("station-1"));

      var warnings = appender.list
        .stream()
        .filter(event -> event.getLevel() == Level.WARN)
        .map(ILoggingEvent::getFormattedMessage)
        .toList();

      assertEquals(1, warnings.size(), "expected exactly one warning, got: " + warnings);
    } finally {
      logger.detachAppender(appender);
    }
  }
}
