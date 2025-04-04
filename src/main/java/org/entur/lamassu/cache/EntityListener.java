package org.entur.lamassu.cache;

import org.entur.lamassu.model.entities.Entity;

/**
 * Interface for listening to changes in entity caches.
 * This allows components to react to entity creations, updates, and deletions.
 */
public interface EntityListener<T extends Entity> {
    /**
     * Called when a new entity is created in the cache.
     *
     * @param id The ID of the created entity
     * @param entity The created entity
     */
    void onEntityCreated(String id, T entity);

    /**
     * Called when an existing entity is updated in the cache.
     *
     * @param id The ID of the updated entity
     * @param entity The updated entity
     */
    void onEntityUpdated(String id, T entity);

    /**
     * Called when an entity is deleted from the cache.
     *
     * @param id The ID of the deleted entity
     * @param entity The deleted entity (may be null for some cache implementations)
     */
    void onEntityDeleted(String id, T entity);
}
