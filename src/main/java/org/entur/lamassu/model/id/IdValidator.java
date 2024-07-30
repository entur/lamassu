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

public interface IdValidator {
    default boolean validate(CharSequence id) {
        return validate(id, 0, id.length());
    }

    boolean validate(CharSequence id, int offset, int length);

    default boolean validateCodespace(CharSequence codespace) {
        return validateCodespace(codespace, 0, codespace.length());
    }

    boolean validateCodespace(CharSequence codespace, int offset, int length);

    default boolean validateType(CharSequence type) {
        return validateType(type, 0, type.length());
    }

    boolean validateType(CharSequence type, int offset, int length);

    default boolean validateValue(CharSequence value) {
        return validateValue(value, 0, value.length());
    }

    boolean validateValue(CharSequence value, int offset, int length);
}
