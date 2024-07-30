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

package org.entur.lamassu.model.id.predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class IdPredicateBuilderTest {

  @Test
  void testCodespace() {
    IdPredicate predicate = IdPredicateBuilder.newInstance().withCodespace("AAA").build();

    assertTrue(predicate.test("AAA:Network:123"));
    assertFalse(predicate.test("BBB:Network:123"));
    assertTrue(predicate.test("AAA:xyz:123"));
    assertFalse(predicate.test("CCC:z:123"));

    assertFalse(predicate.test("AAAANetwork:123"));

    // non-validating
    assertTrue(predicate.test("AAA:X"));

    assertFalse(predicate.test("AAB:Network:123"));
    assertFalse(predicate.test("ABA:Network:123"));
    assertFalse(predicate.test("BAA:Network:123"));
  }

  @Test
  void testType() {
    IdPredicate predicate = IdPredicateBuilder.newInstance().withType("Network").build();

    assertTrue(predicate.test("AAA:Network:123"));
    assertTrue(predicate.test("BBB:Network:123"));
    assertFalse(predicate.test("BBB:Netzork:123"));
    assertFalse(predicate.test("AAA:xyz:1234567"));
    assertFalse(predicate.test("CCC:z:123"));

    // partical validation
    assertFalse(predicate.test("AA:Network:123"));
    assertFalse(predicate.test(":Network:123"));
  }

  @Test
  void testCodespaceAndType() {
    IdPredicate predicate = IdPredicateBuilder
      .newInstance()
      .withCodespace("AAA")
      .withType("Network")
      .build();

    assertTrue(predicate.test("AAA:Network:123"));
    assertFalse(predicate.test("BBB:Network:123"));
    assertFalse(predicate.test("AAA:xyz:123"));
    assertFalse(predicate.test("CCC:z:123"));

    // partical validation
    assertFalse(predicate.test("AA:Network:123"));
    assertFalse(predicate.test(":Network:123"));
  }

  @Test
  void testInvalidCodespaceInput() {
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdPredicateBuilder.newInstance().withCodespace(null).build();
      }
    );
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdPredicateBuilder.newInstance().withCodespace("").build();
      }
    );
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdPredicateBuilder.newInstance().withCodespace("AA").build();
      }
    );
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdPredicateBuilder.newInstance().withCodespace("AAAA").build();
      }
    );
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdPredicateBuilder.newInstance().withCodespace("aaa").build();
      }
    );
  }

  @Test
  void testInvalidTypeInput() {
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdPredicateBuilder.newInstance().withType(null).build();
      }
    );
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdPredicateBuilder.newInstance().withType("").build();
      }
    );
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdPredicateBuilder.newInstance().withType("Network!").build();
      }
    );
  }

  @Test
  void testStream() {
    IdPredicate predicate = IdPredicateBuilder.newInstance().withCodespace("AAA").build();

    List<String> names = Arrays.asList("AAA:B:C", "BBB:B:C");

    List<String> result = names.stream().filter(predicate).toList();

    assertEquals(1, result.size());
    assertTrue(result.contains("AAA:B:C"));
  }
}
