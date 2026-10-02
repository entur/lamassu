package org.entur.lamassu.cache.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.model.entities.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RGeo;
import org.redisson.api.geo.GeoEntry;
import org.redisson.misc.CompletableFutureWrapper;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class SpatialIndexImplTest {

  @Mock
  private RGeo<StationSpatialIndexId> geo;

  private static StationSpatialIndexId indexId(String id) {
    var indexId = new StationSpatialIndexId();
    indexId.setId(id);
    indexId.setCodespace("TST");
    indexId.setSystemId("test-system");
    indexId.setOperatorId("test-operator");
    indexId.setAvailableFormFactors(List.of());
    indexId.setAvailablePropulsionTypes(List.of());
    return indexId;
  }

  /**
   * removeAll must not return before Redis has acknowledged the delete. If it does, a
   * subsequent addAll can have its GEOADD overtake the pending ZREM, and the entry that
   * was just written is deleted instead.
   */
  @Test
  void removeAllWaitsForTheDeleteToComplete() throws Exception {
    var pendingDelete = new CompletableFuture<Boolean>();
    var deleteCompleted = new AtomicBoolean(false);

    when(geo.removeAllAsync(anyCollection()))
      .thenReturn(new CompletableFutureWrapper<>(pendingDelete));

    var completer = new Thread(() -> {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
      deleteCompleted.set(true);
      pendingDelete.complete(true);
    });

    var spatialIndex = new StationSpatialIndexImpl(geo);

    completer.start();
    spatialIndex.removeAll(Set.of(indexId("station-1")));
    // Sample the flag at the moment removeAll returns, before joining the completer.
    var completedBeforeReturn = deleteCompleted.get();
    completer.join();

    assertTrue(
      completedBeforeReturn,
      "removeAll returned before the delete had completed"
    );
  }

  /**
   * An entity whose coordinates are null is filtered out of the GEOADD. Since removeAll has
   * usually already deleted its previous entry, dropping it silently loses it from the index
   * with nothing logged.
   */
  @Test
  void addAllWarnsAboutEntriesDroppedForMissingCoordinates() {
    var appender = new ListAppender<ILoggingEvent>();
    appender.start();
    var logger = (Logger) LoggerFactory.getLogger(StationSpatialIndexImpl.class);
    logger.addAppender(appender);

    try {
      when(geo.addAsync(any(GeoEntry[].class)))
        .thenReturn(new CompletableFutureWrapper<>(1L));

      var positioned = new Station();
      positioned.setId("station-1");
      positioned.setLat(59.9);
      positioned.setLon(10.7);

      var missingCoordinates = new Station();
      missingCoordinates.setId("station-2");

      new StationSpatialIndexImpl(geo)
        .addAll(
          Map.of(
            indexId("station-1"),
            positioned,
            indexId("station-2"),
            missingCoordinates
          )
        );

      var warnings = appender.list
        .stream()
        .filter(event -> event.getLevel() == Level.WARN)
        .map(ILoggingEvent::getFormattedMessage)
        .toList();

      assertEquals(1, warnings.size(), "expected exactly one warning, got: " + warnings);
      assertTrue(
        warnings.get(0).contains("station-2"),
        "warning should name the dropped station, got: " + warnings.get(0)
      );
      assertFalse(
        warnings.get(0).contains("station-1"),
        "warning should not name the station that was added, got: " + warnings.get(0)
      );
    } finally {
      logger.detachAppender(appender);
    }
  }
}
