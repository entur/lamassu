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
import org.entur.lamassu.model.id.IdValidator;

public class IdPredicateBuilder {

  private static final IdValidator validator = DefaultIdValidator.getInstance();

  public static IdPredicateBuilder newInstance() {
    return new IdPredicateBuilder();
  }

  protected String codespace;
  protected String type;

  public IdPredicateBuilder withCodespace(String codespace) {
    this.codespace = codespace;
    return this;
  }

  public IdPredicateBuilder withType(String type) {
    this.type = type;
    return this;
  }

  public IdPredicate build() {
    if (codespace != null && !validator.validateCodespace(codespace)) {
      throw new IllegalStateException(
        "Expected codespace (size 3 with characters A-Z), found " + codespace
      );
    }
    if (type != null && !validator.validateType(type)) {
      throw new IllegalStateException(
        "Expected type (nonempty with characters A-Z), found " + type
      );
    }
    if (codespace != null && type != null) {
      return new IdCodespaceTypePredicate(codespace, type);
    } else if (codespace != null) {
      return new IdCodespacePredicate(codespace);
    } else if (type != null) {
      return new IdTypePredicate(type);
    } else {
      throw new IllegalStateException("Expected codespace and/or type");
    }
  }
}
