package org.entur.lamassu.leader.entityupdater;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;
import java.util.List;
import org.entur.gbfs.loader.v3.GbfsV3Delivery;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.leader.GbfsUpdateContinuityTracker;
import org.entur.lamassu.mapper.entitymapper.RentalUrisMapper;
import org.entur.lamassu.mapper.entitymapper.StationMapper;
import org.entur.lamassu.mapper.entitymapper.TranslationMapper;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.SpatialIndexIdGeneratorService;
import org.entur.lamassu.stubs.EntityCacheStub;
import org.entur.lamassu.stubs.StubUpdateContinuityCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSName;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStationInformation;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSData;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStation;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStationStatus;
import org.mobilitydata.gbfs.v3_0.system_information.GBFSSystemInformation;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSSystemPricingPlans;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleTypes;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Reproduces a production issue where a station newly added by a provider was present
 * in the GBFS feed endpoints but permanently missing from the entity cache / GraphQL
 * API until the subscription was restarted.
 *
 * Scenario: the provider adds a station to station_status before it is visible in
 * station_information (the two files are fetched on independent TTL schedules, and
 * providers do not update files atomically). The station_status delta emits CREATE
 * for the station exactly once, but StationsUpdater must drop it because station
 * information is missing. Since delta continuity is otherwise intact, no CREATE is
 * ever emitted again for that station.
 *
 * The fix: when a station delta cannot be fully applied, station update continuity
 * is cleared, forcing a full rebuild on the next update, which picks up the station
 * once its station information has become available.
 */
@ExtendWith(MockitoExtension.class)
class EntityCachesUpdaterStationInformationLagTest {

  private static final String SYSTEM_ID = "test-system";
  private static final String EXISTING_STATION_ID = "station-1";
  private static final String NEW_STATION_ID = "station-2";

  @Mock
  private SystemUpdater systemUpdater;

  @Mock
  private VehicleTypesUpdater vehicleTypesUpdater;

  @Mock
  private PricingPlansUpdater pricingPlansUpdater;

  @Mock
  private RegionsUpdater regionsUpdater;

  @Mock
  private VehiclesUpdater vehiclesUpdater;

  @Mock
  private GeofencingZonesUpdater geofencingZonesUpdater;

  @Mock
  private StationSpatialIndex spatialIndex;

  @Mock
  private MetricsService metricsService;

  private final EntityCacheStub<Station> stationCache = new EntityCacheStub<>();
  private final EntityCacheStub<VehicleType> vehicleTypeCache = new EntityCacheStub<>();

  private EntityCachesUpdater entityCachesUpdater;
  private FeedProvider feedProvider;

  @BeforeEach
  void setUp() {
    var stationsUpdater = new StationsUpdater(
      stationCache,
      spatialIndex,
      new StationMapper(new TranslationMapper(), new RentalUrisMapper()),
      metricsService,
      new SpatialIndexIdGeneratorService(vehicleTypeCache)
    );

    var continuityTracker = new GbfsUpdateContinuityTracker(
      new StubUpdateContinuityCache(),
      new StubUpdateContinuityCache()
    );

    entityCachesUpdater =
      new EntityCachesUpdater(
        systemUpdater,
        vehicleTypesUpdater,
        pricingPlansUpdater,
        regionsUpdater,
        vehiclesUpdater,
        stationsUpdater,
        geofencingZonesUpdater,
        continuityTracker
      );

    feedProvider = new FeedProvider();
    feedProvider.setSystemId(SYSTEM_ID);
    feedProvider.setCodespace("test");
    feedProvider.setOperatorId("test-operator");
    feedProvider.setLanguage("en");
  }

  @Test
  void newStationShouldEventuallyAppearWhenStationInformationLagsStationStatus() {
    // Poll 0: steady state with one station
    var status0 = statusFeed(1000L, status(EXISTING_STATION_ID, 5));
    var infoWithoutNewStation = infoFeed(info(EXISTING_STATION_ID));

    entityCachesUpdater.updateEntityCaches(
      feedProvider,
      delivery(status0, infoWithoutNewStation),
      oldDelivery(null)
    );
    assertNotNull(stationCache.get(EXISTING_STATION_ID));

    // Poll 1: the new station appears in station_status, but station_information
    // still lags behind
    var status1 = statusFeed(
      2000L,
      status(EXISTING_STATION_ID, 5),
      status(NEW_STATION_ID, 3)
    );

    entityCachesUpdater.updateEntityCaches(
      feedProvider,
      delivery(status1, infoWithoutNewStation),
      oldDelivery(status0)
    );
    // The CREATE delta is dropped because station information is missing
    assertNull(stationCache.get(NEW_STATION_ID));

    // Poll 2: station_information has caught up, station_status is unchanged, so
    // no CREATE delta is emitted for the new station anymore
    var status2 = statusFeed(
      3000L,
      status(EXISTING_STATION_ID, 5),
      status(NEW_STATION_ID, 3)
    );
    var infoWithNewStation = infoFeed(info(EXISTING_STATION_ID), info(NEW_STATION_ID));

    entityCachesUpdater.updateEntityCaches(
      feedProvider,
      delivery(status2, infoWithNewStation),
      oldDelivery(status1)
    );

    // The incomplete update in poll 1 must have broken update continuity, forcing
    // a full rebuild in poll 2 that picks up the new station
    assertNotNull(stationCache.get(NEW_STATION_ID));
    assertNotNull(stationCache.get(EXISTING_STATION_ID));
  }

  private GbfsV3Delivery delivery(
    GBFSStationStatus stationStatus,
    GBFSStationInformation stationInformation
  ) {
    var systemInformation = new GBFSSystemInformation();
    systemInformation.setData(
      new org.mobilitydata.gbfs.v3_0.system_information.GBFSData()
    );
    var vehicleTypes = new GBFSVehicleTypes();
    vehicleTypes.setData(new org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSData());
    var pricingPlans = new GBFSSystemPricingPlans();
    pricingPlans.setData(new org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSData());

    return new GbfsV3Delivery(
      null,
      null,
      systemInformation,
      vehicleTypes,
      stationInformation,
      stationStatus,
      null,
      null,
      pricingPlans,
      null,
      null,
      null
    );
  }

  private GbfsV3Delivery oldDelivery(GBFSStationStatus stationStatus) {
    return new GbfsV3Delivery(
      null,
      null,
      null,
      null,
      null,
      stationStatus,
      null,
      null,
      null,
      null,
      null,
      null
    );
  }

  private GBFSStationStatus statusFeed(long lastUpdated, GBFSStation... stations) {
    var feed = new GBFSStationStatus();
    feed.setLastUpdated(new Date(lastUpdated));
    var data = new GBFSData();
    data.setStations(List.of(stations));
    feed.setData(data);
    return feed;
  }

  private GBFSStation status(String stationId, int numVehiclesAvailable) {
    var status = new GBFSStation();
    status.setStationId(stationId);
    status.setNumVehiclesAvailable(numVehiclesAvailable);
    status.setNumDocksAvailable(10);
    status.setIsInstalled(true);
    status.setIsRenting(true);
    status.setIsReturning(true);
    status.setLastReported(new Date(0));
    return status;
  }

  private GBFSStationInformation infoFeed(
    org.mobilitydata.gbfs.v3_0.station_information.GBFSStation... stations
  ) {
    var feed = new GBFSStationInformation();
    var data = new org.mobilitydata.gbfs.v3_0.station_information.GBFSData();
    data.setStations(List.of(stations));
    feed.setData(data);
    return feed;
  }

  private org.mobilitydata.gbfs.v3_0.station_information.GBFSStation info(
    String stationId
  ) {
    var info = new org.mobilitydata.gbfs.v3_0.station_information.GBFSStation();
    info.setStationId(stationId);
    info.setName(List.of(new GBFSName().withLanguage("en").withText(stationId)));
    info.setLat(59.9);
    info.setLon(10.7);
    return info;
  }
}
