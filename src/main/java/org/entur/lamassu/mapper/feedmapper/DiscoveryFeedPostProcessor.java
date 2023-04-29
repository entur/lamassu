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

import org.entur.gbfs.GbfsDelivery;
import org.entur.gbfs.v2_3.gbfs.GBFS;

import java.util.stream.Collectors;

public class DiscoveryFeedPostProcessor {
    public static void removeUnavailableFiles(GBFS discovery, GbfsDelivery mapped) {
        discovery.getFeedsData().keySet().forEach(language -> {
            var feeds = discovery.getFeedsData().get(language).getFeeds().stream().filter(feed -> {
                switch (feed.getName()) {
                    case SystemInformation:
                        return mapped.getSystemInformation() != null;
                    case VehicleTypes:
                        return mapped.getVehicleTypes() != null;
                    case FreeBikeStatus:
                        return mapped.getFreeBikeStatus() != null;
                    case StationInformation:
                        return mapped.getStationInformation() != null;
                    case StationStatus:
                        return mapped.getStationStatus() != null;
                    case SystemPricingPlans:
                        return mapped.getSystemPricingPlans() != null;
                    case SystemAlerts:
                        return mapped.getSystemAlerts() != null;
                    case SystemHours:
                        return mapped.getSystemHours() !=  null;
                    case SystemCalendar:
                        return mapped.getSystemCalendar() != null;
                    case SystemRegions:
                        return mapped.getSystemRegions() != null;
                    case GeofencingZones:
                        return mapped.getGeofencingZones() != null;
                    default:
                        return false;
                }
            }).collect(Collectors.toList());
            discovery.getFeedsData().get(language).setFeeds(feeds);
        });


    }
}
