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

package org.entur.lamassu.leader.entityupdater;

import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.entur.lamassu.model.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EntityCollectors {

  private EntityCollectors() {
    // Utility class
  }

  public static <
    T extends Entity
  > Collector<T, ?, Map<String, T>> toMapWithDuplicateWarning(Class<T> entityClass) {
    Logger log = LoggerFactory.getLogger(entityClass);
    return Collectors.toMap(
      Entity::getId,
      entity -> entity,
      duplicateMerger(log, entityClass.getSimpleName())
    );
  }

  private static <T extends Entity> BinaryOperator<T> duplicateMerger(
    Logger log,
    String entityTypeName
  ) {
    return (existing, duplicate) -> {
      log.warn(
        "Duplicate {} found with ID: {}. Keeping first occurrence.",
        entityTypeName.toLowerCase(),
        existing.getId()
      );
      return existing;
    };
  }
}
