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

package org.entur.lamassu.controller;

import java.util.NoSuchElementException;
import org.entur.gbfs.v2_3.gbfs.GBFSFeedName;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.service.FeedProviderService;
import org.jetbrains.annotations.NotNull;

public abstract class BaseGBFSFeedController {

  private final GBFSFeedCache feedCache;
  private final FeedProviderService feedProviderService;

  protected BaseGBFSFeedController(
    GBFSFeedCache feedCache,
    FeedProviderService feedProviderService
  ) {
    this.feedCache = feedCache;
    this.feedProviderService = feedProviderService;
  }

  @NotNull
  protected Object getFeed(String systemId, String feed) {
    var feedName = GBFSFeedName.fromValue(feed);
    var feedProvider = feedProviderService.getFeedProviderBySystemId(systemId);

    if (feedProvider == null) {
      throw new NoSuchElementException();
    }

    var data = feedCache.find(feedName, feedProvider);

    if (data == null) {
      throw new NoSuchElementException();
    }
    return data;
  }
}
