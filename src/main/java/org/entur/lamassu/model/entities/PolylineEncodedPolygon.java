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

package org.entur.lamassu.model.entities;

import graphql.schema.DataFetchingEnvironment;
import java.io.Serializable;
import java.util.List;

//        DataFetchingEnvironment
public class PolylineEncodedPolygon implements Entity {

  private String id;
  private List<String> rings;

  public String getId() {
    return id;
  }

  public List<String> getRings() {
    return rings;
  }

  public void setRings(List<String> rings) {
    this.rings = rings;
  }
}
