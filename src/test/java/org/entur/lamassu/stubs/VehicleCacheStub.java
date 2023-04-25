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

package org.entur.lamassu.stubs;

import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.model.entities.Vehicle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class VehicleCacheStub implements VehicleCache {
    private final Map<String, Vehicle> map = new HashMap<>();

    @Override
    public List<Vehicle> getAll(Set<String> keys) {
        return null;
    }

    @Override
    public List<Vehicle> getAll() {
        return null;
    }

    @Override
    public Map<String, Vehicle> getAllAsMap(Set<String> keys) {
        return null;
    }

    @Override
    public Vehicle get(String key) {
        return map.get(key);
    }

    @Override
    public void updateAll(Map<String, Vehicle> entities, int ttl, TimeUnit timeUnit) {
        map.putAll(entities);
    }

    @Override
    public void removeAll(Set<String> keys) {
        keys.forEach(map::remove);
    }

    @Override
    public boolean hasKey(String key) {
        return map.containsKey(key);
    }
}
