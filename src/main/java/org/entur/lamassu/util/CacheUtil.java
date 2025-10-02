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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

public class CacheUtil {

  private static final Logger logger = LoggerFactory.getLogger(CacheUtil.class);

  private CacheUtil() {}

  public static int getTtl(int now, int lastUpdated, int ttl, int minimumTtl) {
    return Math.max(lastUpdated + ttl - now, minimumTtl);
  }

  public static int getTtl(
    int now,
    int lastUpdated,
    int ttl,
    int minimumTtl,
    int maximumTtl
  ) {
    return Math.min(getTtl(now, lastUpdated, ttl, minimumTtl), maximumTtl);
  }

  public static int getMaxAge(
    Class<?> clazz,
    Object data,
    String systemId,
    String feed,
    int now
  ) {
    return getMaxAge(clazz, data, systemId, feed, now, 0);
  }

  public static int getMaxAge(
    Class<?> clazz,
    Object data,
    String systemId,
    String feed,
    int now,
    int minimumTtl
  ) {
    int maxAge = 60;
    try {
      int lastUpdated;
      Object lastUpdatedObject = clazz.getMethod("getLastUpdated").invoke(data);

      if (lastUpdatedObject instanceof Date) {
        lastUpdated = (int) ((Date) lastUpdatedObject).getTime() / 1000;
      } else {
        lastUpdated = (int) lastUpdatedObject;
      }

      Integer ttl = (Integer) clazz.getMethod("getTtl").invoke(data);

      maxAge = getTtl(now, lastUpdated, ttl, minimumTtl);
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
      int lastUpdated;
      Object lastUpdatedObject = clazz.getMethod("getLastUpdated").invoke(data);

      if (lastUpdatedObject instanceof Date) {
        lastUpdated = (int) ((Date) lastUpdatedObject).getTime() / 1000;
      } else {
        lastUpdated = (int) lastUpdatedObject;
      }

      return lastUpdated * 1000L;
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

  public static String generateETag(Object data, String systemId, String feed) {
    try {
      Object lastUpdated = data.getClass().getMethod("getLastUpdated").invoke(data);
      String lastUpdatedStr;

      if (lastUpdated instanceof Date) {
        lastUpdatedStr = String.valueOf(((Date) lastUpdated).getTime() / 1000);
      } else {
        // Handle both Integer (v2) and String (v3 ISO timestamp)
        lastUpdatedStr = String.valueOf(lastUpdated);
      }

      String content = systemId + "-" + feed + "-" + lastUpdatedStr;
      return "\"" + DigestUtils.md5DigestAsHex(content.getBytes()) + "\"";
    } catch (
      IllegalAccessException | InvocationTargetException | NoSuchMethodException e
    ) {
      logger.warn("Unable to generate ETag for systemId={} feed={}", systemId, feed, e);
      return (
        "\"" +
        DigestUtils.md5DigestAsHex(
          (systemId + "-" + feed + "-" + System.currentTimeMillis()).getBytes()
        ) +
        "\""
      );
    }
  }
}
