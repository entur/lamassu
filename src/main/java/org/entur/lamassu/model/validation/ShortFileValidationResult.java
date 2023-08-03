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

import java.util.List;
import org.entur.gbfs.validation.model.FileValidationError;

public class ShortFileValidationResult {

  private String file;
  private boolean required;
  private boolean exists;
  private int errorsCount;
  private String version;
  private List<FileValidationError> errors;

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public boolean isExists() {
    return exists;
  }

  public void setExists(boolean exists) {
    this.exists = exists;
  }

  public int getErrorsCount() {
    return errorsCount;
  }

  public void setErrorsCount(int errorsCount) {
    this.errorsCount = errorsCount;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public List<FileValidationError> getErrors() {
    return errors;
  }

  public void setErrors(List<FileValidationError> errors) {
    this.errors = errors;
  }

  @Override
  public String toString() {
    return (
      "ShortFileValidationResult{" +
      "file='" +
      file +
      '\'' +
      ", required=" +
      required +
      ", exists=" +
      exists +
      ", errorsCount=" +
      errorsCount +
      ", version='" +
      version +
      '\'' +
      ", errors=" +
      errors +
      '}'
    );
  }
}
