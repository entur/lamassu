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

public class BoundingBoxQueryParameters {

  private Double minimumLatitude;
  private Double minimumLongitude;
  private Double maximumLatitude;
  private Double maximumLongitude;

  public BoundingBoxQueryParameters(
    Double minimumLatitude,
    Double minimumLongitude,
    Double maximumLatitude,
    Double maximumLongitude
  ) {
    this.minimumLatitude = minimumLatitude;
    this.minimumLongitude = minimumLongitude;
    this.maximumLatitude = maximumLatitude;
    this.maximumLongitude = maximumLongitude;
  }

  public Double getMinimumLatitude() {
    return minimumLatitude;
  }

  public void setMinimumLatitude(Double minimumLatitude) {
    this.minimumLatitude = minimumLatitude;
  }

  public Double getMinimumLongitude() {
    return minimumLongitude;
  }

  public void setMinimumLongitude(Double minimumLongitude) {
    this.minimumLongitude = minimumLongitude;
  }

  public Double getMaximumLatitude() {
    return maximumLatitude;
  }

  public void setMaximumLatitude(Double maximumLatitude) {
    this.maximumLatitude = maximumLatitude;
  }

  public Double getMaximumLongitude() {
    return maximumLongitude;
  }

  public void setMaximumLongitude(Double maximumLongitude) {
    this.maximumLongitude = maximumLongitude;
  }
}
