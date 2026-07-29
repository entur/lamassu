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
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
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
 * Reproduces a production issue where fields sourced from station_information (such as
 * station_area) were added to an existing station but never appeared in the entity
 * cache / GraphQL API until the subscription was restarted.
 *
 * Entity updates are driven by station_status deltas only; station_information is
 * consulted as a passive lookup while applying those deltas. When a provider changes
 * station_information for a station whose station_status is momentarily stable, no
 * status delta is emitted, so the station is never re-mapped and the new
 * station_information fields never reach the entity cache.
 *
 * The fix: when station_information content changes between deliveries, the station
 * update is performed as a full rebuild (base is dropped), re-mapping every station
 * from the current station_information -- the same effect as restarting the
 * subscription, which is what healed this in production.
 */
@ExtendWith(MockitoExtension.class)
class EntityCachesUpdaterStationInformationChangeTest {

  private static final String SYSTEM_ID = "test-system";
  private static final String STATION_ID = "station-1";

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
  void stationInformationChangeShouldPropagateWhenStationStatusIsStable() {
    // Poll 0: steady state, station has no station_area yet
    var status0 = statusFeed(1000L, status(STATION_ID, 5));
    var infoWithoutArea = infoFeed(info(STATION_ID, null));

    entityCachesUpdater.updateEntityCaches(
      feedProvider,
      delivery(status0, infoWithoutArea),
      oldDelivery(null, null)
    );
    assertNotNull(stationCache.get(STATION_ID));
    assertNull(
      stationCache.get(STATION_ID).getStationArea(),
      "precondition: station_area not yet present"
    );

    // Poll 1: station_status is unchanged (only the feed timestamp advances, so no
    // entity delta is produced), but station_information now carries a station_area
    var status1 = statusFeed(2000L, status(STATION_ID, 5));
    var infoWithArea = infoFeed(info(STATION_ID, stationArea()));

    entityCachesUpdater.updateEntityCaches(
      feedProvider,
      delivery(status1, infoWithArea),
      oldDelivery(status0, infoWithoutArea)
    );

    // The station_information change must propagate even though station_status
    // produced no delta for the station
    assertNotNull(
      stationCache.get(STATION_ID).getStationArea(),
      "station_area added to station_information must reach the entity cache"
    );
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

  private GbfsV3Delivery oldDelivery(
    GBFSStationStatus stationStatus,
    GBFSStationInformation stationInformation
  ) {
    return new GbfsV3Delivery(
      null,
      null,
      null,
      null,
      stationInformation,
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
    String stationId,
    org.geojson.MultiPolygon stationArea
  ) {
    var info = new org.mobilitydata.gbfs.v3_0.station_information.GBFSStation();
    info.setStationId(stationId);
    info.setName(List.of(new GBFSName().withLanguage("en").withText(stationId)));
    info.setLat(59.9);
    info.setLon(10.7);
    info.setStationArea(stationArea);
    return info;
  }

  private org.geojson.MultiPolygon stationArea() {
    var multiPolygon = new org.geojson.MultiPolygon();
    multiPolygon.add(
      new Polygon(
        new LngLatAlt(10.0, 59.0),
        new LngLatAlt(10.1, 59.0),
        new LngLatAlt(10.1, 59.1),
        new LngLatAlt(10.0, 59.0)
      )
    );
    return multiPolygon;
  }
}
