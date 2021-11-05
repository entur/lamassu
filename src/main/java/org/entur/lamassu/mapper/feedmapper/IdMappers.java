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

public class IdMappers {
    private static String mapId(String codespace, String type, String value) {
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

    public static String mapAlertId(String codespace, String value) {
        return mapId(codespace, "Alert", value);
    }

    public static String mapRegionId(String codespace, String value) {
        return mapId(codespace, "Region", value);
    }

    public static String mapStationId(String codespace, String value) {
        return mapId(codespace, "Station", value);
    }
}
