package org.entur.lamassu.leader.entityupdater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.entur.lamassu.model.entities.Entity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntityCollectorsTest {

  @Test
  void toMapWithDuplicateWarning_shouldHandleNoDuplicates() {
    List<TestEntity> entities = List.of(
      new TestEntity("1", "Entity 1"),
      new TestEntity("2", "Entity 2"),
      new TestEntity("3", "Entity 3")
    );

    Map<String, TestEntity> result = entities
      .stream()
      .collect(EntityCollectors.toMapWithDuplicateWarning(TestEntity.class));

    assertEquals(3, result.size());
    assertEquals("Entity 1", result.get("1").getName());
    assertEquals("Entity 2", result.get("2").getName());
    assertEquals("Entity 3", result.get("3").getName());
  }

  @Test
  void toMapWithDuplicateWarning_shouldKeepFirstOccurrenceOnDuplicates() {
    List<TestEntity> entities = List.of(
      new TestEntity("1", "First Entity"),
      new TestEntity("2", "Second Entity"),
      new TestEntity("1", "Duplicate Entity")
    );

    Map<String, TestEntity> result = entities
      .stream()
      .collect(EntityCollectors.toMapWithDuplicateWarning(TestEntity.class));

    assertEquals(2, result.size());
    assertEquals("First Entity", result.get("1").getName());
    assertEquals("Second Entity", result.get("2").getName());
  }

  @Test
  void toMapWithDuplicateWarning_shouldHandleEmptyStream() {
    List<TestEntity> entities = List.of();

    Map<String, TestEntity> result = entities
      .stream()
      .collect(EntityCollectors.toMapWithDuplicateWarning(TestEntity.class));

    assertTrue(result.isEmpty());
  }

  @Test
  void toMapWithDuplicateWarning_shouldHandleMultipleDuplicates() {
    List<TestEntity> entities = List.of(
      new TestEntity("1", "First"),
      new TestEntity("2", "Second"),
      new TestEntity("1", "First Duplicate"),
      new TestEntity("2", "Second Duplicate"),
      new TestEntity("1", "Another First Duplicate")
    );

    Map<String, TestEntity> result = entities
      .stream()
      .collect(EntityCollectors.toMapWithDuplicateWarning(TestEntity.class));

    assertEquals(2, result.size());
    assertEquals("First", result.get("1").getName());
    assertEquals("Second", result.get("2").getName());
  }

  private static class TestEntity implements Entity {

    private final String id;
    private final String name;

    public TestEntity(String id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }
  }
}
