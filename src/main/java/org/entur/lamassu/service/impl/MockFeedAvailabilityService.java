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

package org.entur.lamassu.service.impl;

import org.entur.gbfs.v2_3.gbfs.GBFSFeedName;
import org.entur.lamassu.service.FeedAvailabilityService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This mock service is only to demonstrate functionality. A service based on redis should be implemented
 * before merging.
 */
@Component
public class MockFeedAvailabilityService implements FeedAvailabilityService {
    private final ConcurrentHashMap<String, List<GBFSFeedName>> availableFilesPerSystem = new ConcurrentHashMap<>();

    @Override
    public Map<String, List<GBFSFeedName>> getAvailableFeeds() {
        return availableFilesPerSystem;
    }

    @Override
    public List<GBFSFeedName> getAvailableFiles(String systemId) {
        return availableFilesPerSystem.get(systemId);
    }

    @Override
    public void setAvailableFiles(String systemId, List<GBFSFeedName> files) {
        var minimumRequired = files.containsAll(List.of(GBFSFeedName.SystemInformation, GBFSFeedName.VehicleTypes, GBFSFeedName.SystemPricingPlans));
        var freeFloating = files.contains(GBFSFeedName.FreeBikeStatus);
        var stationBased = files.containsAll(List.of(GBFSFeedName.StationInformation, GBFSFeedName.StationStatus));

        if (minimumRequired && (freeFloating || stationBased)) {
            availableFilesPerSystem.put(systemId, files);
        } else {
            availableFilesPerSystem.remove(systemId);
        }
    }
}
