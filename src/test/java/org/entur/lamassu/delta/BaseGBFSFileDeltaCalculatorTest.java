package org.entur.lamassu.delta;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class BaseGBFSFileDeltaCalculatorTest {

  // Simple test entity class
  private static class TestEntity {

    private String id;
    private String value;
    private Integer number;

    public TestEntity() {}

    public TestEntity(String id, String value, Integer number) {
      this.id = id;
      this.value = value;
      this.number = number;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public Integer getNumber() {
      return number;
    }

    public void setNumber(Integer number) {
      this.number = number;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TestEntity that = (TestEntity) o;
      return (
        java.util.Objects.equals(id, that.id) &&
        java.util.Objects.equals(value, that.value) &&
        java.util.Objects.equals(number, that.number)
      );
    }
  }

  // Simple test feed class
  private static class TestFeed {

    private final long lastUpdated;
    private final long ttl;
    private final List<TestEntity> entities;

    public TestFeed(long lastUpdated, long ttl, List<TestEntity> entities) {
      this.lastUpdated = lastUpdated;
      this.ttl = ttl;
      this.entities = entities;
    }

    public long getLastUpdated() {
      return lastUpdated;
    }

    public long getTtl() {
      return ttl;
    }

    public List<TestEntity> getEntities() {
      return entities;
    }
  }

  // Test implementation of BaseGBFSFileDeltaCalculator
  private static class TestDeltaCalculator
    extends BaseGBFSFileDeltaCalculator<TestFeed, TestEntity> {

    @Override
    protected List<TestEntity> getEntities(TestFeed instance) {
      return instance.getEntities();
    }

    @Override
    protected String getEntityId(TestEntity entity) {
      return entity.getId();
    }

    @Override
    protected TestEntity createEntity() {
      return new TestEntity();
    }

    @Override
    protected long getLastUpdated(TestFeed instance) {
      return instance.getLastUpdated();
    }

    @Override
    protected String getFileName() {
      return "test_feed";
    }
  }

  private final TestDeltaCalculator calculator = new TestDeltaCalculator();

  @Test
  void shouldHandleNullBase() {
    TestFeed compare = new TestFeed(
      1000L,
      60L,
      List.of(new TestEntity("1", "value", 42))
    );

    GBFSFileDelta<TestEntity> delta = calculator.calculateDelta(null, compare);

    assertNotNull(delta);
    assertEquals(1, delta.entityDelta().size());
    assertEquals(DeltaType.CREATE, delta.entityDelta().get(0).type());
  }

  @Test
  void shouldDetectCreatedEntities() {
    TestFeed base = new TestFeed(1000L, 60L, List.of(new TestEntity("1", "value", 42)));

    TestFeed compare = new TestFeed(
      2000L,
      60L,
      List.of(new TestEntity("1", "value", 42), new TestEntity("2", "new", 100))
    );

    GBFSFileDelta<TestEntity> delta = calculator.calculateDelta(base, compare);

    assertEquals(1, delta.entityDelta().size());
    var entityDelta = delta.entityDelta().get(0);
    assertEquals("2", entityDelta.entityId());
    assertEquals(DeltaType.CREATE, entityDelta.type());
    assertEquals("new", entityDelta.entity().getValue());
  }

  @Test
  void shouldDetectDeletedEntities() {
    TestFeed base = new TestFeed(
      1000L,
      60L,
      List.of(new TestEntity("1", "value", 42), new TestEntity("2", "delete", 100))
    );

    TestFeed compare = new TestFeed(
      2000L,
      60L,
      List.of(new TestEntity("1", "value", 42))
    );

    GBFSFileDelta<TestEntity> delta = calculator.calculateDelta(base, compare);

    assertEquals(1, delta.entityDelta().size());
    var entityDelta = delta.entityDelta().get(0);
    assertEquals("2", entityDelta.entityId());
    assertEquals(DeltaType.DELETE, entityDelta.type());
    assertNull(entityDelta.entity());
  }

  @Test
  void shouldDetectUpdatedEntities() {
    TestFeed base = new TestFeed(1000L, 60L, List.of(new TestEntity("1", "old", 42)));

    TestFeed compare = new TestFeed(2000L, 60L, List.of(new TestEntity("1", "new", 42)));

    GBFSFileDelta<TestEntity> delta = calculator.calculateDelta(base, compare);

    assertEquals(1, delta.entityDelta().size());
    var entityDelta = delta.entityDelta().get(0);
    assertEquals("1", entityDelta.entityId());
    assertEquals(DeltaType.UPDATE, entityDelta.type());
    assertEquals("new", entityDelta.entity().getValue());
  }

  @Test
  void shouldNotCreateDeltaForUnchangedEntities() {
    TestFeed base = new TestFeed(1000L, 60L, List.of(new TestEntity("1", "value", 42)));

    TestFeed compare = new TestFeed(
      2000L,
      60L,
      List.of(new TestEntity("1", "value", 42))
    );

    GBFSFileDelta<TestEntity> delta = calculator.calculateDelta(base, compare);

    assertTrue(delta.entityDelta().isEmpty());
  }

  @Test
  void shouldHandleNullValues() {
    TestFeed base = new TestFeed(1000L, 60L, List.of(new TestEntity("1", "value", null)));

    TestFeed compare = new TestFeed(
      2000L,
      60L,
      List.of(new TestEntity("1", "value", 42))
    );

    GBFSFileDelta<TestEntity> delta = calculator.calculateDelta(base, compare);

    assertEquals(1, delta.entityDelta().size());
    var entityDelta = delta.entityDelta().get(0);
    assertEquals(42, entityDelta.entity().getNumber());
  }

  @Test
  void shouldHandleDuplicateEntitiesInBase() {
    // Base feed has duplicate entities with same ID
    TestFeed base = new TestFeed(
      1000L,
      60L,
      List.of(
        new TestEntity("1", "first", 42),
        new TestEntity("2", "second", 100),
        new TestEntity("1", "duplicate", 99) // Duplicate ID - should keep first
      )
    );

    TestFeed compare = new TestFeed(
      2000L,
      60L,
      List.of(new TestEntity("1", "updated", 42))
    );

    GBFSFileDelta<TestEntity> delta = calculator.calculateDelta(base, compare);

    // Should have one update (for ID "1") and one delete (for ID "2")
    assertEquals(2, delta.entityDelta().size());

    // Find the update delta
    var updateDelta = delta
      .entityDelta()
      .stream()
      .filter(d -> d.type() == DeltaType.UPDATE)
      .findFirst()
      .orElse(null);

    assertNotNull(updateDelta);
    assertEquals("1", updateDelta.entityId());
    assertEquals("updated", updateDelta.entity().getValue());

    // Find the delete delta
    var deleteDelta = delta
      .entityDelta()
      .stream()
      .filter(d -> d.type() == DeltaType.DELETE)
      .findFirst()
      .orElse(null);

    assertNotNull(deleteDelta);
    assertEquals("2", deleteDelta.entityId());
  }
}
