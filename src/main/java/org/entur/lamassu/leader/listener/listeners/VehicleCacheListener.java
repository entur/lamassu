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

package org.entur.lamassu.leader.listener.listeners;

import org.entur.lamassu.leader.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.leader.listener.CacheListener;
import org.entur.lamassu.model.entities.Vehicle;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VehicleCacheListener implements CacheListener {
    private final RMapCache<String, Vehicle> cache;
    private final CacheEntryListenerDelegate<Vehicle> delegate;
    private int delegateId;

    @Autowired
    public VehicleCacheListener(RMapCache<String, Vehicle> cache, CacheEntryListenerDelegate<Vehicle> delegate) {
        this.cache = cache;
        this.delegate = delegate;
    }

    @Override
    public void startListening() {
        delegateId = this.cache.addListener(delegate);
    }

    @Override
    public void stopListening() {
        this.cache.removeListener(delegateId);
    }
}
