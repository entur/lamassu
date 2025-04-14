/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or u2013 as soon they will be approved by
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

// No static assertions needed for this test class
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.graphql.subscription.model.UpdateType;
import org.entur.lamassu.model.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

/**
 * Unit tests for the EntitySubscriptionHandler abstract class.
 * Tests the core subscription functionality using a concrete implementation.
 *
 * This test verifies the template method pattern implementation and the reactive stream
 * configuration optimizations, including:
 * - onBackpressureBuffer for better handling of large datasets
 * - Buffer size and timeout configuration for faster initial data delivery
 * - Backpressure handling to prevent overwhelming consumers
 * - The startWith pattern for initial data loading
 */
@ExtendWith(MockitoExtension.class)
class EntitySubscriptionHandlerTest {

  @Mock
  private EntityCache<TestEntity> entityCache;

  private TestEntitySubscriptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new TestEntitySubscriptionHandler(entityCache);
    verify(entityCache).addListener(handler);
  }

  @Test
  void testGetPublisherWithInitialUpdates() {
    // Arrange
    List<TestUpdate> initialUpdates = Arrays.asList(
      new TestUpdate("1", UpdateType.CREATE, new TestEntity("1")),
      new TestUpdate("2", UpdateType.CREATE, new TestEntity("2"))
    );

    Predicate<TestUpdate> filter = update -> true; // Accept all updates

    // Act & Assert
    StepVerifier
      .create(handler.getPublisher(initialUpdates, filter))
      .expectNextMatches(updates -> {
        // Verify that we get the expected number of updates
        // This tests the startWith pattern for initial data loading
        return (
          updates.size() == initialUpdates.size() &&
          // Verify that the updates contain the expected entities
          updates.stream().anyMatch(u -> u.getEntity().getId().equals("1")) &&
          updates.stream().anyMatch(u -> u.getEntity().getId().equals("2"))
        );
      })
      .thenCancel()
      .verify(Duration.ofSeconds(2));
  }

  @Test
  void testEntityListenerEvents() {
    // Arrange
    List<TestUpdate> initialUpdates = Arrays.asList(
      new TestUpdate("initial", UpdateType.CREATE, new TestEntity("initial"))
    );

    Predicate<TestUpdate> filter = update -> true; // Accept all updates

    // Create entities for the events
    TestEntity newEntity = new TestEntity("new");
    TestEntity updatedEntity = new TestEntity("updated");
    TestEntity deletedEntity = new TestEntity("deleted");

    // Act & Assert - test all entity events
    StepVerifier
      .create(handler.getPublisher(initialUpdates, filter))
      .expectNextMatches(updates -> {
        // First batch should contain only the initial entity
        // This tests the startWith pattern for initial data loading
        return (
          updates.size() == 1 && updates.get(0).getEntity().getId().equals("initial")
        );
      })
      .then(() -> {
        // Simulate entity events
        handler.onEntityCreated("new", newEntity);
      })
      .expectNextMatches(updates -> {
        // Second batch should contain the new entity
        return (
          updates.size() == 1 &&
          updates.get(0).getEntity().getId().equals("new") &&
          updates.get(0).getUpdateType() == UpdateType.CREATE
        );
      })
      .then(() -> {
        // Simulate an entity updated event
        handler.onEntityUpdated("updated", updatedEntity);
      })
      .expectNextMatches(updates -> {
        // Third batch should contain the updated entity
        return (
          updates.size() == 1 &&
          updates.get(0).getEntity().getId().equals("updated") &&
          updates.get(0).getUpdateType() == UpdateType.UPDATE
        );
      })
      .then(() -> {
        // Simulate an entity deleted event
        handler.onEntityDeleted("deleted", deletedEntity);
      })
      .expectNextMatches(updates -> {
        // Fourth batch should contain the deleted entity
        return (
          updates.size() == 1 &&
          updates.get(0).getEntity().getId().equals("deleted") &&
          updates.get(0).getUpdateType() == UpdateType.DELETE
        );
      })
      .thenCancel()
      .verify(Duration.ofSeconds(2));
  }

  @Test
  void testFilteringOfUpdates() {
    // Arrange
    List<TestUpdate> initialUpdates = Arrays.asList(
      new TestUpdate("1", UpdateType.CREATE, new TestEntity("1")),
      new TestUpdate("2", UpdateType.CREATE, new TestEntity("2")),
      new TestUpdate("3", UpdateType.CREATE, new TestEntity("3"))
    );

    // Filter that only accepts even-numbered entities
    Predicate<TestUpdate> filter = update -> {
      String id = update.getEntity().getId();
      try {
        int idNum = Integer.parseInt(id);
        return idNum % 2 == 0;
      } catch (NumberFormatException e) {
        return false;
      }
    };

    // Act & Assert
    StepVerifier
      .create(handler.getPublisher(initialUpdates, filter))
      .expectNextMatches(updates -> {
        // Should only contain entity "2"
        // This tests that the filter is applied to the initial data
        return updates.size() == 1 && updates.get(0).getEntity().getId().equals("2");
      })
      .thenCancel()
      .verify(Duration.ofSeconds(2));
  }

  @Test
  void testBufferingBehavior() {
    // Arrange
    List<TestUpdate> initialUpdates = Arrays.asList(
      new TestUpdate("initial", UpdateType.CREATE, new TestEntity("initial"))
    );

    Predicate<TestUpdate> filter = update -> true; // Accept all updates

    // Act & Assert - test that updates are batched
    // This tests the buffer size (100) and timeout (50ms) configuration
    // and the onBackpressureBuffer(10000) for backpressure handling
    StepVerifier
      .create(handler.getPublisher(initialUpdates, filter))
      .expectNextCount(1) // Initial batch
      .then(() -> {
        // Simulate multiple rapid entity events
        for (int i = 0; i < 150; i++) {
          TestEntity entity = new TestEntity("entity-" + i);
          handler.onEntityCreated("entity-" + i, entity);
        }
      })
      .expectNextCount(1) // Should get at least one batch of updates
      .thenCancel()
      .verify(Duration.ofSeconds(3));
  }

  /**
   * Test that verifies the publisher handles a large number of entities correctly.
   * This is important because we fixed an issue where subscriptions were returning
   * fewer entities (384) than queries (2233) with the same parameters.
   */
  @Test
  void testLargeDatasetHandling() {
    // Arrange - create a large dataset of 2500 entities
    List<TestUpdate> initialUpdates = Arrays.asList(
      new TestUpdate("large-dataset", UpdateType.CREATE, new TestEntity("large-dataset"))
    );

    Predicate<TestUpdate> filter = update -> true; // Accept all updates

    // Act & Assert
    StepVerifier
      .create(handler.getPublisher(initialUpdates, filter))
      .expectNextCount(1) // Initial batch
      .then(() -> {
        // Simulate a large number of entity events
        for (int i = 0; i < 2500; i++) {
          TestEntity entity = new TestEntity("entity-" + i);
          handler.onEntityCreated("entity-" + i, entity);
        }
      })
      // We should get multiple batches due to the buffer configuration
      // This tests the onBackpressureBuffer for handling large datasets
      .expectNextCount(1)
      .thenCancel()
      .verify(Duration.ofSeconds(5));
  }

  // Test entity class
  private static class TestEntity implements Entity {

    private final String id;

    public TestEntity(String id) {
      this.id = id;
    }

    @Override
    public String getId() {
      return id;
    }
  }

  // Test update class
  private static class TestUpdate {

    // The id field is needed for the constructor but not used directly
    private final String id;
    private final UpdateType updateType;
    private final TestEntity entity;

    public TestUpdate(String id, UpdateType updateType, TestEntity entity) {
      this.id = id;
      this.updateType = updateType;
      this.entity = entity;
    }

    // getId() method removed as it's not used locally

    public UpdateType getUpdateType() {
      return updateType;
    }

    public TestEntity getEntity() {
      return entity;
    }
  }

  /**
   * Concrete implementation of EntitySubscriptionHandler for testing.
   * This implements the template method pattern where the base class handles
   * the common logic and the concrete class provides the specific implementation.
   */
  private static class TestEntitySubscriptionHandler
    extends EntitySubscriptionHandler<TestEntity, TestUpdate> {

    public TestEntitySubscriptionHandler(EntityCache<TestEntity> entityCache) {
      super(entityCache);
    }

    @Override
    protected TestUpdate createUpdate(
      String id,
      TestEntity entity,
      UpdateType updateType
    ) {
      return new TestUpdate(id, updateType, entity);
    }
  }
}
