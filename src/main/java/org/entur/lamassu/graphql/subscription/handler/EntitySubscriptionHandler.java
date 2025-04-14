/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.graphql.subscription.handler;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.EntityListener;
import org.entur.lamassu.model.entities.Entity;
import org.entur.lamassu.graphql.subscription.model.UpdateType;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger logger = LoggerFactory.getLogger(EntitySubscriptionHandler.class);

  // Using onBackpressureBuffer for better handling of large initial datasets
  protected final Sinks.Many<U> sink = Sinks.many().multicast().onBackpressureBuffer();

  protected EntitySubscriptionHandler(EntityCache<T> entityCache) {
    entityCache.addListener(this);
  }

  /**
   * Gets the publisher for this subscription handler.
   *
   * @return The publisher with initial data
   */
  protected Publisher<List<U>> getPublisher(List<U> initialUpdates, Predicate<U> filter) {
    logger.trace("Preparing to send {} initial updates", initialUpdates.size());

    return sink
      .asFlux()
      .startWith(initialUpdates)
      .filter(filter)
      .bufferTimeout(100, Duration.ofMillis(50))
      .onBackpressureBuffer(10000); // Buffer up to 10000 items before applying backpressure
  }

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
    sink.tryEmitNext(createUpdate(id, entity, UpdateType.CREATE));
  }

  @Override
  public void onEntityUpdated(String id, T entity) {
    sink.tryEmitNext(createUpdate(id, entity, UpdateType.UPDATE));
  }

  @Override
  public void onEntityDeleted(String id, T entity) {
    sink.tryEmitNext(createUpdate(id, entity, UpdateType.DELETE));
  }
}
