package org.entur.lamassu.model.subscription;

/**
 * Enum representing the type of update that occurred to an entity.
 */
public enum UpdateType {
  /**
   * A new entity was created
   */
  CREATE,

  /**
   * An existing entity was updated
   */
  UPDATE,

  /**
   * An entity was deleted
   */
  DELETE,
}
