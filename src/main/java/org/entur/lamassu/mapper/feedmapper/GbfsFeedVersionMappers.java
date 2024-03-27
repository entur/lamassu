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

import org.entur.gbfs.loader.v2.GbfsV2Delivery;
import org.entur.gbfs.loader.v3.GbfsV3Delivery;
import org.entur.gbfs.mapper.GBFSMapper;

/**
 * Static wrappers around gbfs-mapper-java for mapping all feeds in a Gbfs delivery instance
 */
public class GbfsFeedVersionMappers {

  /**
   * Map from v3 to v2
   */
  public static GbfsV2Delivery map(GbfsV3Delivery source, String languageCode) {
    return new GbfsV2Delivery(
      GBFSMapper.INSTANCE.map(source.discovery(), languageCode),
      // TODO since we now produce v2.x and v3.x we can generate the versions feed
      null,
      GBFSMapper.INSTANCE.map(source.systemInformation(), languageCode),
      GBFSMapper.INSTANCE.map(source.vehicleTypes(), languageCode),
      GBFSMapper.INSTANCE.map(source.stationInformation(), languageCode),
      GBFSMapper.INSTANCE.map(source.stationStatus(), languageCode),
      GBFSMapper.INSTANCE.map(source.vehicleStatus(), languageCode),
      null,
      null,
      GBFSMapper.INSTANCE.map(source.systemRegions(), languageCode),
      GBFSMapper.INSTANCE.map(source.systemPricingPlans(), languageCode),
      GBFSMapper.INSTANCE.map(source.systemAlerts(), languageCode),
      GBFSMapper.INSTANCE.map(source.geofencingZones(), languageCode),
      source.validationResult()
    );
  }

  /**
   * Map from v2 to v3
   */
  public static GbfsV3Delivery map(GbfsV2Delivery source, String languageCode) {
    return new GbfsV3Delivery(
      GBFSMapper.INSTANCE.map(source.discovery(), languageCode),
      // TODO since we now produce v2.x and v3.x we can generate the versions feed
      null,
      GBFSMapper.INSTANCE.map(source.systemInformation(), languageCode),
      GBFSMapper.INSTANCE.map(source.vehicleTypes(), languageCode),
      GBFSMapper.INSTANCE.map(source.stationInformation(), languageCode),
      GBFSMapper.INSTANCE.map(source.stationStatus(), languageCode),
      GBFSMapper.INSTANCE.map(source.freeBikeStatus(), languageCode),
      GBFSMapper.INSTANCE.map(source.systemRegions(), languageCode),
      GBFSMapper.INSTANCE.map(source.systemPricingPlans(), languageCode),
      GBFSMapper.INSTANCE.map(source.systemAlerts(), languageCode),
      GBFSMapper.INSTANCE.map(source.geofencingZones(), languageCode),
      source.validationResult()
    );
  }
}
