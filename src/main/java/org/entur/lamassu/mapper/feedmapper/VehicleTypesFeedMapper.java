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

import org.entur.gbfs.v2_2.vehicle_types.GBFSData;
import org.entur.gbfs.v2_2.vehicle_types.GBFSVehicleType;
import org.entur.gbfs.v2_2.vehicle_types.GBFSVehicleTypes;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.entur.lamassu.mapper.feedmapper.IdMappers.VEHICLE_TYPE_ID_TYPE;

@Component
public class VehicleTypesFeedMapper implements FeedMapper<GBFSVehicleTypes> {
    @Value("${org.entur.lamassu.targetGbfsVersion:2.2}")
    private String targetGbfsVersion;

    @Override
    public GBFSVehicleTypes map(GBFSVehicleTypes source, FeedProvider feedProvider) {
        if (feedProvider.getVehicleTypes() != null) {
            return customVehicleTypes(feedProvider);
        }

        if (source == null) {
            return null;
        }

        var mapped = new GBFSVehicleTypes();
        mapped.setVersion(GBFSVehicleTypes.Version.fromValue(targetGbfsVersion));
        mapped.setTtl(source.getTtl());
        mapped.setLastUpdated(source.getLastUpdated());
        mapped.setData(mapData(source.getData(), feedProvider.getCodespace()));
        return mapped;
    }

    private GBFSVehicleTypes customVehicleTypes(FeedProvider feedProvider) {
        var custom = new GBFSVehicleTypes();
        custom.setVersion(GBFSVehicleTypes.Version.fromValue(targetGbfsVersion));
        custom.setLastUpdated((int) System.currentTimeMillis() / 1000);
        custom.setTtl((int)Duration.ofDays(1).toSeconds());
        var data = new GBFSData();
        data.setVehicleTypes(feedProvider.getVehicleTypes());
        custom.setData(mapData(data, feedProvider.getCodespace()));
        return custom;
    }

    private GBFSData mapData(GBFSData data, String codespace) {
        var mapped = new GBFSData();
        mapped.setVehicleTypes(mapVehicleTypes(data.getVehicleTypes(), codespace));
        return mapped;
    }

    private List<GBFSVehicleType> mapVehicleTypes(List<GBFSVehicleType> vehicleTypes, String codespace) {
        return vehicleTypes.stream()
                .map(vehicleType -> mapVehicleType(vehicleType, codespace))
                .collect(Collectors.toList());
    }

    private GBFSVehicleType mapVehicleType(GBFSVehicleType vehicleType, String codespace) {
        var mapped = new GBFSVehicleType();
        mapped.setVehicleTypeId(IdMappers.mapId(codespace, VEHICLE_TYPE_ID_TYPE, vehicleType.getVehicleTypeId()));
        mapped.setName(vehicleType.getName());
        mapped.setPropulsionType(vehicleType.getPropulsionType());
        mapped.setFormFactor(vehicleType.getFormFactor());
        mapped.setMaxRangeMeters(vehicleType.getMaxRangeMeters());
        return mapped;
    }
}
