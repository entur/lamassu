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

package org.entur.lamassu.util;

import java.net.URI;
import org.entur.lamassu.model.provider.FeedProvider;

public class FeedUrlUtil {

  private FeedUrlUtil() {}

  public static URI mapFeedUrl(
    String baseUrl,
    org.entur.gbfs.v2_3.gbfs.GBFSFeedName feedName,
    FeedProvider feedProvider
  ) {
    var systemId = feedProvider.getSystemId();
    var feedUrl = addToPath(baseUrl, "gbfs");
    feedUrl = addToPath(feedUrl, systemId);
    return URI.create(addToPath(feedUrl, feedName.value()).toLowerCase());
  }

  public static String mapFeedUrl(
    String baseUrl,
    org.entur.gbfs.v3_0_RC2.gbfs.GBFSFeed.Name feedName,
    FeedProvider feedProvider
  ) {
    var systemId = feedProvider.getSystemId();
    var feedUrl = addToPath(baseUrl, "gbfs/v3beta");
    feedUrl = addToPath(feedUrl, systemId);
    return addToPath(feedUrl, feedName.value()).toLowerCase();
  }

  private static String addToPath(String base, String toAdd) {
    return String.format("%s/%s", base, toAdd);
  }
}
