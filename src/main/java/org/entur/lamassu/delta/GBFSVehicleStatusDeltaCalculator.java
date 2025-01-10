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

package org.entur.lamassu.delta;

import java.util.List;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicleStatus;

public class GBFSVehicleStatusDeltaCalculator
  extends BaseGBFSFileDeltaCalculator<GBFSVehicleStatus, GBFSVehicle> {

  public static final String FILE_NAME = "vehicle_status";

  @Override
  protected List<GBFSVehicle> getEntities(GBFSVehicleStatus instance) {
    return instance.getData().getVehicles();
  }

  @Override
  protected String getEntityId(GBFSVehicle entity) {
    return entity.getVehicleId();
  }

  @Override
  protected GBFSVehicle createEntity() {
    return new GBFSVehicle();
  }

  @Override
  protected long getLastUpdated(GBFSVehicleStatus instance) {
    return instance.getLastUpdated().getTime();
  }

  @Override
  protected long getTtl(GBFSVehicleStatus instance) {
    return instance.getTtl();
  }

  @Override
  protected String getFileName() {
    return FILE_NAME;
  }
}
