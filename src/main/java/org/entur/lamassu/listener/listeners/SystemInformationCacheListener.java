package org.entur.lamassu.listener.listeners;

import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.listener.CacheListener;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.SystemInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

@Component
public class SystemInformationCacheListener extends AbstractCacheListener<GBFSBase, SystemInformation> implements CacheListener<SystemInformation> {
    private MutableCacheEntryListenerConfiguration<String, GBFSBase> listenerConfiguration;

    @Autowired
    public SystemInformationCacheListener(Cache<String, GBFSBase> cache, CacheEntryListenerDelegate<GBFSBase, SystemInformation> delegate) {
        super(cache, delegate);
    }

    @Override
    protected MutableCacheEntryListenerConfiguration<String, GBFSBase> getListenerConfiguration(CacheEntryListenerDelegate<GBFSBase, SystemInformation> delegate) {
        if (listenerConfiguration == null) {
            listenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                    FactoryBuilder.factoryOf(
                            new CacheEntryListener<>(delegate)
                    ),
                    FactoryBuilder.factoryOf(
                            SystemInformationEventFilter.class
                    ),
                    false,
                    false
            );
        }
        return listenerConfiguration;
    }
}
