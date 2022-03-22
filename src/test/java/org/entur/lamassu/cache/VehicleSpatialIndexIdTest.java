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


import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.entur.lamassu.cache.AbstractSpatialIndexId.SPATIAL_INDEX_ID_SEPARATOR;

public class VehicleSpatialIndexIdTest {
    private static final String expectedVehicleId = "TST:Vehicle:12";
    private static final String expectedCodespace = "TST";
    private static final String expectedSystemId = "TST:System:1";
    private static final String expectedOperatorId = "TST:Operator:1";
    private static final FormFactor expectedFormFactor = FormFactor.SCOOTER;
    private static final PropulsionType expectedPropulsionType = PropulsionType.ELECTRIC;
    private static final boolean expectedReserved = false;
    private static final boolean expectedDisabled = false;

    private static final String toTest = expectedVehicleId
            + SPATIAL_INDEX_ID_SEPARATOR
            + expectedCodespace
            + SPATIAL_INDEX_ID_SEPARATOR
            + expectedSystemId
            + SPATIAL_INDEX_ID_SEPARATOR
            + expectedOperatorId
            + SPATIAL_INDEX_ID_SEPARATOR
            + expectedFormFactor
            + SPATIAL_INDEX_ID_SEPARATOR
            + expectedPropulsionType
            + SPATIAL_INDEX_ID_SEPARATOR
            + expectedReserved
            + SPATIAL_INDEX_ID_SEPARATOR
            + expectedDisabled;

    @Test
    void testFromString() {
        var testSubject = getTestSubject();
        Assertions.assertNotNull(testSubject);
        Assertions.assertEquals(expectedVehicleId, testSubject.getId());
        Assertions.assertEquals(expectedCodespace, testSubject.getCodespace());
        Assertions.assertEquals(expectedSystemId, testSubject.getSystemId());
        Assertions.assertEquals(expectedOperatorId, testSubject.getOperatorId());
        Assertions.assertEquals(expectedFormFactor, testSubject.getFormFactor());
        Assertions.assertEquals(expectedPropulsionType, testSubject.getPropulsionType());
        Assertions.assertEquals(expectedReserved, testSubject.getReserved());
        Assertions.assertEquals(expectedDisabled, testSubject.getDisabled());
    }

    private VehicleSpatialIndexId getTestSubject() {
        return VehicleSpatialIndexId.fromString(toTest);
    }
}
