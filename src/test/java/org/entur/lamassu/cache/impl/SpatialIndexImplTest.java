package org.entur.lamassu.cache.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.BatchOptions;
import org.redisson.api.RBatch;
import org.redisson.api.RGeo;
import org.redisson.api.RGeoAsync;
import org.redisson.api.RedissonClient;
import org.redisson.api.geo.GeoEntry;
import org.redisson.client.RedisException;
import org.redisson.client.codec.Codec;
import org.redisson.misc.CompletableFutureWrapper;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class SpatialIndexImplTest {

  private static final String INDEX_NAME = "stationSpatialIndex_1";

  @Mock
  private RGeo<StationSpatialIndexId> geo;

  @Mock
  private RedissonClient redissonClient;

  @Mock
  private RBatch batch;

  @Mock
  private RGeoAsync<StationSpatialIndexId> batchGeo;

  @Mock
  private Codec codec;

  private StationSpatialIndexImpl spatialIndex() {
    return new StationSpatialIndexImpl(geo, redissonClient);
  }

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

  private static Station station(String id) {
    var station = new Station();
    station.setId(id);
    station.setLat(59.9);
    station.setLon(10.7);
    return station;
  }

  private void givenABatch() {
    when(geo.getName()).thenReturn(INDEX_NAME);
    when(geo.getCodec()).thenReturn(codec);
    when(redissonClient.createBatch(any(BatchOptions.class))).thenReturn(batch);
    when(batch.<StationSpatialIndexId>getGeo(anyString(), any(Codec.class)))
      .thenReturn(batchGeo);
  }

  private static List<String> warningsFrom(ListAppender<ILoggingEvent> appender) {
    return appender.list
      .stream()
      .filter(event -> event.getLevel() == Level.WARN)
      .map(ILoggingEvent::getFormattedMessage)
      .toList();
  }

  private ListAppender<ILoggingEvent> attachAppender() {
    var appender = new ListAppender<ILoggingEvent>();
    appender.start();
    ((Logger) LoggerFactory.getLogger(StationSpatialIndexImpl.class)).addAppender(
        appender
      );
    return appender;
  }

  private void detachAppender(ListAppender<ILoggingEvent> appender) {
    ((Logger) LoggerFactory.getLogger(StationSpatialIndexImpl.class)).detachAppender(
        appender
      );
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

    var spatialIndex = spatialIndex();

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
    var appender = attachAppender();

    try {
      when(geo.addAsync(any(GeoEntry[].class)))
        .thenReturn(new CompletableFutureWrapper<>(1L));

      var missingCoordinates = new Station();
      missingCoordinates.setId("station-2");

      spatialIndex()
        .addAll(
          Map.of(
            indexId("station-1"),
            station("station-1"),
            indexId("station-2"),
            missingCoordinates
          )
        );

      var warnings = warningsFrom(appender);

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
      detachAppender(appender);
    }
  }

  /**
   * When an entity moves to a new index key, the delete of the old member and the add of the
   * new one must reach Redis as one MULTI/EXEC. Issued separately, the entity is genuinely
   * absent between them and a concurrent geo query misses it.
   */
  @Test
  void replaceAllIssuesTheDeleteAndTheAddInOneAtomicBatch() {
    givenABatch();

    spatialIndex()
      .replaceAll(
        Set.of(indexId("old-key")),
        Map.of(indexId("new-key"), station("station-1"))
      );

    var options = ArgumentCaptor.forClass(BatchOptions.class);
    verify(redissonClient).createBatch(options.capture());
    assertEquals(
      BatchOptions.ExecutionMode.REDIS_WRITE_ATOMIC,
      options.getValue().getExecutionMode()
    );

    // The batched handle must address the same Redis key with the same codec as the live
    // index, or the batch writes somewhere else entirely.
    verify(batch).getGeo(INDEX_NAME, codec);

    var inOrder = inOrder(batchGeo, batch);
    inOrder.verify(batchGeo).removeAllAsync(Set.of(indexId("old-key")));
    inOrder.verify(batchGeo).addAsync(any(GeoEntry[].class));
    inOrder.verify(batch).execute();

    // Nothing may be issued outside the batch, or it would not be atomic.
    verify(geo, never()).removeAllAsync(anyCollection());
    verify(geo, never()).addAsync(any(GeoEntry[].class));
  }

  /**
   * The batch is only worth its MULTI/EXEC overhead when there is actually a delete to pair
   * with the add. An ordinary status change keeps the same key and removes nothing.
   */
  @Test
  void replaceAllDoesNotOpenABatchWhenThereIsNothingToRemove() {
    when(geo.addAsync(any(GeoEntry[].class)))
      .thenReturn(new CompletableFutureWrapper<>(1L));

    spatialIndex().replaceAll(Set.of(), Map.of(indexId("key"), station("station-1")));

    verify(geo).addAsync(any(GeoEntry[].class));
    verifyNoInteractions(redissonClient);
  }

  @Test
  void replaceAllDoesNotOpenABatchWhenThereIsNothingToAdd() {
    when(geo.removeAllAsync(anyCollection()))
      .thenReturn(new CompletableFutureWrapper<>(true));

    spatialIndex().replaceAll(Set.of(indexId("old-key")), Map.of());

    verify(geo).removeAllAsync(Set.of(indexId("old-key")));
    verifyNoInteractions(redissonClient);
  }

  /**
   * The coordinate check applies on the batched path too. An entity that cannot be
   * positioned leaves only the delete, which is the silent-loss shape that the warning
   * exists to make visible.
   */
  @Test
  void replaceAllWarnsAboutEntriesDroppedForMissingCoordinates() {
    var appender = attachAppender();

    try {
      when(geo.removeAllAsync(anyCollection()))
        .thenReturn(new CompletableFutureWrapper<>(true));

      var missingCoordinates = new Station();
      missingCoordinates.setId("station-1");

      spatialIndex()
        .replaceAll(
          Set.of(indexId("old-key")),
          Map.of(indexId("new-key"), missingCoordinates)
        );

      var warnings = warningsFrom(appender);

      assertEquals(1, warnings.size(), "expected exactly one warning, got: " + warnings);
      assertTrue(
        warnings.get(0).contains("station-1"),
        "warning should name the dropped station, got: " + warnings.get(0)
      );
    } finally {
      detachAppender(appender);
    }
  }

  @Test
  void replaceAllWarnsWhenTheBatchFails() {
    var appender = attachAppender();

    try {
      givenABatch();
      when(batch.execute()).thenThrow(new RedisException("redis is down"));

      spatialIndex()
        .replaceAll(
          Set.of(indexId("old-key")),
          Map.of(indexId("new-key"), station("station-1"))
        );

      assertEquals(
        1,
        warningsFrom(appender).size(),
        "expected exactly one warning, got: " + warningsFrom(appender)
      );
    } finally {
      detachAppender(appender);
    }
  }
}
