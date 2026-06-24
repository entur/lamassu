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

package org.entur.lamassu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStationStatus;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicleStatus;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FeedFreshnessServiceTest {

  private static final int MAX_TOLERATED_OVERDUE_SECONDS = 120;

  @Mock
  private GBFSV3FeedCache feedCache;

  private FeedFreshnessService service;
  private FeedProvider provider;

  @BeforeEach
  void setUp() {
    service = new FeedFreshnessService(feedCache, MAX_TOLERATED_OVERDUE_SECONDS);
    provider = new FeedProvider();
    provider.setSystemId("boltoslo");
  }

  private GBFSVehicleStatus vehicleStatus(long lastUpdatedEpochSec, int ttl) {
    return new GBFSVehicleStatus()
      .withLastUpdated(new Date(lastUpdatedEpochSec * 1000))
      .withTtl(ttl);
  }

  private GBFSStationStatus stationStatus(long lastUpdatedEpochSec, int ttl) {
    return new GBFSStationStatus()
      .withLastUpdated(new Date(lastUpdatedEpochSec * 1000))
      .withTtl(ttl);
  }

  private long now() {
    return Instant.now().getEpochSecond();
  }

  @Test
  void isLive_returnsTrue_whenVehicleStatusIsFresh() {
    when(feedCache.find(eq(GBFSFeed.Name.VEHICLE_STATUS), any()))
      .thenReturn(vehicleStatus(now(), 60));

    assertTrue(service.isLive(provider));
  }

  @Test
  void isLive_returnsTrue_whenStationStatusIsFresh() {
    when(feedCache.find(eq(GBFSFeed.Name.STATION_STATUS), any()))
      .thenReturn(stationStatus(now(), 60));

    assertTrue(service.isLive(provider));
  }

  @Test
  void isLive_returnsFalse_whenVehicleStatusIsOverdue() {
    when(feedCache.find(eq(GBFSFeed.Name.VEHICLE_STATUS), any()))
      .thenReturn(vehicleStatus(now() - 1000, 60));

    assertFalse(service.isLive(provider));
  }

  @Test
  void isLive_returnsFalse_whenNoRealtimeFeedIsPresent() {
    assertFalse(service.isLive(provider));
  }

  @Test
  void lastUpdated_returnsVehicleStatusTimestamp_whenPresent() {
    long ts = now() - 30;
    when(feedCache.find(eq(GBFSFeed.Name.VEHICLE_STATUS), any()))
      .thenReturn(vehicleStatus(ts, 60));

    assertEquals(Optional.of(Instant.ofEpochSecond(ts)), service.lastUpdated(provider));
  }

  @Test
  void lastUpdated_returnsEmpty_whenNoRealtimeFeedIsPresent() {
    assertEquals(Optional.empty(), service.lastUpdated(provider));
  }

  @Test
  void isFeedOverdue_returnsFalse_whenFeedIsAbsent() {
    assertFalse(service.isFeedOverdue(provider, GBFSFeed.Name.VEHICLE_STATUS));
  }

  @Test
  void isFeedOverdue_returnsTrue_whenPastTtlPlusTolerance() {
    when(feedCache.find(eq(GBFSFeed.Name.VEHICLE_STATUS), any()))
      .thenReturn(vehicleStatus(now() - 1000, 60));

    assertTrue(service.isFeedOverdue(provider, GBFSFeed.Name.VEHICLE_STATUS));
  }
}
