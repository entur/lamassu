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

package org.entur.lamassu.mapper.feedmapper;

import org.entur.gbfs.v2_2.system_alerts.GBFSAlert;
import org.entur.gbfs.v2_2.system_alerts.GBFSData;
import org.entur.gbfs.v2_2.system_alerts.GBFSSystemAlerts;
import org.entur.gbfs.v2_2.system_alerts.GBFSTime;

import java.util.List;
import java.util.stream.Collectors;

import static org.entur.lamassu.mapper.feedmapper.IdMappers.ALERT_ID_TYPE;
import static org.entur.lamassu.mapper.feedmapper.IdMappers.REGION_ID_TYPE;
import static org.entur.lamassu.mapper.feedmapper.IdMappers.STATION_ID_TYPE;

public class SystemAlertsFeedMapper {

    public GBFSSystemAlerts mapSystemAlerts(GBFSSystemAlerts systemAlerts, String codespace) {
        var mappedSystemAlerts = new GBFSSystemAlerts();
        mappedSystemAlerts.setVersion(systemAlerts.getVersion());
        mappedSystemAlerts.setLastUpdated(systemAlerts.getLastUpdated());
        mappedSystemAlerts.setTtl(systemAlerts.getTtl());
        mappedSystemAlerts.setData(mapData(systemAlerts.getData(), codespace));
        return mappedSystemAlerts;
    }

    private GBFSData mapData(GBFSData data, String codespace) {
        var mappedData = new GBFSData();
        mappedData.setAlerts(
                data.getAlerts().stream().map(
                        alert -> mapAlert(alert, codespace)
                ).collect(Collectors.toList())
        );
        return mappedData;
    }

    private GBFSAlert mapAlert(GBFSAlert alert, String codespace) {
        var mappedAlert = new GBFSAlert();
        mappedAlert.setAlertId(IdMappers.mapId(codespace, ALERT_ID_TYPE, alert.getAlertId()));
        mappedAlert.setLastUpdated(alert.getLastUpdated());
        mappedAlert.setUrl(alert.getUrl());
        mappedAlert.setDescription(alert.getDescription());
        mappedAlert.setRegionIds(IdMappers.mapIds(codespace, REGION_ID_TYPE, alert.getRegionIds()));
        mappedAlert.setDescription(alert.getDescription());
        mappedAlert.setStationIds(IdMappers.mapIds(codespace, STATION_ID_TYPE, alert.getStationIds()));
        mappedAlert.setSummary(alert.getSummary());
        mappedAlert.setTimes(mapTimes(alert.getTimes()));
        mappedAlert.setType(alert.getType());
        return mappedAlert;
    }

    private List<GBFSTime> mapTimes(List<GBFSTime> times) {
        if (times == null) {
            return null;
        }

        return times.stream().map(this::mapTime).collect(Collectors.toList());
    }

    private GBFSTime mapTime(GBFSTime time) {
        var mappedTime = new GBFSTime();
        mappedTime.setStart(time.getStart());
        mappedTime.setEnd(time.getEnd());
        return mappedTime;
    }
}
