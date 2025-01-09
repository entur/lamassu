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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFS;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;

class CacheUtilTest {

  // 2021-12-20T11:33:20Z
  private final int now = 1640000000;

  @Test
  void getTtlReturnsMinimumTtlWhenExpired() {
    int expected = 3600;
    Assertions.assertEquals(expected, CacheUtil.getTtl(now, now - 3600, 10, expected));
  }

  @Test
  void getTtlReturnsMinimumTtlWhenLessThanMinimumTtl() {
    int expected = 3600;
    Assertions.assertEquals(expected, CacheUtil.getTtl(now, now - 20, 10, expected));
  }

  @Test
  void getTtlReturnsCalculatedTtlWhenLargerThanMinimumTtl() {
    Assertions.assertEquals(20, CacheUtil.getTtl(now, now - 10, 30, 10));
  }

  @Test
  void getMaxAgeWorks() {
    Assertions.assertEquals(
      60,
      CacheUtil.getMaxAge(
        GBFSFeedName.GBFS.implementingClass(),
        new GBFS().withLastUpdated(1640000000 - 60).withTtl(120),
        null,
        null,
        now
      )
    );
  }

  @Test
  void getMaxAgeReturnsZeroWhenCalculatedTtlIsInThePast() {
    Assertions.assertEquals(
      0,
      CacheUtil.getMaxAge(
        GBFSFeedName.GBFS.implementingClass(),
        new GBFS().withLastUpdated(1540000000).withTtl(60),
        null,
        null,
        now
      )
    );
  }

  @Test
  void getMaxAgeDoesNotThrowWithNullLastUpdated() {
    Assertions.assertDoesNotThrow(() ->
      CacheUtil.getMaxAge(
        GBFSFeedName.GBFS.implementingClass(),
        new GBFS().withTtl(60),
        null,
        null,
        now
      )
    );
  }

  @Test
  void getLastModifiedWorks() {
    Assertions.assertEquals(
      now * 1000L,
      CacheUtil.getLastModified(
        GBFSFeedName.GBFS.implementingClass(),
        new GBFS().withLastUpdated(now),
        null,
        null
      )
    );
  }

  @Test
  void getLastModifiedWithNullLastUpdatedDoesNotThrow() {
    Assertions.assertDoesNotThrow(() ->
      CacheUtil.getLastModified(
        GBFSFeedName.GBFS.implementingClass(),
        new GBFS(),
        null,
        null
      )
    );
  }
}
