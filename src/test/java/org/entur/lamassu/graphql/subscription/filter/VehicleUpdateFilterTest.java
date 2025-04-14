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

import java.util.List;
import java.util.function.UnaryOperator;
import org.entur.lamassu.graphql.subscription.model.UpdateType;
import org.entur.lamassu.graphql.subscription.model.VehicleUpdate;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the VehicleUpdateFilter class.
 * Tests filtering logic for vehicle updates based on various criteria.
 */
class VehicleUpdateFilterTest {

  private static final String TEST_CODESPACE = "TST";
  private static final String TEST_SYSTEM_ID = "test-system";
  private static final String TEST_OPERATOR_ID = "test-operator";
  private static final UnaryOperator<String> CODESPACE_RESOLVER = systemId ->
    TEST_CODESPACE;

  @Test
  void testFilterByBoundingBox() {
    // Arrange
    VehicleFilterParameters filterParams = new VehicleFilterParameters(
      List.of(TEST_CODESPACE),
      null,
      null,
      null,
      null,
      null,
      false,
      false
    );

    BoundingBoxQueryParameters bbox = new BoundingBoxQueryParameters(
      59.0,
      10.0,
      60.0,
      11.0
    );

    VehicleUpdateFilter filter = new VehicleUpdateFilter(
      filterParams,
      bbox,
      CODESPACE_RESOLVER
    );

    // Create test updates inside and outside the bounding box
    VehicleUpdate insideUpdate = createVehicleUpdate(59.5, 10.5);
    VehicleUpdate outsideUpdate = createVehicleUpdate(58.5, 9.5);

    // Assert
    assertTrue(
      filter.test(insideUpdate),
      "Vehicle inside bounding box should pass filter"
    );
    assertFalse(
      filter.test(outsideUpdate),
      "Vehicle outside bounding box should not pass filter"
    );
  }

  @Test
  void testFilterByRange() {
    // Arrange
    VehicleFilterParameters filterParams = new VehicleFilterParameters(
      List.of(TEST_CODESPACE),
      null,
      null,
      null,
      null,
      null,
      false,
      false
    );

    // Center at Oslo Central Station with 1km range
    RangeQueryParameters rangeParams = new RangeQueryParameters(59.911, 10.753, 1000.0); // 1000.0 meters

    VehicleUpdateFilter filter = new VehicleUpdateFilter(
      filterParams,
      rangeParams,
      CODESPACE_RESOLVER
    );

    // Create test updates inside and outside the range
    VehicleUpdate insideUpdate = createVehicleUpdate(59.912, 10.755); // ~200m away
    VehicleUpdate outsideUpdate = createVehicleUpdate(59.950, 10.850); // ~5km away

    // Assert
    assertTrue(filter.test(insideUpdate), "Vehicle inside range should pass filter");
    assertFalse(
      filter.test(outsideUpdate),
      "Vehicle outside range should not pass filter"
    );
  }

  @Test
  void testFilterByCodespace() {
    // Arrange
    VehicleFilterParameters filterParams = new VehicleFilterParameters(
      List.of("TST"),
      null,
      null,
      null,
      null,
      null,
      false,
      false
    );

    BoundingBoxQueryParameters bbox = new BoundingBoxQueryParameters(
      59.0,
      10.0,
      60.0,
      11.0
    );

    UnaryOperator<String> codespaceResolver = systemId -> {
      if (TEST_SYSTEM_ID.equals(systemId)) {
        return "TST";
      } else {
        return "OTHER";
      }
    };

    VehicleUpdateFilter filter = new VehicleUpdateFilter(
      filterParams,
      bbox,
      codespaceResolver
    );

    // Create test updates with matching and non-matching codespaces
    VehicleUpdate matchingUpdate = createVehicleUpdate(59.5, 10.5);

    Vehicle nonMatchingVehicle = createVehicle(59.5, 10.5);
    nonMatchingVehicle.setSystemId("other-system");
    VehicleUpdate nonMatchingUpdate = new VehicleUpdate(
      "other-id",
      UpdateType.CREATE,
      nonMatchingVehicle
    );

    // Assert
    assertTrue(
      filter.test(matchingUpdate),
      "Vehicle with matching codespace should pass filter"
    );
    assertFalse(
      filter.test(nonMatchingUpdate),
      "Vehicle with non-matching codespace should not pass filter"
    );
  }

  @Test
  void testFilterByFormFactor() {
    // Arrange
    VehicleFilterParameters filterParams = new VehicleFilterParameters(
      null,
      null,
      null,
      null,
      List.of(FormFactor.SCOOTER),
      null,
      false,
      false
    );

    BoundingBoxQueryParameters bbox = new BoundingBoxQueryParameters(
      59.0,
      10.0,
      60.0,
      11.0
    );

    VehicleUpdateFilter filter = new VehicleUpdateFilter(
      filterParams,
      bbox,
      CODESPACE_RESOLVER
    );

    // Create test updates with matching and non-matching form factors
    VehicleUpdate scooterUpdate = createVehicleUpdateWithFormFactor(
      59.5,
      10.5,
      FormFactor.SCOOTER
    );
    VehicleUpdate bikeUpdate = createVehicleUpdateWithFormFactor(
      59.5,
      10.5,
      FormFactor.BICYCLE
    );

    // Assert
    assertTrue(filter.test(scooterUpdate), "Scooter should pass form factor filter");
    assertFalse(
      filter.test(bikeUpdate),
      "Bicycle should not pass scooter form factor filter"
    );
  }

  @Test
  void testFilterByPropulsionType() {
    // Arrange
    VehicleFilterParameters filterParams = new VehicleFilterParameters(
      null,
      null,
      null,
      null,
      null,
      List.of(PropulsionType.ELECTRIC),
      false,
      false
    );

    BoundingBoxQueryParameters bbox = new BoundingBoxQueryParameters(
      59.0,
      10.0,
      60.0,
      11.0
    );

    VehicleUpdateFilter filter = new VehicleUpdateFilter(
      filterParams,
      bbox,
      CODESPACE_RESOLVER
    );

    // Create test updates with matching and non-matching propulsion types
    VehicleUpdate electricUpdate = createVehicleUpdateWithPropulsion(
      59.5,
      10.5,
      PropulsionType.ELECTRIC
    );
    VehicleUpdate humanUpdate = createVehicleUpdateWithPropulsion(
      59.5,
      10.5,
      PropulsionType.HUMAN
    );

    // Assert
    assertTrue(
      filter.test(electricUpdate),
      "Electric vehicle should pass propulsion filter"
    );
    assertFalse(
      filter.test(humanUpdate),
      "Human-powered vehicle should not pass electric propulsion filter"
    );
  }

  @Test
  void testFilterByReservedStatus() {
    // Arrange - don't include reserved vehicles
    VehicleFilterParameters filterParams = new VehicleFilterParameters(
      null,
      null,
      null,
      null,
      null,
      null,
      false,
      true
    );

    BoundingBoxQueryParameters bbox = new BoundingBoxQueryParameters(
      59.0,
      10.0,
      60.0,
      11.0
    );

    VehicleUpdateFilter filter = new VehicleUpdateFilter(
      filterParams,
      bbox,
      CODESPACE_RESOLVER
    );

    // Create test updates with reserved and non-reserved vehicles
    VehicleUpdate reservedUpdate = createVehicleUpdateWithReservedStatus(
      59.5,
      10.5,
      true
    );
    VehicleUpdate availableUpdate = createVehicleUpdateWithReservedStatus(
      59.5,
      10.5,
      false
    );

    // Assert
    assertFalse(
      filter.test(reservedUpdate),
      "Reserved vehicle should not pass filter when includeReserved is false"
    );
    assertTrue(filter.test(availableUpdate), "Available vehicle should pass filter");

    // Now test with includeReserved = true
    VehicleFilterParameters includeReservedParams = new VehicleFilterParameters(
      null,
      null,
      null,
      null,
      null,
      null,
      true,
      true
    );

    VehicleUpdateFilter includeReservedFilter = new VehicleUpdateFilter(
      includeReservedParams,
      bbox,
      CODESPACE_RESOLVER
    );

    assertTrue(
      includeReservedFilter.test(reservedUpdate),
      "Reserved vehicle should pass filter when includeReserved is true"
    );
  }

  @Test
  void testFilterByDisabledStatus() {
    // Arrange - don't include disabled vehicles
    VehicleFilterParameters filterParams = new VehicleFilterParameters(
      null,
      null,
      null,
      null,
      null,
      null,
      true,
      false
    );

    BoundingBoxQueryParameters bbox = new BoundingBoxQueryParameters(
      59.0,
      10.0,
      60.0,
      11.0
    );

    VehicleUpdateFilter filter = new VehicleUpdateFilter(
      filterParams,
      bbox,
      CODESPACE_RESOLVER
    );

    // Create test updates with disabled and enabled vehicles
    VehicleUpdate disabledUpdate = createVehicleUpdateWithDisabledStatus(
      59.5,
      10.5,
      true
    );
    VehicleUpdate enabledUpdate = createVehicleUpdateWithDisabledStatus(
      59.5,
      10.5,
      false
    );

    // Assert
    assertFalse(
      filter.test(disabledUpdate),
      "Disabled vehicle should not pass filter when includeDisabled is false"
    );
    assertTrue(filter.test(enabledUpdate), "Enabled vehicle should pass filter");

    // Now test with includeDisabled = true
    VehicleFilterParameters includeDisabledParams = new VehicleFilterParameters(
      null,
      null,
      null,
      null,
      null,
      null,
      true,
      true
    );

    VehicleUpdateFilter includeDisabledFilter = new VehicleUpdateFilter(
      includeDisabledParams,
      bbox,
      CODESPACE_RESOLVER
    );

    assertTrue(
      includeDisabledFilter.test(disabledUpdate),
      "Disabled vehicle should pass filter when includeDisabled is true"
    );
  }

  @Test
  void testNullVehicleUpdate() {
    // Arrange
    VehicleFilterParameters filterParams = new VehicleFilterParameters(
      null,
      null,
      null,
      null,
      null,
      null,
      false,
      false
    );

    BoundingBoxQueryParameters bbox = new BoundingBoxQueryParameters(
      59.0,
      10.0,
      60.0,
      11.0
    );

    VehicleUpdateFilter filter = new VehicleUpdateFilter(
      filterParams,
      bbox,
      CODESPACE_RESOLVER
    );

    // Assert
    assertFalse(filter.test(null), "Null update should not pass filter");

    // Test with null vehicle in update
    VehicleUpdate nullVehicleUpdate = new VehicleUpdate(
      "test-id",
      UpdateType.CREATE,
      null
    );
    assertFalse(
      filter.test(nullVehicleUpdate),
      "Update with null vehicle should not pass filter"
    );
  }

  // Helper methods to create test data
  private VehicleUpdate createVehicleUpdate(double lat, double lon) {
    Vehicle vehicle = createVehicle(lat, lon);
    return new VehicleUpdate("test-id", UpdateType.CREATE, vehicle);
  }

  private Vehicle createVehicle(double lat, double lon) {
    Vehicle vehicle = new Vehicle();
    vehicle.setId("TST:Vehicle:test");
    vehicle.setSystemId(TEST_SYSTEM_ID);
    vehicle.setLat(lat);
    vehicle.setLon(lon);

    // Set up system with operator
    System system = new System();
    system.setId(TEST_SYSTEM_ID);

    // Initialize the operator object
    org.entur.lamassu.model.entities.Operator operator =
      new org.entur.lamassu.model.entities.Operator();
    operator.setId(TEST_OPERATOR_ID);
    system.setOperator(operator);

    vehicle.setSystem(system);

    return vehicle;
  }

  private VehicleUpdate createVehicleUpdateWithFormFactor(
    double lat,
    double lon,
    FormFactor formFactor
  ) {
    Vehicle vehicle = createVehicle(lat, lon);
    VehicleType vehicleType = new VehicleType();
    vehicleType.setFormFactor(formFactor);
    vehicle.setVehicleType(vehicleType);
    return new VehicleUpdate("test-id", UpdateType.CREATE, vehicle);
  }

  private VehicleUpdate createVehicleUpdateWithPropulsion(
    double lat,
    double lon,
    PropulsionType propulsionType
  ) {
    Vehicle vehicle = createVehicle(lat, lon);
    VehicleType vehicleType = new VehicleType();
    vehicleType.setPropulsionType(propulsionType);
    vehicle.setVehicleType(vehicleType);
    return new VehicleUpdate("test-id", UpdateType.CREATE, vehicle);
  }

  private VehicleUpdate createVehicleUpdateWithReservedStatus(
    double lat,
    double lon,
    boolean reserved
  ) {
    Vehicle vehicle = createVehicle(lat, lon);
    vehicle.setReserved(reserved);
    return new VehicleUpdate("test-id", UpdateType.CREATE, vehicle);
  }

  private VehicleUpdate createVehicleUpdateWithDisabledStatus(
    double lat,
    double lon,
    boolean disabled
  ) {
    Vehicle vehicle = createVehicle(lat, lon);
    vehicle.setDisabled(disabled);
    return new VehicleUpdate("test-id", UpdateType.CREATE, vehicle);
  }
}
