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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.model.entities.System;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolvableSystemsTest {

  @Mock
  private EntityReader<System> systemReader;

  private ResolvableSystems resolvableSystems;

  @BeforeEach
  void setUp() {
    resolvableSystems = new ResolvableSystems(systemReader);
  }

  @Test
  void retainsOnlyItemsWhoseSystemCanBeResolved() {
    var known = new System();
    known.setId("known");
    // Some caches report a missing key as a null value rather than leaving it out
    var found = new HashMap<String, System>();
    found.put("known", known);
    found.put("expired", null);
    when(systemReader.getAllAsMap(Set.of("known", "expired"))).thenReturn(found);

    var retained = resolvableSystems.retainResolvable(
      List.of("known:1", "expired:1", "known:2", "expired:2"),
      item -> item.split(":")[0]
    );

    assertEquals(List.of("known:1", "known:2"), retained);
  }

  @Test
  void itemsWithoutASystemIdAreLeftOutWithoutALookup() {
    var retained = resolvableSystems.retainResolvable(
      java.util.Arrays.asList((String) null),
      item -> item
    );

    assertTrue(retained.isEmpty());
    verify(systemReader, never()).getAllAsMap(anySet());
  }

  @Test
  void aSystemIdIsResolvableOnlyWhenTheSystemExists() {
    when(systemReader.get("known")).thenReturn(new System());

    assertTrue(resolvableSystems.isResolvable("known"));
    assertFalse(resolvableSystems.isResolvable("expired"));
    assertFalse(resolvableSystems.isResolvable(null));
  }
}
