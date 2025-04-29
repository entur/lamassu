package org.entur.lamassu.graphql.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.entur.lamassu.graphql.subscription.filter.StationUpdateFilter;
import org.entur.lamassu.graphql.subscription.handler.StationSubscriptionHandler;
import org.entur.lamassu.graphql.subscription.model.StationUpdate;
import org.entur.lamassu.graphql.subscription.model.UpdateType;
import org.entur.lamassu.graphql.validation.QueryParameterValidator;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.StationFilterParameters;
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
 * Tests for the StationSubscriptionController class.
 * Uses a combination of unit tests and lightweight integration tests to verify
 * the subscription functionality.
 */
@ExtendWith(MockitoExtension.class)
class StationSubscriptionControllerTest {

  @Mock
  private StationSubscriptionHandler subscriptionHandler;

  @Mock
  private QueryParameterValidator validationService;

  @Mock
  private FeedProviderService feedProviderService;

  @InjectMocks
  private StationSubscriptionController controller;

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
  void testStationSubscriptionWithBoundingBox() {
    // Setup test data
    Station testStation = createTestStation();
    StationUpdate testUpdate = new StationUpdate(
      testStation.getId(),
      UpdateType.CREATE,
      testStation
    );

    // Create a test publisher
    Flux<List<StationUpdate>> testPublisher = Flux.just(List.of(testUpdate));

    // Mock the subscription handler
    when(subscriptionHandler.getPublisher(any(StationUpdateFilter.class)))
      .thenReturn(testPublisher);

    // Execute the controller method with bounding box parameters
    Publisher<List<StationUpdate>> result = controller.stations(
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
      null
    );

    // Verify the result
    StepVerifier
      .create(result)
      .expectNextMatches(updates -> {
        return (
          updates.size() == 1 &&
          updates.get(0).getStation().getId().equals(testStation.getId())
        );
      })
      .verifyComplete();

    // Verify correct filter was created
    ArgumentCaptor<StationUpdateFilter> filterCaptor = ArgumentCaptor.forClass(
      StationUpdateFilter.class
    );
    verify(subscriptionHandler).getPublisher(filterCaptor.capture());

    StationUpdateFilter capturedFilter = filterCaptor.getValue();
    assertNotNull(capturedFilter.getBoundingBoxParameters());
    assertEquals(59.0, capturedFilter.getBoundingBoxParameters().getMinimumLatitude());
    assertEquals(10.0, capturedFilter.getBoundingBoxParameters().getMinimumLongitude());
    assertEquals(60.0, capturedFilter.getBoundingBoxParameters().getMaximumLatitude());
    assertEquals(11.0, capturedFilter.getBoundingBoxParameters().getMaximumLongitude());
  }

  @Test
  void testStationSubscriptionWithRangeSearch() {
    // Setup range search
    when(validationService.isRangeSearch(any(), any(), any())).thenReturn(true);

    // Setup test data
    Station testStation = createTestStation();
    StationUpdate testUpdate = new StationUpdate(
      testStation.getId(),
      UpdateType.CREATE,
      testStation
    );

    // Create a test publisher
    Flux<List<StationUpdate>> testPublisher = Flux.just(List.of(testUpdate));

    // Mock the subscription handler
    when(subscriptionHandler.getPublisher(any(StationUpdateFilter.class)))
      .thenReturn(testPublisher);

    // Execute the controller method with range parameters
    Publisher<List<StationUpdate>> result = controller.stations(
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
      null
    );

    // Verify the result
    StepVerifier
      .create(result)
      .expectNextMatches(updates -> {
        return (
          updates.size() == 1 &&
          updates.get(0).getStation().getId().equals(testStation.getId())
        );
      })
      .verifyComplete();

    // Verify correct filter was created
    ArgumentCaptor<StationUpdateFilter> filterCaptor = ArgumentCaptor.forClass(
      StationUpdateFilter.class
    );
    verify(subscriptionHandler).getPublisher(filterCaptor.capture());

    StationUpdateFilter capturedFilter = filterCaptor.getValue();
    assertNotNull(capturedFilter.getRangeQueryParameters());
    assertEquals(59.5, capturedFilter.getRangeQueryParameters().getLat());
    assertEquals(10.5, capturedFilter.getRangeQueryParameters().getLon());
    assertEquals(1000.0, capturedFilter.getRangeQueryParameters().getRange());
  }

  @Test
  void testStationSubscriptionWithVehicleTypeFilters() {
    // Setup test data
    Station testStation = createTestStation();
    StationUpdate testUpdate = new StationUpdate(
      testStation.getId(),
      UpdateType.CREATE,
      testStation
    );

    // Create a test publisher
    Flux<List<StationUpdate>> testPublisher = Flux.just(List.of(testUpdate));

    // Mock the subscription handler
    when(subscriptionHandler.getPublisher(any(StationUpdateFilter.class)))
      .thenReturn(testPublisher);

    // Execute the controller method with vehicle type filters
    Publisher<List<StationUpdate>> result = controller.stations(
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
      List.of(PropulsionType.ELECTRIC)
    );

    // Verify the result
    StepVerifier
      .create(result)
      .expectNextMatches(updates -> {
        return (
          updates.size() == 1 &&
          updates.get(0).getStation().getId().equals(testStation.getId())
        );
      })
      .verifyComplete();

    // Verify correct filter was created
    ArgumentCaptor<StationUpdateFilter> filterCaptor = ArgumentCaptor.forClass(
      StationUpdateFilter.class
    );
    verify(subscriptionHandler).getPublisher(filterCaptor.capture());

    StationUpdateFilter capturedFilter = filterCaptor.getValue();
    StationFilterParameters filterParams = capturedFilter.getFilterParameters();
    assertNotNull(filterParams.getAvailableFormFactors());
    assertEquals(1, filterParams.getAvailableFormFactors().size());
    assertEquals(FormFactor.BICYCLE, filterParams.getAvailableFormFactors().get(0));

    assertNotNull(filterParams.getAvailablePropulsionTypes());
    assertEquals(1, filterParams.getAvailablePropulsionTypes().size());
    assertEquals(
      PropulsionType.ELECTRIC,
      filterParams.getAvailablePropulsionTypes().get(0)
    );
  }

  @Test
  void testStationSubscriptionWithOperatorFilter() {
    // Setup test data
    Station testStation = createTestStation();
    StationUpdate testUpdate = new StationUpdate(
      testStation.getId(),
      UpdateType.CREATE,
      testStation
    );

    // Create a test publisher
    Flux<List<StationUpdate>> testPublisher = Flux.just(List.of(testUpdate));

    // Mock the subscription handler
    when(subscriptionHandler.getPublisher(any(StationUpdateFilter.class)))
      .thenReturn(testPublisher);

    // Execute the controller method with operator filter
    Publisher<List<StationUpdate>> result = controller.stations(
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
      null
    );

    // Verify the result
    StepVerifier
      .create(result)
      .expectNextMatches(updates -> {
        return (
          updates.size() == 1 &&
          updates.get(0).getStation().getId().equals(testStation.getId())
        );
      })
      .verifyComplete();

    // Verify correct filter was created
    ArgumentCaptor<StationUpdateFilter> filterCaptor = ArgumentCaptor.forClass(
      StationUpdateFilter.class
    );
    verify(subscriptionHandler).getPublisher(filterCaptor.capture());

    StationUpdateFilter capturedFilter = filterCaptor.getValue();
    StationFilterParameters filterParams = capturedFilter.getFilterParameters();
    assertNotNull(filterParams.getOperators());
    assertEquals(1, filterParams.getOperators().size());
    assertEquals("test-operator", filterParams.getOperators().get(0));
  }

  /**
   * Creates a test station for use in tests.
   */
  private Station createTestStation() {
    Station station = new Station();
    station.setId("test-station-id");
    // Don't set name as it requires TranslatedString
    station.setLat(59.5);
    station.setLon(10.5);
    station.setSystemId(TEST_SYSTEM_ID);
    return station;
  }
}
