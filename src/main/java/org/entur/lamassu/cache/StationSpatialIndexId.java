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

public class StationSpatialIndexId extends AbstractSpatialIndexId {
    private static final Logger logger = LoggerFactory.getLogger(StationSpatialIndexId.class);

    public static StationSpatialIndexId fromString(String indexId) {
        try {
            var parsed = new StationSpatialIndexId();
            var parts = indexId.split(SPATIAL_INDEX_ID_SEPARATOR);
            parsed.parse(parts);
            return parsed;
        } catch (IndexOutOfBoundsException e) {
            logger.warn("Caught IndexOutOfBoundsException while trying to parse spatial index id {}", indexId, e);
            return null;
        }
    }

    @Override
    public String toString() {
        return getId() + SPATIAL_INDEX_ID_SEPARATOR +
                getCodespace() + SPATIAL_INDEX_ID_SEPARATOR +
                getSystemId() + SPATIAL_INDEX_ID_SEPARATOR +
                getOperatorId();
    }
}
