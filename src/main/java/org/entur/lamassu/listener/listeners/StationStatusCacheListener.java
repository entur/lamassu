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
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.StationStatus;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

@Component
public class StationStatusCacheListener extends AbstractCacheListener<GBFSBase, StationStatus> implements CacheListener<StationStatus> {
    private MutableCacheEntryListenerConfiguration<String, GBFSBase> listenerConfiguration;

    protected StationStatusCacheListener(Cache<String, GBFSBase> cache, CacheEntryListenerDelegate<GBFSBase, StationStatus> delegate) {
        super(cache, delegate);
    }

    @Override
    protected MutableCacheEntryListenerConfiguration<String, GBFSBase> getListenerConfiguration(CacheEntryListenerDelegate<GBFSBase, StationStatus> delegate) {
        if (listenerConfiguration == null) {
            listenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                    FactoryBuilder.factoryOf(
                            new CacheEntryListener<>(delegate)
                    ),
                    FactoryBuilder.factoryOf(
                            StationStatusEventFilter.class
                    ),
                    true,
                    false
            );
        }
        return listenerConfiguration;
    }
}
