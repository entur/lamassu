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

public class IdBuilder {

  public static IdBuilder newInstance() {
    return new IdBuilder();
  }

  protected static final String ID_SEPARATOR_CHAR = ":";

  private static final IdValidator defaultValidator = DefaultIdValidator.getInstance();

  private final IdValidator validator;

  protected String codespace;
  protected String type;
  protected String value;

  public IdBuilder() {
    this(defaultValidator);
  }

  public IdBuilder(IdValidator validator) {
    this.validator = validator;
  }

  public IdBuilder withCodespace(String codespace) {
    this.codespace = codespace;
    return this;
  }

  public IdBuilder withType(String type) {
    this.type = type;
    return this;
  }

  public IdBuilder withValue(String value) {
    this.value = value;

    return this;
  }

  public String build() {
    if (codespace == null || !validator.validateCodespace(codespace)) {
      throw new IllegalStateException(
        "Expected codespace (size 3 with characters A-Z), found " + codespace
      );
    }
    if (type == null || !validator.validateType(type)) {
      throw new IllegalStateException(
        "Expected type (nonempty with characters A-Z), found " + type
      );
    }
    if (value == null || !validator.validateValue(value)) {
      throw new IllegalStateException(
        "Expected value (nonempty with characters A-Z, a-z, ø, Ø, æ, Æ, å, Å, underscore, \\ and -), found " +
        value
      );
    }
    return String.join(ID_SEPARATOR_CHAR, codespace, type, value);
  }
}
