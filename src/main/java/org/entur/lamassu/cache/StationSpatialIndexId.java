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

import java.util.List;

public class StationSpatialIndexId extends AbstractSpatialIndexId implements SpatialIndexId {
    private List<FormFactor> formFactors;
    private List<PropulsionType> propulsionTypes;

    public List<FormFactor> getFormFactors() {
        return formFactors;
    }

    public void setFormFactors(List<FormFactor> formFactors) {
        this.formFactors = formFactors;
    }

    public List<PropulsionType> getPropulsionTypes() {
        return propulsionTypes;
    }

    public void setPropulsionTypes(List<PropulsionType> propulsionTypes) {
        this.propulsionTypes = propulsionTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractSpatialIndexId that = (AbstractSpatialIndexId) o;
        return getId().equals(that.getId());
    }
}
