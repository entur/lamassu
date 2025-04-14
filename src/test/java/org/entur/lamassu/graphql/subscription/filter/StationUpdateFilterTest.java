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

package org.entur.lamassu.graphql.subscription.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import org.entur.lamassu.graphql.subscription.model.StationUpdate;
import org.entur.lamassu.graphql.subscription.model.UpdateType;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.entities.VehicleTypeAvailability;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.StationFilterParameters;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the StationUpdateFilter class.
 * Tests filtering logic for station updates based on various criteria.
 */
public class StationUpdateFilterTest {

  private static final String TEST_CODESPACE = "TST";
  private static final String TEST_SYSTEM_ID = "test-system";
  private static final UnaryOperator<String> CODESPACE_RESOLVER = systemId -> {
    // Return different codespaces based on the systemId
    if ("other-system".equals(systemId)) {
      return "OTHER";
    }
    return TEST_CODESPACE;
  };

  @Test
  void testFilterByBoundingBox() {
    // Create filter parameters
    StationFilterParameters filterParams = new StationFilterParameters(
      null, // codespaces
      null, // systems
      null, // operators
      null, // count
      null, // availableFormFactors
      null // availablePropulsionTypes
    );

    // Create bounding box parameters for Oslo area
    BoundingBoxQueryParameters bboxParams = new BoundingBoxQueryParameters(
      59.9,
      10.7,
      59.95,
      10.8
    );

    StationUpdateFilter filter = new StationUpdateFilter(
      filterParams,
      bboxParams,
      CODESPACE_RESOLVER
    );

    // Create test updates inside and outside the bounding box
    StationUpdate insideUpdate = createStationUpdate(59.92, 10.75); // Inside bbox
    StationUpdate outsideUpdate = createStationUpdate(60.0, 11.0); // Outside bbox

    // Assert
    assertTrue(filter.test(insideUpdate), "Station inside bbox should pass filter");
    assertFalse(
      filter.test(outsideUpdate),
      "Station outside bbox should not pass filter"
    );
  }

  @Test
  void testFilterByRange() {
    // Create filter parameters
    StationFilterParameters filterParams = new StationFilterParameters(
      null, // codespaces
      null, // systems
      null, // operators
      null, // count
      null, // availableFormFactors
      null // availablePropulsionTypes
    );

    // Center at Oslo Central Station with 1km range
    RangeQueryParameters rangeParams = new RangeQueryParameters(59.911, 10.753, 1000.0); // 1000.0 meters

    StationUpdateFilter filter = new StationUpdateFilter(
      filterParams,
      rangeParams,
      CODESPACE_RESOLVER
    );

    // Create test updates inside and outside the range
    StationUpdate insideUpdate = createStationUpdate(59.912, 10.755); // ~200m away
    StationUpdate outsideUpdate = createStationUpdate(59.950, 10.850); // ~5km away

    // Assert
    assertTrue(filter.test(insideUpdate), "Station inside range should pass filter");
    assertFalse(
      filter.test(outsideUpdate),
      "Station outside range should not pass filter"
    );
  }

  @Test
  void testFilterByCodespace() {
    // Create filter parameters with codespace
    StationFilterParameters filterParams = new StationFilterParameters(
      List.of(TEST_CODESPACE), // codespaces
      null, // systems
      null, // operators
      null, // count
      null, // availableFormFactors
      null // availablePropulsionTypes
    );

    // Create range parameters
    RangeQueryParameters rangeParams = new RangeQueryParameters(59.911, 10.753, 1000.0);

    StationUpdateFilter filter = new StationUpdateFilter(
      filterParams,
      rangeParams,
      CODESPACE_RESOLVER
    );

    // Create test updates with matching and non-matching codespace
    StationUpdate matchingUpdate = createStationUpdate(59.912, 10.755); // Has TEST_CODESPACE
    StationUpdate nonMatchingUpdate = createStationUpdateWithDifferentCodespace(
      59.912,
      10.755,
      "OTHER"
    );

    // Assert
    assertTrue(
      filter.test(matchingUpdate),
      "Station with matching codespace should pass filter"
    );
    assertFalse(
      filter.test(nonMatchingUpdate),
      "Station with non-matching codespace should not pass filter"
    );
  }

  @Test
  void testFilterByFormFactor() {
    // Create filter parameters with form factor
    StationFilterParameters filterParams = new StationFilterParameters(
      null, // codespaces
      null, // systems
      null, // operators
      null, // count
      Arrays.asList(FormFactor.BICYCLE), // availableFormFactors
      null // availablePropulsionTypes
    );

    // Create range parameters
    RangeQueryParameters rangeParams = new RangeQueryParameters(59.911, 10.753, 1000.0);

    StationUpdateFilter filter = new StationUpdateFilter(
      filterParams,
      rangeParams,
      CODESPACE_RESOLVER
    );

    // Create test updates with matching and non-matching form factors
    StationUpdate matchingUpdate = createStationUpdateWithFormFactor(
      59.912,
      10.755,
      FormFactor.BICYCLE
    );
    StationUpdate nonMatchingUpdate = createStationUpdateWithFormFactor(
      59.912,
      10.755,
      FormFactor.SCOOTER
    );

    // Assert
    assertTrue(
      filter.test(matchingUpdate),
      "Station with matching form factor should pass filter"
    );
    assertFalse(
      filter.test(nonMatchingUpdate),
      "Station with non-matching form factor should not pass filter"
    );
  }

  @Test
  void testFilterByPropulsionType() {
    // Create filter parameters with propulsion type
    StationFilterParameters filterParams = new StationFilterParameters(
      null, // codespaces
      null, // systems
      null, // operators
      null, // count
      null, // availableFormFactors
      Arrays.asList(PropulsionType.ELECTRIC) // availablePropulsionTypes
    );

    // Create range parameters
    RangeQueryParameters rangeParams = new RangeQueryParameters(59.911, 10.753, 1000.0);

    StationUpdateFilter filter = new StationUpdateFilter(
      filterParams,
      rangeParams,
      CODESPACE_RESOLVER
    );

    // Create test updates with matching and non-matching propulsion types
    StationUpdate matchingUpdate = createStationUpdateWithPropulsion(
      59.912,
      10.755,
      PropulsionType.ELECTRIC
    );
    StationUpdate nonMatchingUpdate = createStationUpdateWithPropulsion(
      59.912,
      10.755,
      PropulsionType.COMBUSTION
    );

    // Assert
    assertTrue(
      filter.test(matchingUpdate),
      "Station with matching propulsion type should pass filter"
    );
    assertFalse(
      filter.test(nonMatchingUpdate),
      "Station with non-matching propulsion type should not pass filter"
    );
  }

  @Test
  void testNullStationUpdate() {
    // Create filter parameters
    StationFilterParameters filterParams = new StationFilterParameters(
      null, // codespaces
      null, // systems
      null, // operators
      null, // count
      null, // availableFormFactors
      null // availablePropulsionTypes
    );

    // Create range parameters
    RangeQueryParameters rangeParams = new RangeQueryParameters(59.911, 10.753, 1000.0);

    StationUpdateFilter filter = new StationUpdateFilter(
      filterParams,
      rangeParams,
      CODESPACE_RESOLVER
    );

    // Assert
    assertFalse(filter.test(null), "Null update should not pass filter");
    assertFalse(
      filter.test(new StationUpdate("id", UpdateType.CREATE, null)),
      "Update with null station should not pass filter"
    );
  }

  // Helper methods to create test data
  private StationUpdate createStationUpdate(double lat, double lon) {
    Station station = createStation(lat, lon);
    return new StationUpdate(station.getId(), UpdateType.CREATE, station);
  }

  private Station createStation(double lat, double lon) {
    Station station = new Station();
    station.setId("TST:Station:test");
    station.setSystemId(TEST_SYSTEM_ID);
    station.setLat(lat);
    station.setLon(lon);

    // Set up system
    System system = new System();
    system.setId(TEST_SYSTEM_ID);
    system.setOperator(new org.entur.lamassu.model.entities.Operator());
    station.setSystem(system);

    // Add default vehicle type availability
    List<VehicleTypeAvailability> vehicleTypesAvailable = new ArrayList<>();
    VehicleTypeAvailability availability = new VehicleTypeAvailability();
    VehicleType vehicleType = new VehicleType();
    vehicleType.setFormFactor(FormFactor.BICYCLE);
    vehicleType.setPropulsionType(PropulsionType.ELECTRIC);
    availability.setVehicleType(vehicleType);
    availability.setCount(10);
    vehicleTypesAvailable.add(availability);
    station.setVehicleTypesAvailable(vehicleTypesAvailable);

    return station;
  }

  private StationUpdate createStationUpdateWithDifferentCodespace(
    double lat,
    double lon,
    String codespace
  ) {
    Station station = createStation(lat, lon);
    // Change the ID to use a different codespace
    station.setId(codespace + ":Station:test");

    // Create a new system with a different system ID
    String differentSystemId = "other-system";
    station.setSystemId(differentSystemId);

    // Set up system with the different system ID
    System system = new System();
    system.setId(differentSystemId);
    system.setOperator(new org.entur.lamassu.model.entities.Operator());
    station.setSystem(system);

    return new StationUpdate(station.getId(), UpdateType.CREATE, station);
  }

  private StationUpdate createStationUpdateWithFormFactor(
    double lat,
    double lon,
    FormFactor formFactor
  ) {
    Station station = createStation(lat, lon);
    VehicleType vehicleType = new VehicleType();
    vehicleType.setFormFactor(formFactor);
    vehicleType.setPropulsionType(PropulsionType.ELECTRIC);

    VehicleTypeAvailability availability = new VehicleTypeAvailability();
    availability.setVehicleType(vehicleType);
    availability.setCount(10);

    List<VehicleTypeAvailability> vehicleTypesAvailable = new ArrayList<>();
    vehicleTypesAvailable.add(availability);
    station.setVehicleTypesAvailable(vehicleTypesAvailable);

    return new StationUpdate(station.getId(), UpdateType.CREATE, station);
  }

  private StationUpdate createStationUpdateWithPropulsion(
    double lat,
    double lon,
    PropulsionType propulsionType
  ) {
    Station station = createStation(lat, lon);
    VehicleType vehicleType = new VehicleType();
    vehicleType.setFormFactor(FormFactor.BICYCLE);
    vehicleType.setPropulsionType(propulsionType);

    VehicleTypeAvailability availability = new VehicleTypeAvailability();
    availability.setVehicleType(vehicleType);
    availability.setCount(10);

    List<VehicleTypeAvailability> vehicleTypesAvailable = new ArrayList<>();
    vehicleTypesAvailable.add(availability);
    station.setVehicleTypesAvailable(vehicleTypesAvailable);

    return new StationUpdate(station.getId(), UpdateType.CREATE, station);
  }
}
