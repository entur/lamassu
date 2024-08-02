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

package org.entur.lamassu.model.id;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class IdBuilderTest {

  @Test
  void testCodespace() {
    String build = IdBuilder
      .newInstance()
      .withCodespace("AAA")
      .withType("Network")
      .withValue("123")
      .build();
    assertEquals("AAA:Network:123", build);
  }

  @Test
  void testInvalidCodespaceInput() {
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdBuilder.newInstance().withType("Network").withValue("123").build();
      }
    );
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdBuilder.newInstance().withCodespace("AAA").withValue("123").build();
      }
    );
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdBuilder.newInstance().withCodespace("AAA").withType("Network").build();
      }
    );
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdBuilder
          .newInstance()
          .withCodespace("AA")
          .withType("Network")
          .withValue("123")
          .build();
      }
    );
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdBuilder
          .newInstance()
          .withCodespace("AAAA")
          .withType("Network")
          .withValue("123")
          .build();
      }
    );
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdBuilder
          .newInstance()
          .withCodespace("AAA")
          .withType("")
          .withValue("123")
          .build();
      }
    );
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdBuilder
          .newInstance()
          .withCodespace("AAA")
          .withType("Network")
          .withValue("")
          .build();
      }
    );
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdBuilder.newInstance().withCodespace("AAA").withType("Network!").build();
      }
    );
  }

  @Test
  void testValidValueInput() {
    assertDoesNotThrow(() -> {
      IdBuilder
        .newInstance()
        .withCodespace("AAA")
        .withType("Vehicle")
        .withValue("ValidString123@")
        .build();
    });
    assertDoesNotThrow(() -> {
      IdBuilder
        .newInstance()
        .withCodespace("AAA")
        .withType("Vehicle")
        .withValue("Another:Valid/String_-")
        .build();
    });
    assertDoesNotThrow(() -> {
      IdBuilder
        .newInstance()
        .withCodespace("AAA")
        .withType("Vehicle")
        .withValue("EdgeCase")
        .build();
    });
    assertDoesNotThrow(() -> {
      IdBuilder
        .newInstance()
        .withCodespace("AAA")
        .withType("Vehicle")
        .withValue("Another.Valid@String:/_-")
        .build();
    });
  }

  @Test
  void testInvalidValueInput() {
    assertThrows(
      IllegalStateException.class,
      () -> {
        IdBuilder
          .newInstance()
          .withCodespace("AAA")
          .withType("Vehicle")
          .withValue("Invalid String with space")
          .build();
      }
    );

    assertThrows(
      IllegalStateException.class,
      () -> {
        IdBuilder
          .newInstance()
          .withCodespace("AAA")
          .withType("Vehicle")
          .withValue("Invalid String!")
          .build();
      }
    );

    assertThrows(
      IllegalStateException.class,
      () -> {
        IdBuilder
          .newInstance()
          .withCodespace("AAA")
          .withType("Vehicle")
          .withValue("StringWith\tTab")
          .build();
      }
    );

    assertThrows(
      IllegalStateException.class,
      () -> {
        IdBuilder
          .newInstance()
          .withCodespace("AAA")
          .withType("Vehicle")
          .withValue("StringWith\nNewline")
          .build();
      }
    );

    assertThrows(
      IllegalStateException.class,
      () -> {
        IdBuilder
          .newInstance()
          .withCodespace("AAA")
          .withType("Vehicle")
          .withValue("NonPrintable\u0001")
          .build();
      }
    );
  }

  @Test
  void testValidButDiscouragedValueInput() {
    assertDoesNotThrow(() -> {
      IdBuilder
        .newInstance()
        .withCodespace("AAA")
        .withType("Vehicle")
        .withValue("AnotherInvalid#String$")
        .build();
    });
    assertDoesNotThrow(() -> {
      IdBuilder
        .newInstance()
        .withCodespace("AAA")
        .withType("Vehicle")
        .withValue("Invalid%String^&*()")
        .build();
    });
  }
}
