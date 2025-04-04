package org.entur.lamassu.graphql.subscription;

import org.entur.lamassu.cache.EntityListener;
import org.entur.lamassu.model.entities.Entity;
import org.entur.lamassu.model.subscription.UpdateType;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Base class for entity subscription handlers.
 * Implements the EntityListener interface to receive cache updates and publishes them to subscribers.
 *
 * @param <T> The entity type
 * @param <U> The update type
 */
public abstract class EntitySubscriptionHandler<T extends Entity, U> implements EntityListener<T> {

    protected final Sinks.Many<U> sink;
    private final Flux<U> publisher;
    private int listenerId;

    /**
     * Creates a new EntitySubscriptionHandler.
     */
    protected EntitySubscriptionHandler() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
        this.publisher = sink.asFlux();
    }

    /**
     * Gets the publisher for this subscription handler.
     *
     * @return The publisher
     */
    public Publisher<U> getPublisher() {
        return publisher;
    }
    
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

    /**
     * Initializes the subscription by sending initial data.
     * This should be called after the handler is registered as a listener.
     */
    public abstract void initialize();
}
