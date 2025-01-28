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
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStation;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStationStatus;

public class GBFSStationStatusDeltaCalculator
  extends BaseGBFSFileDeltaCalculator<GBFSStationStatus, GBFSStation> {

  public static final String FILE_NAME = "station_status";

  @Override
  protected List<GBFSStation> getEntities(GBFSStationStatus instance) {
    return instance.getData().getStations();
  }

  @Override
  protected String getEntityId(GBFSStation entity) {
    return entity.getStationId();
  }

  @Override
  protected GBFSStation createEntity() {
    return new GBFSStation();
  }

  @Override
  protected long getLastUpdated(GBFSStationStatus instance) {
    return instance.getLastUpdated().getTime();
  }

  @Override
  protected String getFileName() {
    return FILE_NAME;
  }
}
