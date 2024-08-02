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

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultIdValidator implements IdValidator {

  public static final char ID_SEPARATOR_CHAR = ':';
  public static final int ID_CODESPACE_LENGTH = 3;
  public static final int ID_MINIMUM_LENGTH = 6;

  protected static final DefaultIdValidator instance = new DefaultIdValidator();
  private static final Logger log = LoggerFactory.getLogger(DefaultIdValidator.class);

  public static DefaultIdValidator getInstance() {
    return instance;
  }

  public boolean validate(CharSequence string, int offset, int length) {
    // minimum size is XXX:X:X
    if (length < ID_MINIMUM_LENGTH) {
      return false;
    }
    if (string.charAt(offset + ID_CODESPACE_LENGTH) != ':') {
      return false;
    }
    int last = getLastSeperatorIndex(string, ID_CODESPACE_LENGTH + 1, length);
    if (last == -1) {
      return false;
    }
    return (
      validateCodespace(string, 0, ID_CODESPACE_LENGTH) &&
      validateType(string, ID_CODESPACE_LENGTH + 1, last) &&
      validateValue(string, last + 1, string.length())
    );
  }

  protected static int getLastSeperatorIndex(
    CharSequence string,
    int startIndex,
    int endIndex
  ) {
    for (int i = endIndex - 1; i >= startIndex; i--) {
      if (string.charAt(i) == ID_SEPARATOR_CHAR) {
        return i;
      }
    }
    return -1;
  }

  public boolean validateCodespace(CharSequence codespace, int startIndex, int endIndex) {
    // length 3
    // A-Z
    if (endIndex - startIndex == ID_CODESPACE_LENGTH) {
      for (int i = startIndex; i < endIndex; i++) {
        char c = codespace.charAt(i);
        if (c < 'A' || c > 'Z') {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  public boolean validateType(CharSequence type, int startIndex, int endIndex) {
    // not empty string
    // A-Z
    // a-z
    if (endIndex > startIndex) {
      for (int i = startIndex; i < endIndex; i++) {
        char c = type.charAt(i);
        if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  public boolean validateValue(CharSequence value, int startIndex, int endIndex) {
    if (endIndex > startIndex) {
      for (int i = startIndex; i < endIndex; i++) {
        char c = value.charAt(i);
        if (!isValueCharacter(c)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  protected static boolean isValueCharacter(char c) {
    if (c < 0x21 || c > 0x7E) {
      return false; // Not in the ASCII printable range
    }

    if (
      (c >= 'A' && c <= 'Z') ||
      (c >= 'a' && c <= 'z') ||
      (c >= '0' && c <= '9') ||
      !List.of('.', '@', ':', '/', '_', '-').contains(c)
    ) {
      log.warn("SHOULD be restricted to `A-Z`, `a-z`, `0-9` and `.@:/_-` *(as of v3.0)*");
    }

    return true;
  }
}
