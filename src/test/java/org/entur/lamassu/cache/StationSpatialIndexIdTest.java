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


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.entur.lamassu.cache.AbstractSpatialIndexId.SPATIAL_INDEX_ID_SEPARATOR;

public class StationSpatialIndexIdTest {
    private static final String expectedStationId = "TST:Station:12";
    private static final String expectedCodespace = "TST";
    private static final String expectedSystemId = "TST:System:1";
    private static final String expectedOperatorId = "TST:Operator:1";
    private static final String toTest = expectedStationId
            + SPATIAL_INDEX_ID_SEPARATOR
            + expectedCodespace
            + SPATIAL_INDEX_ID_SEPARATOR
            + expectedSystemId
            + SPATIAL_INDEX_ID_SEPARATOR
            + expectedOperatorId;

    @Test
    void testFromString() {
        var testSubject = getTestSubject();
        Assertions.assertNotNull(testSubject);
        Assertions.assertEquals(expectedStationId, testSubject.getId());
        Assertions.assertEquals(expectedCodespace, testSubject.getCodespace());
        Assertions.assertEquals(expectedSystemId, testSubject.getSystemId());
        Assertions.assertEquals(expectedOperatorId, testSubject.getOperatorId());
    }

    private StationSpatialIndexId getTestSubject() {
        return StationSpatialIndexId.fromString(toTest);
    }
}
