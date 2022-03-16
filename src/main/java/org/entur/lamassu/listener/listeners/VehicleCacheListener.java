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

package org.entur.lamassu.listener.listeners;

import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.listener.CacheListener;
import org.entur.lamassu.listener.delegates.VehicleListenerDelegate;
import org.entur.lamassu.model.entities.Vehicle;
import org.redisson.api.RLocalCachedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

@Component
public class VehicleCacheListener implements CacheListener {
    private final RLocalCachedMap<String, Vehicle> cache;
    private final VehicleListenerDelegate delegate;
    private int delegateId;

    @Autowired
    public VehicleCacheListener(RLocalCachedMap<String, Vehicle> cache, VehicleListenerDelegate delegate) {
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
