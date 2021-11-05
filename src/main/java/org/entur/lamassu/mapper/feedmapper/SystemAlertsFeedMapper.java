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

import no.entur.abt.netex.id.NetexIdBuilder;
import no.entur.abt.netex.id.predicate.NetexIdPredicate;
import no.entur.abt.netex.id.predicate.NetexIdPredicateBuilder;
import org.entur.gbfs.v2_2.system_alerts.GBFSAlert;
import org.entur.gbfs.v2_2.system_alerts.GBFSData;
import org.entur.gbfs.v2_2.system_alerts.GBFSSystemAlerts;
import org.entur.gbfs.v2_2.system_alerts.GBFSTime;
import org.entur.lamassu.model.provider.FeedProvider;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class SystemAlertsFeedMapper {
    private static final String ID_TYPE = "Alert";

    public GBFSSystemAlerts mapSystemAlerts(GBFSSystemAlerts systemAlerts, FeedProvider feedProvider) {
        var mappedSystemAlerts = new GBFSSystemAlerts();
        mappedSystemAlerts.setVersion(systemAlerts.getVersion());
        mappedSystemAlerts.setLastUpdated(systemAlerts.getLastUpdated());
        mappedSystemAlerts.setTtl(systemAlerts.getTtl());
        mappedSystemAlerts.setData(mapData(systemAlerts.getData(), feedProvider));
        return mappedSystemAlerts;
    }

    private GBFSData mapData(GBFSData data, FeedProvider feedProvider) {
        var mappedData = new GBFSData();
        mappedData.setAlerts(
                data.getAlerts().stream().map(
                        alert -> mapAlert(alert, feedProvider)
                ).collect(Collectors.toList())
        );
        return mappedData;
    }

    private GBFSAlert mapAlert(GBFSAlert alert, FeedProvider feedProvider) {
        var mappedAlert = new GBFSAlert();
        mappedAlert.setAlertId(IdMappers.mapAlertId(feedProvider.getCodespace(), alert.getAlertId()));
        mappedAlert.setLastUpdated(alert.getLastUpdated());
        mappedAlert.setUrl(alert.getUrl());
        mappedAlert.setDescription(alert.getDescription());
        mappedAlert.setRegionIds(alert.getRegionIds().stream().map(regionId -> IdMappers.mapRegionId(feedProvider.getCodespace(), regionId)).collect(Collectors.toList()));
        mappedAlert.setDescription(alert.getDescription());
        mappedAlert.setStationIds(alert.getStationIds().stream().map(stationId -> IdMappers.mapStationId(feedProvider.getCodespace(), stationId)).collect(Collectors.toList()));
        mappedAlert.setSummary(alert.getSummary());
        mappedAlert.setTimes(alert.getTimes().stream().map(this::mapTime).collect(Collectors.toList()));
        mappedAlert.setType(alert.getType());
        return mappedAlert;
    }

    private GBFSTime mapTime(GBFSTime time) {
        var mappedTime = new GBFSTime();
        mappedTime.setStart(time.getStart());
        mappedTime.setEnd(time.getEnd());
        return mappedTime;
    }
}
