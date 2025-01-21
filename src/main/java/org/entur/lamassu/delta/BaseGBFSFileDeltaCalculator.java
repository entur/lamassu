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

package org.entur.lamassu.delta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseGBFSFileDeltaCalculator<S, T>
  implements GBFSFileDeltaCalculator<S, T> {

  private static final List<String> EXCLUDE_METHODS = List.of(
    "toString",
    "hashCode",
    "equals"
  );

  @Override
  public final GBFSFileDelta<T> calculateDelta(@Nullable S base, @NotNull S compare) {
    List<GBFSEntityDelta<T>> entityDeltas = getEntityDeltas(base, compare);
    return getGBFSFileDelta(base, compare, entityDeltas);
  }

  private @NotNull GBFSFileDelta<T> getGBFSFileDelta(
    S base,
    @NotNull S compare,
    List<GBFSEntityDelta<T>> entityDeltas
  ) {
    return new GBFSFileDelta<>(
      getNullableLastUpdated(base),
      getLastUpdated(compare),
      getFileName(),
      entityDeltas
    );
  }

  private @NotNull List<GBFSEntityDelta<T>> getEntityDeltas(S base, @NotNull S compare) {
    List<T> baseEntities = getBaseEntities(base);
    Map<String, T> baseEntityMap = getBaseEntityMap(baseEntities);
    List<String> baseEntityIds = getEntityIds(baseEntityMap);
    List<T> compareEntities = getEntities(compare);
    List<String> compareEntityIds = getEntityIds(compareEntities);

    return Stream
      .of(
        getDeletedEntityDeltas(baseEntities, compareEntityIds),
        getKeptEntityDeltas(compareEntities, baseEntityMap, baseEntityIds)
      )
      .flatMap(Collection::stream)
      .toList();
  }

  private @NotNull List<String> getEntityIds(Map<String, ?> entityMap) {
    return entityMap.keySet().stream().toList();
  }

  private @NotNull List<String> getEntityIds(List<T> entities) {
    return entities.stream().map(this::getEntityId).toList();
  }

  private @NotNull List<T> getBaseEntities(S base) {
    return base != null ? getEntities(base) : List.of();
  }

  private @NotNull Map<String, T> getBaseEntityMap(List<T> baseEntities) {
    return baseEntities.stream().collect(Collectors.toMap(this::getEntityId, v -> v));
  }

  private @NotNull List<GBFSEntityDelta<T>> getDeletedEntityDeltas(
    List<T> baseEntities,
    List<String> compareEntityIds
  ) {
    return baseEntities
      .stream()
      .map(this::getEntityId)
      .filter(id -> !compareEntityIds.contains(id))
      .map(id -> new GBFSEntityDelta<T>(id, DeltaType.DELETE, null))
      .toList();
  }

  private @NotNull List<GBFSEntityDelta<T>> getKeptEntityDeltas(
    List<T> compareEntities,
    Map<String, T> baseEntityMap,
    List<String> baseEntityIds
  ) {
    return compareEntities
      .stream()
      // We do not need to return a delta for entities that haven't changed. We trust the implementation
      // of equals from the gbfs model here.
      .filter(entity -> !entity.equals(baseEntityMap.get(getEntityId(entity))))
      .map(entity -> {
        var entityId = getEntityId(entity);
        // If the entity exists in the base, then this delta is an update, and we can compute
        // the entity delta
        if (baseEntityIds.contains(entityId)) {
          return new GBFSEntityDelta<>(
            entityId,
            DeltaType.UPDATE,
            getEntityDelta(baseEntityMap.get(entityId), entity)
          );
          // Otherwise, this is a new entity, and the "delta" contains the entire entity
        } else {
          return new GBFSEntityDelta<>(entityId, DeltaType.CREATE, entity);
        }
      })
      .toList();
  }

  private T getEntityDelta(T base, T compare) {
    T delta = createEntity();
    Method[] methods = base.getClass().getDeclaredMethods();
    for (Method method : methods) {
      try {
        if (isMethodEligibleForDelta(method) && hasValueChanged(method, base, compare)) {
          copyValueToDelta(method, getSetter(methods, method.getName()), compare, delta);
        }
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new GBFSDeltaException("Failed to process field " + method.getName(), e);
      }
    }
    return delta;
  }

  private boolean isMethodEligibleForDelta(Method method) {
    return !EXCLUDE_METHODS.contains(method.getName()) && isGetter(method);
  }

  private boolean isGetter(Method method) {
    return method.getParameterCount() == 0;
  }

  private boolean hasValueChanged(Method method, T a, T b)
    throws IllegalAccessException, InvocationTargetException {
    Object valueA = method.invoke(a);
    return valueA == null || !valueA.equals(method.invoke(b));
  }

  private void copyValueToDelta(
    Method getter,
    Optional<Method> setter,
    T source,
    T target
  ) {
    setter.ifPresent(s -> {
      try {
        s.invoke(target, getter.invoke(source));
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new GBFSDeltaException(
          "Failed to set value for field " + getter.getName(),
          e
        );
      }
    });
  }

  private @NotNull Optional<Method> getSetter(Method[] methods, String getterName) {
    String setterName = getterName.replace("get", "set");
    return Arrays
      .stream(methods)
      .filter(method1 -> method1.getName().equals(setterName))
      .findFirst();
  }

  private Long getNullableLastUpdated(S instance) {
    if (instance == null) {
      return null;
    }
    return getLastUpdated(instance);
  }

  /**
   * Get a list of enumerable entities from the GBFS file instance
   * @param instance The GBFS file instance
   * @return List of enumerable entities of type T
   */
  protected abstract List<T> getEntities(S instance);

  /**
   * Get the id of the entity
   * @param entity The entity
   * @return The entity's id
   */
  protected abstract String getEntityId(T entity);

  /**
   * Create a new instance of the entity of type T
   * @return An instance of T
   */
  protected abstract T createEntity();

  /**
   * Get the last updated time of the GBFS file instance
   * @param instance The GBFS file instance
   * @return The last updated time
   */
  protected abstract long getLastUpdated(S instance);

  /**
   * Get the file name of the GBFS file
   * @return The file name
   */
  protected abstract String getFileName();
}
