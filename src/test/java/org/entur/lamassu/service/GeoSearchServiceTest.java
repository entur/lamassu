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

import org.entur.lamassu.cache.StationCache;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.impl.GeoSearchServiceImpl;
import org.entur.lamassu.stubs.VehicleCacheStub;
import org.entur.lamassu.util.SpatialIndexIdUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GeoSearchServiceTest {

    private final VehicleSpatialIndex vehicleSpatialIndex = mock(VehicleSpatialIndex.class);
    private final StationSpatialIndex stationSpatialIndex = mock(StationSpatialIndex.class);
    private final VehicleCache vehicleCache = new VehicleCacheStub();
    private final StationCache stationCache = mock(StationCache.class);
    private final FeedProvider feedProvider = getFeedProvider();
    private final GeoSearchService service = new GeoSearchServiceImpl(
        vehicleSpatialIndex,
        stationSpatialIndex,
        vehicleCache,
        stationCache
    );

    @Before
    public void setup() {
        var vehicles = new ArrayList<Vehicle>();

        for (PrimitiveIterator.OfInt it = IntStream.range(0, 10).iterator(); it.hasNext(); ) {
            vehicles.add(getVehicle(it.next()));
        }

        vehicleCache.updateAll(vehicles.stream().collect(Collectors.toMap(Vehicle::getId, v -> v)), 0, TimeUnit.SECONDS);

        when(vehicleSpatialIndex.getAll()).thenReturn(
                vehicles.stream()
                        .map(vehicle -> SpatialIndexIdUtil.createVehicleSpatialIndexId(vehicle, feedProvider))
                        .collect(Collectors.toList())
        );
    }

    @Test
    public void sanityCheck() {
        Assertions.assertTrue(vehicleCache.hasKey("foo_1"));
    }

    @Test
    public void testGetVehicleOrphansIsEmpty() {
        var orphans = service.getVehicleSpatialIndexOrphans();
        Assertions.assertTrue(orphans.isEmpty());
    }

    @Test
    public void testGetVehicleOrphansReturnsOrphanWhenRemovingVehicleFromCache() {
        vehicleCache.removeAll(Set.of("foo_1"));
        var orphans = service.getVehicleSpatialIndexOrphans();
        Assertions.assertEquals(1, orphans.size());
        Assertions.assertTrue(orphans.contains("foo_1"));
    }

    @Test
    public void testRemoveVehicleSpatialIndexOrphans() {
        var vehicleToRemove = vehicleCache.get("foo_1");
        vehicleCache.removeAll(Set.of("foo_1"));
        var orphans = service.removeVehicleSpatialIndexOrphans();
        verify(vehicleSpatialIndex).removeAll(Set.of(SpatialIndexIdUtil.createVehicleSpatialIndexId(vehicleToRemove, feedProvider)));
    }

    private Vehicle getVehicle(int i) {
        var vehicle = new Vehicle();
        vehicle.setId("foo_" + i);
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
