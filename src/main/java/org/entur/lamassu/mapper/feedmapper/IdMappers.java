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
import no.entur.abt.netex.id.predicate.NetexIdPredicateBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IdMappers {
    public static final String STATION_ID_TYPE = "Station";
    public static final String REGION_ID_TYPE = "Region";
    public static final String ALERT_ID_TYPE = "Alert";
    public static final String PRICING_PLAN_ID_TYPE = "PricingPlan";
    public static final String VEHICLE_TYPE_ID_TYPE = "VehicleType";
    public static final String BIKE_ID_TYPE = "Vehicle";

    private IdMappers() {}

    public static String mapId(String codespace, String type, String value) {
        var predicate = NetexIdPredicateBuilder.newInstance()
                .withCodespace(codespace)
                .withType(type)
                .build();
        if (predicate.test(value)) {
            return value;
        } else {
            return NetexIdBuilder.newInstance()
                    .withCodespace(codespace)
                    .withType(type)
                    .withValue(value)
                    .build();
        }
    }

    public static Optional<List<String>> mapIds(String codespace, String type, List<String> values) {
        return Optional.ofNullable(values)
                .map(
                        v -> v.stream()
                                .map(id -> mapId(codespace, type, id))
                                .collect(Collectors.toList())
                );
    }
}
