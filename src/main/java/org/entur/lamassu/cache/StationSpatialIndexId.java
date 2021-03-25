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

package org.entur.lamassu.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StationSpatialIndexId {
    private static final Logger logger = LoggerFactory.getLogger(StationSpatialIndexId.class);
    private String stationId;
    private String operator;
    private String codespace;

    public static StationSpatialIndexId fromString(String indexId) {
        try {
            var parsed = new StationSpatialIndexId();
            var parts = indexId.split("_");
            parsed.setStationId(parts[0]);
            parsed.setOperator(parts[1]);
            parsed.setCodespace(parts[2]);
            return parsed;
        } catch (IndexOutOfBoundsException e) {
            logger.warn("Caught IndexOutOfBoundsException while trying to parse spatial index id {}", indexId, e);
            return null;
        }
    }

    @Override
    public String toString() {
        return stationId + '_' +
                operator + '_' +
                codespace + '_';
    }


    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getCodespace() {
        return codespace;
    }

    public void setCodespace(String codespace) {
        this.codespace = codespace;
    }
}
