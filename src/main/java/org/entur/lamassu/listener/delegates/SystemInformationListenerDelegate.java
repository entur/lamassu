package org.entur.lamassu.listener.delegates;

import org.entur.lamassu.cache.SystemCache;
import org.entur.lamassu.cache.VehicleTypeCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.mapper.SystemMapper;
import org.entur.lamassu.mapper.VehicleTypeMapper;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.SystemInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;
import java.util.Map;

@Component
public class SystemInformationListenerDelegate implements CacheEntryListenerDelegate<GBFSBase, SystemInformation> {
    private final SystemMapper systemMapper;
    private final SystemCache systemCache;
    private final FeedProviderConfig feedProviderConfig;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public SystemInformationListenerDelegate(SystemMapper systemMapper, SystemCache systemCache, FeedProviderConfig feedProviderConfig) {
        this.systemMapper = systemMapper;
        this.systemCache = systemCache;
        this.feedProviderConfig = feedProviderConfig;
    }

    @Override
    public void onCreated(CacheEntryEvent<? extends String, GBFSBase> event) {
        addOrUpdateSystem(event);
    }

    @Override
    public void onUpdated(CacheEntryEvent<? extends String, GBFSBase> event) {
        addOrUpdateSystem(event);
    }

    @Override
    public void onRemoved(CacheEntryEvent<? extends String, GBFSBase> event) {
        // TODO implement
    }

    @Override
    public void onExpired(CacheEntryEvent<? extends String, GBFSBase> event) {
        // TODO implement
    }

    public void addOrUpdateSystem(CacheEntryEvent<? extends String, ? extends GBFSBase> event) {
        var split = event.getKey().split("_");
        var feedProvider = feedProviderConfig.get(split[split.length - 1]);
        var systemInformationFeed = (SystemInformation) event.getValue();
        try {
            var systemInformation = systemInformationFeed.getData();
            var system = systemMapper.mapSystem(systemInformation);
            systemCache.updateAll(Map.of(feedProvider.getName(), system));
            logger.info("Added system to system cache from feed {}", event.getKey());
        } catch (NullPointerException e) {
            logger.warn("Caught NullPointerException when updating system cache from systemInformationFeed: {}", systemInformationFeed, e);
        }
    }
}
