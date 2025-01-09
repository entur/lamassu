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

package org.entur.lamassu.service;

import java.util.List;

public abstract class FilterParameters {

  private List<String> codespaces;
  private List<String> systems;
  private List<String> operators;
  private Integer count;

  public FilterParameters(
    List<String> codespaces,
    List<String> systems,
    List<String> operators,
    Integer count
  ) {
    this.codespaces = codespaces;
    this.systems = systems;
    this.operators = operators;
    this.count = count;
  }

  public List<String> getCodespaces() {
    return codespaces;
  }

  public void setCodespaces(List<String> codespaces) {
    this.codespaces = codespaces;
  }

  public List<String> getSystems() {
    return systems;
  }

  public void setSystems(List<String> systems) {
    this.systems = systems;
  }

  public List<String> getOperators() {
    return operators;
  }

  public void setOperators(List<String> operators) {
    this.operators = operators;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }
}
