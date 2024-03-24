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

package org.entur.lamassu.mapper.feedmapper.v3;

import static org.entur.lamassu.mapper.feedmapper.IdMappers.ALERT_ID_TYPE;
import static org.entur.lamassu.mapper.feedmapper.IdMappers.REGION_ID_TYPE;
import static org.entur.lamassu.mapper.feedmapper.IdMappers.STATION_ID_TYPE;

import java.util.List;
import java.util.stream.Collectors;
import org.entur.gbfs.v3_0_RC2.system_alerts.GBFSAlert;
import org.entur.gbfs.v3_0_RC2.system_alerts.GBFSData;
import org.entur.gbfs.v3_0_RC2.system_alerts.GBFSSystemAlerts;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.mapper.feedmapper.IdMappers;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.stereotype.Component;

@Component
public class V3SystemAlertsFeedMapper extends AbstractFeedMapper<GBFSSystemAlerts> {

  private static final GBFSSystemAlerts.Version VERSION =
    GBFSSystemAlerts.Version._3_0_RC_2;

  public GBFSSystemAlerts map(GBFSSystemAlerts systemAlerts, FeedProvider feedProvider) {
    if (systemAlerts == null) {
      return null;
    }

    var codespace = feedProvider.getCodespace();
    var mappedSystemAlerts = new GBFSSystemAlerts();
    mappedSystemAlerts.setVersion(VERSION);
    mappedSystemAlerts.setLastUpdated(systemAlerts.getLastUpdated());
    mappedSystemAlerts.setTtl(systemAlerts.getTtl());
    mappedSystemAlerts.setData(mapData(systemAlerts.getData(), codespace));
    return mappedSystemAlerts;
  }

  private GBFSData mapData(GBFSData data, String codespace) {
    var mappedData = new GBFSData();
    mappedData.setAlerts(mapAlerts(data.getAlerts(), codespace));
    return mappedData;
  }

  private List<GBFSAlert> mapAlerts(List<GBFSAlert> alerts, String codespace) {
    return alerts
      .stream()
      .map(alert -> mapAlert(alert, codespace))
      .collect(Collectors.toList());
  }

  private GBFSAlert mapAlert(GBFSAlert alert, String codespace) {
    var mappedAlert = new GBFSAlert();
    mappedAlert.setAlertId(IdMappers.mapId(codespace, ALERT_ID_TYPE, alert.getAlertId()));
    mappedAlert.setLastUpdated(alert.getLastUpdated());
    mappedAlert.setUrl(alert.getUrl());
    mappedAlert.setDescription(alert.getDescription());
    mappedAlert.setRegionIds(
      IdMappers.mapIds(codespace, REGION_ID_TYPE, alert.getRegionIds()).orElse(null)
    );
    mappedAlert.setDescription(alert.getDescription());
    mappedAlert.setStationIds(
      IdMappers.mapIds(codespace, STATION_ID_TYPE, alert.getStationIds()).orElse(null)
    );
    mappedAlert.setSummary(alert.getSummary());
    mappedAlert.setTimes(alert.getTimes());
    mappedAlert.setType(alert.getType());
    return mappedAlert;
  }
}
