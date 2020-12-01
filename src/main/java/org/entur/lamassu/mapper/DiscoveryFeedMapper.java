package org.entur.lamassu.mapper;

import org.entur.lamassu.model.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DiscoveryFeedMapper {
    @Value("${org.entur.lamassu.baseUrl}")
    private String baseUrl;

    public GBFS mapDiscoveryFeed(GBFS source, FeedProvider feedProvider) {
        GBFS mapped = new GBFS();
        GBFS.Data mappedData = new GBFS.Data();
        Map<String, GBFS.Data> dataWrapper = new HashMap<>();
        mapped.setLastUpdated(source.getLastUpdated());
        mapped.setTtl(source.getTtl());
        mapped.setVersion(source.getVersion());
        mappedData.setFeeds(
                source.getData()
                    .get(feedProvider.getLanguage())
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

    private String mapFeedUrl(GBFSFeedName feedName, FeedProvider feedProvider) {
        String codespace = feedProvider.getCodespace();
        String city = feedProvider.getCity();
        String vehicleType = feedProvider.getVehicleType();
        var feedUrl = String.format("%s/gbfs/%s", baseUrl, codespace);
        if (city != null) {
            feedUrl = String.format("%s/%s", feedUrl, city);
        }
        if (vehicleType != null) {
            feedUrl = String.format("%s/%s", feedUrl, vehicleType);
        }
        return String.format("%s/%s", feedUrl, feedName.toValue()).toLowerCase();
    }
}
