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

package org.entur.lamassu.mapper.feedmapper;

import org.entur.gbfs.v2_2.gbfs.GBFS;
import org.entur.gbfs.v2_2.gbfs.GBFSFeed;
import org.entur.gbfs.v2_2.gbfs.GBFSFeeds;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.FeedUrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DiscoveryFeedMapper {
    private final String baseUrl;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DiscoveryFeedMapper(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public GBFS mapDiscoveryFeed(GBFS source, FeedProvider feedProvider) {
        if (source.getFeedsData() == null) {
            logger.warn("Missing discovery data for provider={} feed={}", feedProvider, source);
            return null;
        }

        var mapped = new GBFS();
        var mappedData = new GBFSFeeds();
        Map<String, GBFSFeeds> dataWrapper = new HashMap<>();
        mapped.setLastUpdated(source.getLastUpdated());
        mapped.setTtl(source.getTtl());
        mapped.setVersion(source.getVersion());

        String languageKey;
        if (source.getFeedsData().containsKey(feedProvider.getLanguage())) {
            languageKey = feedProvider.getLanguage();
        } else {
            languageKey = source.getFeedsData().keySet().iterator().next();
            logger.warn("Language key not found in discovery feed for provider {} - using {} instead", feedProvider, languageKey);
        }

        mappedData.setFeeds(
                source.getFeedsData()
                    .get(languageKey)
                    .getFeeds()
                    .stream()
                    .map(feed -> {
                        var mappedFeed = new GBFSFeed();
                        mappedFeed.setName(feed.getName());
                        mappedFeed.setUrl(FeedUrlUtil.mapFeedUrl(baseUrl, feed.getName(), feedProvider));
                        return mappedFeed;
                    }).collect(Collectors.toList())
        );
        dataWrapper.put(feedProvider.getLanguage(), mappedData);
        mapped.setFeedsData(dataWrapper);
        return mapped;
    }
}
