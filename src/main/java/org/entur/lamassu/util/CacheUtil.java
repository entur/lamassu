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

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheUtil {

  private static final Logger logger = LoggerFactory.getLogger(CacheUtil.class);

  private CacheUtil() {}

  public static int getTtl(int lastUpdated, int ttl, int minimumTtl) {
    var now = (int) Instant.now().getEpochSecond();
    return Math.max(lastUpdated + ttl - now, minimumTtl);
  }

  public static int getTtl(int lastUpdated, int ttl, int minimumTtl, int maximumTtl) {
    return Math.min(getTtl(lastUpdated, ttl, minimumTtl), maximumTtl);
  }

  public static int getMaxAge(Class<?> clazz, Object data, String systemId, String feed) {
    int maxAge = 60;
    try {
      Integer lastUpdated = (Integer) clazz.getMethod("getLastUpdated").invoke(data);
      Integer ttl = (Integer) clazz.getMethod("getTtl").invoke(data);

      maxAge = getTtl(lastUpdated, ttl, 0);
    } catch (
      IllegalAccessException
      | InvocationTargetException
      | NoSuchMethodException
      | NullPointerException e
    ) {
      logger.warn("Unable to calculate maxAge systemId={} feed={}", systemId, feed, e);
    }

    return maxAge;
  }

  public static long getLastModified(
    Class<?> clazz,
    Object data,
    String systemId,
    String feed
  ) {
    try {
      Integer lastUpdated = (Integer) clazz.getMethod("getLastUpdated").invoke(data);
      return lastUpdated.longValue() * 1000L;
    } catch (
      IllegalAccessException
      | InvocationTargetException
      | NoSuchMethodException
      | NullPointerException e
    ) {
      logger.warn(
        "Unable to calculate lastModified systemId={} feed={}",
        systemId,
        feed,
        e
      );
      return System.currentTimeMillis();
    }
  }
}
