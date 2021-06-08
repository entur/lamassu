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
import org.entur.lamassu.model.entities.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

@Component
public class VehicleCacheListener extends AbstractCacheListener<Vehicle, Vehicle> implements CacheListener<Vehicle> {
    private MutableCacheEntryListenerConfiguration<String, Vehicle> listenerConfiguration;

    @Autowired
    public VehicleCacheListener(Cache<String, Vehicle> cache, CacheEntryListenerDelegate<Vehicle, Vehicle> delegate) {
        super(cache, delegate);
    }

    @Override
    protected MutableCacheEntryListenerConfiguration<String, Vehicle> getListenerConfiguration(CacheEntryListenerDelegate<Vehicle, Vehicle> delegate) {
        if (listenerConfiguration == null) {
            listenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                    FactoryBuilder.factoryOf(
                            new CacheEntryListener<>(delegate)
                    ),
                    null,
                    true,
                    true
            );
        }
        return listenerConfiguration;
    }
}
