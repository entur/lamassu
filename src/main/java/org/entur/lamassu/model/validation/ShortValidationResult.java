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

package org.entur.lamassu.model.validation;

import java.util.HashMap;
import java.util.Map;
import org.entur.gbfs.validation.model.ValidationSummary;

public class ShortValidationResult {

  private ValidationSummary summary = null;
  private Map<String, ShortFileValidationResult> files = new HashMap<>();

  public ValidationSummary getSummary() {
    return summary;
  }

  public void setSummary(ValidationSummary summary) {
    this.summary = summary;
  }

  public Map<String, ShortFileValidationResult> getFiles() {
    return files;
  }

  public void setFiles(Map<String, ShortFileValidationResult> files) {
    this.files = files;
  }

  @Override
  public String toString() {
    return "ShortValidationResult{" + "summary=" + summary + ", files=" + files + '}';
  }
}
