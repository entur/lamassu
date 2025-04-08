package org.entur.lamassu.graphql.subscription;

import java.time.Duration;
import java.util.List;
import org.entur.lamassu.cache.EntityListener;
import org.entur.lamassu.model.entities.Entity;
import org.entur.lamassu.model.subscription.UpdateType;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Sinks;

/**
 * Base class for entity subscription handlers.
 * Implements the EntityListener interface to receive cache updates and publishes them to subscribers.
 *
 * @param <T> The entity type
 * @param <U> The update type
 */
public abstract class EntitySubscriptionHandler<T extends Entity, U>
  implements EntityListener<T> {

  private final Sinks.Many<U> sink = Sinks.many().multicast().directBestEffort();
  private int listenerId;

  /**
   * Gets the publisher for this subscription handler.
   * Initializes the publisher with initial data from getInitialUpdates().
   *
   * @return The publisher with initial data
   */
  public Publisher<List<U>> getPublisher() {
    // Get initial updates from concrete implementation
    List<U> initialUpdates = getInitialUpdates();

    return sink
      .asFlux()
      .startWith(initialUpdates)
      //.filter() <-- is this the correct place to actually add a filter?
      .bufferTimeout(500, Duration.ofMillis(250))
      .onBackpressureDrop();
  }

  /**
   * Gets the initial updates for this subscription.
   * This should be implemented by concrete classes to provide initial data.
   *
   * @return A list of initial updates
   */
  protected abstract List<U> getInitialUpdates();

  /**
   * Sets the listener ID for this subscription handler.
   * This is used for cleanup when the subscription ends.
   *
   * @param listenerId The listener ID
   */
  public void setListenerId(int listenerId) {
    this.listenerId = listenerId;
  }

  /**
   * Gets the listener ID for this subscription handler.
   *
   * @return The listener ID
   */
  public int getListenerId() {
    return listenerId;
  }

  /**
   * Checks if an entity matches the filter criteria for this subscription.
   *
   * @param entity The entity to check
   * @return true if the entity matches the filter criteria, false otherwise
   */
  protected abstract boolean matchesFilter(T entity);

  /**
   * Creates an update object for the given entity and update type.
   *
   * @param id The entity ID
   * @param entity The entity
   * @param updateType The update type
   * @return The update object
   */
  protected abstract U createUpdate(String id, T entity, UpdateType updateType);

  @Override
  public void onEntityCreated(String id, T entity) {
    if (matchesFilter(entity)) {
      sink.tryEmitNext(createUpdate(id, entity, UpdateType.CREATE));
    }
  }

  @Override
  public void onEntityUpdated(String id, T entity) {
    if (matchesFilter(entity)) {
      sink.tryEmitNext(createUpdate(id, entity, UpdateType.UPDATE));
    }
  }

  @Override
  public void onEntityDeleted(String id, T entity) {
    // For DELETE events, we have the entity from the Redisson listener
    // We can filter it just like CREATE and UPDATE events
    if (entity != null && matchesFilter(entity)) {
      sink.tryEmitNext(createUpdate(id, entity, UpdateType.DELETE));
    }
  }
}
