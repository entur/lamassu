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

public class IdCodespaceTypePredicate implements IdPredicate {
    protected final char[] codespaceColonType;

    public IdCodespaceTypePredicate(CharSequence codespace, CharSequence type) {
        if (codespace.length() != DefaultIdValidator.ID_CODESPACE_LENGTH) {
            throw new IllegalArgumentException();
        }
        if (type.length() == 0) {
            throw new IllegalArgumentException();
        }

        char[] chars = new char[1 + codespace.length() + type.length()];

        for (int i = 0; i < codespace.length(); i++) {
            chars[i] = codespace.charAt(i);
        }
        chars[DefaultIdValidator.ID_CODESPACE_LENGTH] = DefaultIdValidator.ID_SEPARATOR_CHAR;
        for (int i = 0; i < type.length(); i++) {
            chars[DefaultIdValidator.ID_CODESPACE_LENGTH + 1 + i] = type.charAt(i);
        }
        this.codespaceColonType = chars;
    }

    public boolean test(CharSequence t) {
        // must contain all chars, plus an additional colon
        if (t.length() < codespaceColonType.length + 1) {
            return false;
        }

        if (t.charAt(codespaceColonType.length) != DefaultIdValidator.ID_SEPARATOR_CHAR) {
            return false;
        }

        for (int i = 0; i < codespaceColonType.length; i++) {
            if (codespaceColonType[i] != t.charAt(i)) {
                return false;
            }
        }

        return true;
    }
}
