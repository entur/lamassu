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

package org.entur.lamassu.cache;

import java.util.concurrent.TimeUnit;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;

public interface GBFSV2FeedCache {
  <T> T find(GBFSFeedName feedName, FeedProvider feedProvider);
  <T> void update(
    GBFSFeedName feedName,
    FeedProvider feedProvider,
    T feed,
    int ttl,
    TimeUnit timeUnit
  );
  <T> T getAndUpdate(
    GBFSFeedName feedName,
    FeedProvider feedProvider,
    T feed,
    int ttl,
    TimeUnit timeUnit
  );
}
