package org.entur.lamassu.graphql.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.entur.lamassu.graphql.subscription.filter.VehicleUpdateFilter;
import org.entur.lamassu.graphql.subscription.handler.VehicleSubscriptionHandler;
import org.entur.lamassu.graphql.subscription.model.UpdateType;
import org.entur.lamassu.graphql.subscription.model.VehicleUpdate;
import org.entur.lamassu.graphql.validation.QueryParameterValidator;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Tests for the VehicleSubscriptionController class.
 * Verifies the subscription functionality for vehicle updates.
 */
@ExtendWith(MockitoExtension.class)
class VehicleSubscriptionControllerTest {

  @Mock
  private VehicleSubscriptionHandler subscriptionHandler;

  @Mock
  private QueryParameterValidator validationService;

  @Mock
  private FeedProviderService feedProviderService;

  @InjectMocks
  private VehicleSubscriptionController controller;

  private static final String TEST_SYSTEM_ID = "test-system";
  private static final String TEST_CODESPACE = "TST";

  @BeforeEach
  void setUp() {
    // Setup common mocks with lenient to avoid UnnecessaryStubbingException
    FeedProvider feedProvider = new FeedProvider();
    feedProvider.setSystemId(TEST_SYSTEM_ID);
    feedProvider.setCodespace(TEST_CODESPACE);
    lenient()
      .when(feedProviderService.getFeedProviderBySystemId(TEST_SYSTEM_ID))
      .thenReturn(feedProvider);

    // Allow validation to pass
    lenient()
      .when(validationService.isRangeSearch(any(), any(), any()))
      .thenReturn(false);
  }

  @Test
  void testVehicleSubscriptionWithBoundingBox() {
    // Setup test data
    Vehicle testVehicle = createTestVehicle();
    VehicleUpdate testUpdate = new VehicleUpdate(
      testVehicle.getId(),
      UpdateType.CREATE,
      testVehicle
    );

    // Create a test publisher
    Flux<List<VehicleUpdate>> testPublisher = Flux.just(List.of(testUpdate));

    // Mock the subscription handler
    when(subscriptionHandler.getPublisher(any(VehicleUpdateFilter.class)))
      .thenReturn(testPublisher);

    // Execute the controller method with bounding box parameters
    Publisher<List<VehicleUpdate>> result = controller.vehicles(
      null,
      null,
      null,
      59.0,
      10.0,
      60.0,
      11.0,
      List.of(TEST_CODESPACE),
      List.of(TEST_SYSTEM_ID),
      null,
      null,
      null,
      null,
      null
    );

    // Verify the result
    StepVerifier
      .create(result)
      .expectNextMatches(updates -> {
        return (
          updates.size() == 1 &&
          updates.get(0).getVehicle().getId().equals(testVehicle.getId())
        );
      })
      .verifyComplete();

    // Verify correct filter was created
    ArgumentCaptor<VehicleUpdateFilter> filterCaptor = ArgumentCaptor.forClass(
      VehicleUpdateFilter.class
    );
    verify(subscriptionHandler).getPublisher(filterCaptor.capture());

    VehicleUpdateFilter capturedFilter = filterCaptor.getValue();
    assertNotNull(capturedFilter.getBoundingBoxParameters());
    assertEquals(59.0, capturedFilter.getBoundingBoxParameters().getMinimumLatitude());
    assertEquals(10.0, capturedFilter.getBoundingBoxParameters().getMinimumLongitude());
    assertEquals(60.0, capturedFilter.getBoundingBoxParameters().getMaximumLatitude());
    assertEquals(11.0, capturedFilter.getBoundingBoxParameters().getMaximumLongitude());
  }

  @Test
  void testVehicleSubscriptionWithRangeSearch() {
    // Setup range search
    when(validationService.isRangeSearch(any(), any(), any())).thenReturn(true);

    // Setup test data
    Vehicle testVehicle = createTestVehicle();
    VehicleUpdate testUpdate = new VehicleUpdate(
      testVehicle.getId(),
      UpdateType.CREATE,
      testVehicle
    );

    // Create a test publisher
    Flux<List<VehicleUpdate>> testPublisher = Flux.just(List.of(testUpdate));

    // Mock the subscription handler
    when(subscriptionHandler.getPublisher(any(VehicleUpdateFilter.class)))
      .thenReturn(testPublisher);

    // Execute the controller method with range parameters
    Publisher<List<VehicleUpdate>> result = controller.vehicles(
      59.5,
      10.5,
      1000,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );

    // Verify the result
    StepVerifier
      .create(result)
      .expectNextMatches(updates -> {
        return (
          updates.size() == 1 &&
          updates.get(0).getVehicle().getId().equals(testVehicle.getId())
        );
      })
      .verifyComplete();

    // Verify correct filter was created
    ArgumentCaptor<VehicleUpdateFilter> filterCaptor = ArgumentCaptor.forClass(
      VehicleUpdateFilter.class
    );
    verify(subscriptionHandler).getPublisher(filterCaptor.capture());

    VehicleUpdateFilter capturedFilter = filterCaptor.getValue();
    assertNotNull(capturedFilter.getRangeQueryParameters());
    assertEquals(59.5, capturedFilter.getRangeQueryParameters().getLat());
    assertEquals(10.5, capturedFilter.getRangeQueryParameters().getLon());
    assertEquals(1000.0, capturedFilter.getRangeQueryParameters().getRange());
  }

  @Test
  void testVehicleSubscriptionWithVehicleTypeFilters() {
    // Setup test data
    Vehicle testVehicle = createTestVehicle();
    VehicleUpdate testUpdate = new VehicleUpdate(
      testVehicle.getId(),
      UpdateType.CREATE,
      testVehicle
    );

    // Create a test publisher
    Flux<List<VehicleUpdate>> testPublisher = Flux.just(List.of(testUpdate));

    // Mock the subscription handler
    when(subscriptionHandler.getPublisher(any(VehicleUpdateFilter.class)))
      .thenReturn(testPublisher);

    // Execute the controller method with vehicle type filters
    Publisher<List<VehicleUpdate>> result = controller.vehicles(
      null,
      null,
      null,
      59.0,
      10.0,
      60.0,
      11.0,
      null,
      null,
      null,
      List.of(FormFactor.BICYCLE),
      List.of(PropulsionType.ELECTRIC),
      null,
      null
    );

    // Verify the result
    StepVerifier
      .create(result)
      .expectNextMatches(updates -> {
        return (
          updates.size() == 1 &&
          updates.get(0).getVehicle().getId().equals(testVehicle.getId())
        );
      })
      .verifyComplete();

    // Verify correct filter was created
    ArgumentCaptor<VehicleUpdateFilter> filterCaptor = ArgumentCaptor.forClass(
      VehicleUpdateFilter.class
    );
    verify(subscriptionHandler).getPublisher(filterCaptor.capture());

    VehicleUpdateFilter capturedFilter = filterCaptor.getValue();
    VehicleFilterParameters filterParams = capturedFilter.getFilterParameters();
    assertNotNull(filterParams.getFormFactors());
    assertEquals(1, filterParams.getFormFactors().size());
    assertEquals(FormFactor.BICYCLE, filterParams.getFormFactors().get(0));

    assertNotNull(filterParams.getPropulsionTypes());
    assertEquals(1, filterParams.getPropulsionTypes().size());
    assertEquals(PropulsionType.ELECTRIC, filterParams.getPropulsionTypes().get(0));
  }

  @Test
  void testVehicleSubscriptionWithStatusFilters() {
    // Setup test data
    Vehicle testVehicle = createTestVehicle();
    VehicleUpdate testUpdate = new VehicleUpdate(
      testVehicle.getId(),
      UpdateType.CREATE,
      testVehicle
    );

    // Create a test publisher
    Flux<List<VehicleUpdate>> testPublisher = Flux.just(List.of(testUpdate));

    // Mock the subscription handler
    when(subscriptionHandler.getPublisher(any(VehicleUpdateFilter.class)))
      .thenReturn(testPublisher);

    // Execute the controller method with status filters
    Publisher<List<VehicleUpdate>> result = controller.vehicles(
      null,
      null,
      null,
      59.0,
      10.0,
      60.0,
      11.0,
      null,
      null,
      null,
      null,
      null,
      true, // includeReserved
      false // includeDisabled
    );

    // Verify the result
    StepVerifier
      .create(result)
      .expectNextMatches(updates -> {
        return (
          updates.size() == 1 &&
          updates.get(0).getVehicle().getId().equals(testVehicle.getId())
        );
      })
      .verifyComplete();

    // Verify correct filter was created
    ArgumentCaptor<VehicleUpdateFilter> filterCaptor = ArgumentCaptor.forClass(
      VehicleUpdateFilter.class
    );
    verify(subscriptionHandler).getPublisher(filterCaptor.capture());

    VehicleUpdateFilter capturedFilter = filterCaptor.getValue();
    VehicleFilterParameters filterParams = capturedFilter.getFilterParameters();
    assertTrue(filterParams.getIncludeReserved());
    assertEquals(false, filterParams.getIncludeDisabled());
  }

  @Test
  void testVehicleSubscriptionWithOperatorFilter() {
    // Setup test data
    Vehicle testVehicle = createTestVehicle();
    VehicleUpdate testUpdate = new VehicleUpdate(
      testVehicle.getId(),
      UpdateType.CREATE,
      testVehicle
    );

    // Create a test publisher
    Flux<List<VehicleUpdate>> testPublisher = Flux.just(List.of(testUpdate));

    // Mock the subscription handler
    when(subscriptionHandler.getPublisher(any(VehicleUpdateFilter.class)))
      .thenReturn(testPublisher);

    // Execute the controller method with operator filter
    Publisher<List<VehicleUpdate>> result = controller.vehicles(
      null,
      null,
      null,
      59.0,
      10.0,
      60.0,
      11.0,
      null,
      null,
      List.of("test-operator"),
      null,
      null,
      null,
      null
    );

    // Verify the result
    StepVerifier
      .create(result)
      .expectNextMatches(updates -> {
        return (
          updates.size() == 1 &&
          updates.get(0).getVehicle().getId().equals(testVehicle.getId())
        );
      })
      .verifyComplete();

    // Verify correct filter was created
    ArgumentCaptor<VehicleUpdateFilter> filterCaptor = ArgumentCaptor.forClass(
      VehicleUpdateFilter.class
    );
    verify(subscriptionHandler).getPublisher(filterCaptor.capture());

    VehicleUpdateFilter capturedFilter = filterCaptor.getValue();
    VehicleFilterParameters filterParams = capturedFilter.getFilterParameters();
    assertNotNull(filterParams.getOperators());
    assertEquals(1, filterParams.getOperators().size());
    assertEquals("test-operator", filterParams.getOperators().get(0));
  }

  /**
   * Creates a test vehicle for use in tests.
   */
  private Vehicle createTestVehicle() {
    Vehicle vehicle = new Vehicle();
    vehicle.setId("test-vehicle-id");
    vehicle.setLat(59.5);
    vehicle.setLon(10.5);
    vehicle.setSystemId(TEST_SYSTEM_ID);

    // Set vehicle type
    VehicleType vehicleType = new VehicleType();
    vehicleType.setFormFactor(FormFactor.BICYCLE);
    vehicleType.setPropulsionType(PropulsionType.ELECTRIC);
    vehicle.setVehicleType(vehicleType);

    return vehicle;
  }
}
