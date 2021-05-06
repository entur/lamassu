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

package org.entur.lamassu.mapper;

import org.entur.lamassu.model.entities.RentalUris;
import org.springframework.stereotype.Component;

@Component
public class RentalUrisMapper {
    public RentalUris mapRentalUris(org.entur.lamassu.model.gbfs.v2_1.RentalUris rentalUris) {
        if (rentalUris == null) {
            return null;
        }

        var mapped = new RentalUris();
        mapped.setAndroid(rentalUris.getAndroid());
        mapped.setIos(rentalUris.getIos());
        mapped.setWeb(rentalUris.getWeb());
        return mapped;
    }
}
