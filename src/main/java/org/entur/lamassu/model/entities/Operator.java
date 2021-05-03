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

package org.entur.lamassu.model.entities;

import java.io.Serializable;
import java.util.Objects;

public class Operator implements Serializable {
    private String id;
    private TranslatedString name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TranslatedString getName() {
        return name;
    }

    public void setName(TranslatedString name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Operator{" +
                "id='" + id + '\'' +
                ", name=" + name +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var operator = (Operator) o;
        return id.equals(operator.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
