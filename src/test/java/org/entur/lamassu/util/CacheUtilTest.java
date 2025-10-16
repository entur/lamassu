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

  @Test
  void generateETagWithV2IntegerTimestamp() {
    var etag = CacheUtil.generateETag(
      new GBFS().withLastUpdated(now),
      "test-system",
      "gbfs"
    );

    Assertions.assertNotNull(etag);
    Assertions.assertTrue(etag.startsWith("\""));
    Assertions.assertTrue(etag.endsWith("\""));
    Assertions.assertEquals(34, etag.length()); // 32 hex chars + 2 quotes
  }

  @Test
  void generateETagWithV3DateTimestamp() {
    // V3 uses Date for lastUpdated
    var v3Data = new org.mobilitydata.gbfs.v3_0.gbfs.GBFSGbfs()
      .withLastUpdated(new java.util.Date(now * 1000L));

    var etag = CacheUtil.generateETag(v3Data, "test-system", "gbfs");

    Assertions.assertNotNull(etag);
    Assertions.assertTrue(etag.startsWith("\""));
    Assertions.assertTrue(etag.endsWith("\""));
    Assertions.assertEquals(34, etag.length());
  }

  @Test
  void generateETagIsDeterministic() {
    var data = new GBFS().withLastUpdated(now);
    var etag1 = CacheUtil.generateETag(data, "system", "feed");
    var etag2 = CacheUtil.generateETag(data, "system", "feed");

    Assertions.assertEquals(etag1, etag2);
  }

  @Test
  void generateETagChangesWithDifferentTimestamp() {
    var data1 = new GBFS().withLastUpdated(now);
    var data2 = new GBFS().withLastUpdated(now + 60);

    var etag1 = CacheUtil.generateETag(data1, "system", "feed");
    var etag2 = CacheUtil.generateETag(data2, "system", "feed");

    Assertions.assertNotEquals(etag1, etag2);
  }

  @Test
  void generateETagChangesWithDifferentSystemId() {
    var data = new GBFS().withLastUpdated(now);

    var etag1 = CacheUtil.generateETag(data, "system1", "feed");
    var etag2 = CacheUtil.generateETag(data, "system2", "feed");

    Assertions.assertNotEquals(etag1, etag2);
  }

  @Test
  void generateETagChangesWithDifferentFeed() {
    var data = new GBFS().withLastUpdated(now);

    var etag1 = CacheUtil.generateETag(data, "system", "feed1");
    var etag2 = CacheUtil.generateETag(data, "system", "feed2");

    Assertions.assertNotEquals(etag1, etag2);
  }

  @Test
  void generateETagWithNullLastUpdatedDoesNotThrow() {
    Assertions.assertDoesNotThrow(() ->
      CacheUtil.generateETag(new GBFS(), "system", "feed")
    );
  }
}
