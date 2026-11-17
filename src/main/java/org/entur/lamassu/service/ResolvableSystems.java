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

package org.entur.lamassu.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.model.entities.System;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Tells which systems can be resolved, so that vehicles and stations whose system can't be
 * are left out of query results.
 *
 * <p>Their system is a non-null field in the GraphQL schema, so a single such entity makes
 * the whole result fail, including the entities of every other provider in it. The system
 * entry expires when its provider stops serving system_information, while the vehicles and
 * stations stay cached, so this happens whenever a feed breaks in that way.
 */
@Component
public class ResolvableSystems {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final EntityReader<System> systemReader;

  @Autowired
  public ResolvableSystems(EntityReader<System> systemReader) {
    this.systemReader = systemReader;
  }

  /**
   * Whether the system can be resolved. A null system id never can.
   */
  public boolean isResolvable(String systemId) {
    return systemId != null && systemReader.get(systemId) != null;
  }

  /**
   * Keeps the items whose system can be resolved, in their original order. Looks each
   * distinct system up once.
   */
  public <T> List<T> retainResolvable(
    Collection<T> items,
    Function<T, String> systemIdOf
  ) {
    Set<String> distinctSystemIds = items
      .stream()
      .map(systemIdOf)
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
    Set<String> resolvable = distinctSystemIds.isEmpty()
      ? Set.of()
      : systemReader
        .getAllAsMap(distinctSystemIds)
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue() != null)
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());

    var retained = items
      .stream()
      .filter(item -> {
        var systemId = systemIdOf.apply(item);
        return systemId != null && resolvable.contains(systemId);
      })
      .toList();
    if (retained.size() < items.size()) {
      logger.debug(
        "Left out {} of {} entities without a resolvable system, systems without one: {}",
        items.size() - retained.size(),
        items.size(),
        distinctSystemIds
          .stream()
          .filter(id -> !resolvable.contains(id))
          .sorted()
          .toList()
      );
    }
    return retained;
  }
}
