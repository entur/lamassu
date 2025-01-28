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

package org.entur.lamassu.stubs;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.entur.lamassu.cache.UpdateContinuityCache;

/**
 * In-memory stub implementation of UpdateContinuityCache for testing.
 */
public class StubUpdateContinuityCache implements UpdateContinuityCache {

  private final Map<String, Date> cache = new HashMap<>();

  @Override
  public Date getLastUpdateTime(String systemId) {
    return cache.get(systemId);
  }

  @Override
  public void setLastUpdateTime(String systemId, Date timestamp) {
    cache.put(systemId, timestamp);
  }

  public void clear() {
    cache.clear();
  }
}
