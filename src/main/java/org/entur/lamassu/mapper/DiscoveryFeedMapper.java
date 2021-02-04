package org.entur.lamassu.mapper;

import org.entur.lamassu.model.feedprovider.FeedProvider;
import org.entur.lamassu.model.feedprovider.FeedProviderDiscovery;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DiscoveryFeedMapper {
    @Value("${org.entur.lamassu.baseUrl}")
    private String baseUrl;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public GBFS mapDiscoveryFeed(GBFS source, FeedProvider feedProvider) {
        GBFS mapped = new GBFS();
        GBFS.Data mappedData = new GBFS.Data();
        Map<String, GBFS.Data> dataWrapper = new HashMap<>();
        mapped.setLastUpdated(source.getLastUpdated());
        mapped.setTtl(source.getTtl());
        mapped.setVersion(source.getVersion());

        String languageKey;
        if (source.getData().containsKey(feedProvider.getLanguage())) {
            languageKey = feedProvider.getLanguage();
        } else {
            languageKey = source.getData().keySet().iterator().next();
            logger.warn("Language key not found in discovery feed for provider {} - using {} instead", feedProvider, languageKey);
        }

        mappedData.setFeeds(
                source.getData()
                    .get(languageKey)
                    .getFeeds()
                    .stream()
                    .map(feed -> {
                        var mappedFeed = new GBFS.GBFSFeed();
                        mappedFeed.setName(feed.getName());
                        mappedFeed.setUrl(mapFeedUrl(feed.getName(), feedProvider));
                        return mappedFeed;
                    }).collect(Collectors.toList())
        );
        dataWrapper.put(feedProvider.getLanguage(), mappedData);
        mapped.setData(dataWrapper);
        return mapped;
    }

    public FeedProviderDiscovery.Provider mapFeedProvider(FeedProvider feedProvider) {
        FeedProviderDiscovery.Provider mapped = new FeedProviderDiscovery.Provider();
        mapped.setName(feedProvider.getName());
        mapped.setUrl(mapFeedUrl(GBFSFeedName.GBFS, feedProvider));
        return mapped;
    }

    private String mapFeedUrl(GBFSFeedName feedName, FeedProvider feedProvider) {
        var providerName= feedProvider.getName();
        var feedUrl = addToPath(baseUrl, "gbfs");
        feedUrl = addToPath(feedUrl, providerName);
        return addToPath(feedUrl, feedName.toValue()).toLowerCase();
    }

    private String addToPath(String base, String toAdd) {
        return String.format("%s/%s", base, toAdd);
    }
}
