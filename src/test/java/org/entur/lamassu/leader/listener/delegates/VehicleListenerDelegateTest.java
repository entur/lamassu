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

package org.entur.lamassu.leader.listener.delegates;

import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.util.SpatialIndexIdUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.map.event.EntryEvent;

import java.util.Set;

import static org.mockito.Mockito.when;

class VehicleListenerDelegateTest {
    VehicleSpatialIndex mockIndex = Mockito.mock(VehicleSpatialIndex.class);
    FeedProviderService mockFeedProviderService = Mockito.mock(FeedProviderService.class);


    @Test
    void testExpiry() {
        FeedProvider feedProvider = getFeedProvider();
        Vehicle vehicle = getVehicle();

        when(mockFeedProviderService.getFeedProviderBySystemId(feedProvider.getSystemId())).thenReturn(feedProvider);

        EntryEvent<String, Vehicle> event = new EntryEvent<>(null, EntryEvent.Type.EXPIRED, "foo_bar", vehicle, null);

        var subject = new VehicleListenerDelegate(mockFeedProviderService, mockIndex);
        subject.onExpired(event);

        var expectedId = SpatialIndexIdUtil.createVehicleSpatialIndexId(vehicle, feedProvider);
        Mockito.verify(mockIndex).removeAll(Set.of(expectedId));

    }

    private Vehicle getVehicle() {
        var vehicle = new Vehicle();
        vehicle.setId("foo");
        var vehicleType = new VehicleType();
        vehicleType.setFormFactor(FormFactor.SCOOTER);
        vehicleType.setPropulsionType(PropulsionType.ELECTRIC);
        vehicle.setVehicleType(vehicleType);
        vehicle.setReserved(false);
        vehicle.setDisabled(false);
        return vehicle;
    }

    private FeedProvider getFeedProvider() {
        var feedProvider = new FeedProvider();
        feedProvider.setSystemId("bar");
        return feedProvider;
    }


}
