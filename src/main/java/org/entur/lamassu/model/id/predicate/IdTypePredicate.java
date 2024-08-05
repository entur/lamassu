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

import org.entur.lamassu.model.id.DefaultIdValidator;

public class IdTypePredicate implements IdPredicate {

  // type, padded with 4 chars
  protected final char[] type;

  public IdTypePredicate(CharSequence type) {
    // add 4 padding so that we can directly compare
    // indexes
    char[] chars = new char[DefaultIdValidator.ID_CODESPACE_LENGTH + 1 + type.length()];
    for (int i = 0; i < type.length(); i++) {
      chars[DefaultIdValidator.ID_CODESPACE_LENGTH + 1 + i] = type.charAt(i);
    }
    this.type = chars;
  }

  public boolean test(CharSequence t) {
    if (t.length() < type.length + 1) {
      return false;
    }
    if (
      t.charAt(DefaultIdValidator.ID_CODESPACE_LENGTH) !=
      DefaultIdValidator.ID_SEPARATOR_CHAR
    ) {
      return false;
    }

    // XXX:TYPE:
    if (t.charAt(type.length) != DefaultIdValidator.ID_SEPARATOR_CHAR) {
      return false;
    }

    for (int i = DefaultIdValidator.ID_CODESPACE_LENGTH + 1; i < type.length; i++) {
      if (type[i] != t.charAt(i)) {
        return false;
      }
    }

    return true;
  }
}
