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
    protected VehicleCacheListener(Cache<String, Vehicle> cache, CacheEntryListenerDelegate<Vehicle, Vehicle> delegate) {
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
                    false,
                    false
            );
        }
        return listenerConfiguration;
    }
}
