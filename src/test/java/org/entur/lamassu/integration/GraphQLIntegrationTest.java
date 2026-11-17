package org.entur.lamassu.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.graphql.test.tester.GraphQlTester.Response;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.test.tester.WebGraphQlTester;

class GraphQLIntegrationTest extends AbstractIntegrationTestBase {

  // Spring Boot 4 no longer auto-provides a GraphQlTester bean; build one in-process from the
  // auto-configured WebGraphQlHandler (behaviour-equivalent for these query-correctness tests).
  @Autowired
  private WebGraphQlHandler webGraphQlHandler;

  @Autowired
  private EntityCache<Vehicle> vehicleCache;

  @Autowired
  private VehicleSpatialIndex vehicleSpatialIndex;

  @Autowired
  private EntityCache<Station> stationCache;

  @Autowired
  private StationSpatialIndex stationSpatialIndex;

  private WebGraphQlTester graphQlTester;

  @BeforeEach
  void setUpGraphQlTester() {
    graphQlTester = WebGraphQlTester.create(webGraphQlHandler);
  }

  @Test
  void testVehiclesQuery() {
    Response response = graphQlTester
      .documentName("vehicles_query_with_disabled")
      .execute();

    response.path("vehicles").entityList(Object.class).hasSize(2);

    response.path("vehicles[0].id").entity(String.class).isEqualTo("TST:Vehicle:1234");

    response
      .path("vehicles[0].system.name.translation[0].value")
      .entity(String.class)
      .isEqualTo("Test");
  }

  @Test
  void testVehicleByIdQuery() {
    Response response = graphQlTester.documentName("vehicle_by_id_query").execute();

    response.path("vehicle.id").entity(String.class).isEqualTo("TST:Vehicle:1235");
  }

  @Test
  void testVehiclesByIdQuery() {
    Response response = graphQlTester.documentName("vehicles_by_id_query").execute();

    response
      .path("vehicles[*].id")
      .entityList(String.class)
      .hasSize(2)
      .contains("TST:Vehicle:1235", "OZO:Vehicle:973a5c94-c288-4a2b-afa6-de8aeb6ae2e5");
  }

  @Test
  void testVehicleQueryWithoutDisabled() {
    Response response = graphQlTester
      .documentName("vehicles_query_without_disabled")
      .execute();

    response.path("vehicles").entityList(Object.class).hasSize(1);
  }

  @Test
  void testScooterFilterIncludesScooterStanding() {
    // The deprecated SCOOTER form factor must still match vehicles mapped to the GBFS v3
    // SCOOTER_STANDING form factor (see FormFactor#expandFormFactorFilter).
    Response response = graphQlTester
      .documentName("vehicles_query_scooter_filter")
      .execute();

    response.path("vehicles").entityList(Object.class).hasSize(1);
    response
      .path("vehicles[0].id")
      .entity(String.class)
      .isEqualTo("OZO:Vehicle:987fd100-b822-4347-86a4-b3eef8ca8b53");
    response
      .path("vehicles[0].vehicleType.formFactor")
      .entity(String.class)
      .isEqualTo("SCOOTER_STANDING");
  }

  @Test
  void testStationsQuery() {
    Response response = graphQlTester.documentName("stations_query").execute();

    response.path("stations[0].id").entity(String.class).isEqualTo("TST:Station:2");

    response
      .path("stations[0].name.translation[0].value")
      .entity(String.class)
      .isEqualTo("Cooler bikes");

    response
      .path("stations[0].rentalUris.web")
      .entity(String.class)
      .isEqualTo("https://rentmybikes.com");
  }

  @Test
  void testStationAreaPolylineEncodedMultiPolygon() {
    Response response = graphQlTester
      .documentName("stations_polyline_encoded_multi_polygon_query")
      .execute();

    response
      .path("stations[0].stationAreaPolylineEncodedMultiPolygon[0][0]")
      .entity(String.class)
      .isEqualTo("gxk~wBiiiyRdBM@oHaBLEnH");
  }

  @Test
  void testStationByIdQuery() {
    Response response = graphQlTester.documentName("station_by_id_query").execute();

    response.path("station.id").entity(String.class).isEqualTo("TST:Station:1");

    response
      .path("station.vehicleDocksAvailable[0].count")
      .entity(String.class)
      .isEqualTo("2");

    response.path("station.numVehiclesAvailable").entity(String.class).isEqualTo("1");

    response.path("station.numVehiclesDisabled").entity(String.class).isEqualTo("2");
  }

  @Test
  void testStationsByIdQuery() {
    Response response = graphQlTester.documentName("stations_by_id_query").execute();

    response.path("stations[0].id").entity(String.class).isEqualTo("TST:Station:1");

    response
      .path("stations[0].vehicleDocksAvailable[0].count")
      .entity(String.class)
      .isEqualTo("2");
  }

  @Test
  void testSystemsByIdQuery() {
    Response response = graphQlTester.documentName("systems_by_id_query").execute();

    response.path("systems").entityList(Object.class).hasSize(1);

    response.path("systems[0].id").entity(String.class).isEqualTo("testozon");

    response
      .path("systems[0].openingHours")
      .entity(String.class)
      .isEqualTo("Apr 1-Nov 3 00:00-24:00");
  }

  @Test
  void testSystemsQueryWithoutIdsReturnsAllSystems() {
    Response response = graphQlTester.documentName("systems_query").execute();

    response
      .path("systems[*].id")
      .entityList(String.class)
      .hasSize(2)
      .contains("testatlantis", "testozon");
  }

  @Test
  void testSystemsQueryUnknownSystemIsRejected() {
    graphQlTester
      .documentName("systems_query_unknown_system")
      .execute()
      .errors()
      .satisfy(errors -> {
        assertEquals(1, errors.size());
        assertEquals("Unknown system(s)", errors.get(0).getMessage());
      });
  }

  @Test
  void testGeofencingZones() {
    Response response = graphQlTester.documentName("geofencing_zones_query").execute();

    response
      .path("geofencingZones[0].systemId")
      .entity(String.class)
      .isEqualTo("testatlantis");

    response
      .path("geofencingZones[0].geojson.features[0].properties.rules[0].rideStartAllowed")
      .entity(String.class)
      .isEqualTo("true");

    response
      .path("geofencingZones[0].geojson.features[0].properties.rules[0].rideEndAllowed")
      .entity(String.class)
      .isEqualTo("true");
  }

  @Test
  void testPolylineEncodedMultiPolygon() {
    Response response = graphQlTester
      .documentName("polyline_encoded_multi_polygon_query")
      .execute();

    response
      .path(
        "geofencingZones[0].geojson.features[0].properties.polylineEncodedMultiPolygon[0][0]"
      )
      .entity(String.class)
      .isEqualTo(
        "qznuqBsgw~TnwAepEpsFglSveIexThp[{cVj}DvcE|yA|`AvlJjjFnBj_@vRhM`PrdD`JlJxfJddoBpnl@bcx@hmOtzDzfFyvSb`\\pfQpkW|{{@~aDpkpAmjDndH|rCf_Zr{IdjSwwN|ml@q~@|{_@mf^jhVoxXrdc@ohPahUyvJqyc@ee^bcMud\\sz\\{me@tc@g_Swv\\unAv~f@{zIzsCeqCtA}w@`QeWlsDgiAtGgu@ux@mzAea@}iFy{F}wGsv@keN|i@utTdxSaoD~ud@aoMc`EitJkvf@idCe_@klAn`G{~Gp_B}~CxtAynI|kKsyAp}SovPz`I~{JxhWc|dAvmq@qg^ogfBrhNegNqxH{gZlCui^niU_djAfm^e{~@lh^wn_@yoCkhHc}l@lj]iiZq{^g}m@rxo@kle@edGc_g@_ku@h}h@ephCnmgAk|fDz|j@~yCrhOz_[pq}@~_@r}BprChbC}m@~@duChb@n`@~d@z|Ah`BjrBr}@k{@to_@~j_@rhKofGcsEcdLl~M{g]`tKo~Al}XxxaA`pVhnm@"
      );
  }

  @Test
  void testUnknownOperatorDoesNotThrow() {
    Response response = graphQlTester
      .documentName("stations_query_unknown_operator")
      .execute();

    response.path("stations").entityList(Object.class).hasSize(0);
  }

  @Test
  void testVehiclesBoundingBoxQuery() {
    Response response = graphQlTester.documentName("vehicles_bbox_query").execute();

    response.path("vehicles").entityList(Object.class).hasSize(2);
  }

  @Test
  void testStationsBoundingBoxQuery() {
    Response response = graphQlTester.documentName("stations_bbox_query").execute();

    response.path("stations").entityList(Object.class).hasSize(1);
  }

  /**
   * A vehicle or station whose system can't be resolved, e.g. because its provider stopped
   * serving system_information, must be left out instead of failing the whole result.
   */
  @Test
  void testEntitiesWithoutResolvableSystemAreLeftOut() {
    var knownSystemId = vehicleCache.get("TST:Vehicle:1234").getSystemId();
    // Far from the other test data. The vehicle and station without a system are closest to
    // the search point, so that they would take up the places in the count if they counted.
    var broken = vehicle("no-system-vehicle", "no-such-system", 10.0, 10.0);
    var working = vehicle("known-system-vehicle", knownSystemId, 10.0, 10.001);
    var brokenStation = station("no-system-station", "no-such-system", 10.0, 10.0);
    var workingStation = station("known-system-station", knownSystemId, 10.0, 10.001);
    for (var vehicle : List.of(broken, working)) {
      vehicleCache.updateAll(Map.of(vehicle.getId(), vehicle));
      vehicleSpatialIndex.addAll(Map.of(vehicleIndexId(vehicle), vehicle));
    }
    for (var station : List.of(brokenStation, workingStation)) {
      stationCache.updateAll(Map.of(station.getId(), station));
      stationSpatialIndex.addAll(Map.of(stationIndexId(station), station));
    }

    try {
      var vehicles = graphQlTester
        .document(
          "{ vehicles(lat: 10.0, lon: 10.0, range: 1000, count: 1) { id system { id } } }"
        )
        .execute();
      vehicles.errors().verify();
      vehicles
        .path("vehicles[*].id")
        .entityList(String.class)
        .containsExactly(working.getId());

      var stations = graphQlTester
        .document(
          "{ stations(lat: 10.0, lon: 10.0, range: 1000, count: 1) { id system { id } } }"
        )
        .execute();
      stations.errors().verify();
      stations
        .path("stations[*].id")
        .entityList(String.class)
        .containsExactly(workingStation.getId());

      var byIds = graphQlTester
        .document(
          "{ vehicles(ids: [\"no-system-vehicle\", \"known-system-vehicle\"]) { id system { id } } }"
        )
        .execute();
      byIds.errors().verify();
      byIds
        .path("vehicles[*].id")
        .entityList(String.class)
        .containsExactly(working.getId());

      var single = graphQlTester
        .document("{ vehicle(id: \"no-system-vehicle\") { id system { id } } }")
        .execute();
      single.errors().verify();
      single.path("vehicle").valueIsNull();
    } finally {
      vehicleCache.removeAll(Set.of(broken.getId(), working.getId()));
      vehicleSpatialIndex.removeAll(
        Set.of(vehicleIndexId(broken), vehicleIndexId(working))
      );
      stationCache.removeAll(Set.of(brokenStation.getId(), workingStation.getId()));
      stationSpatialIndex.removeAll(
        Set.of(stationIndexId(brokenStation), stationIndexId(workingStation))
      );
    }
  }

  private static Vehicle vehicle(String id, String systemId, double lat, double lon) {
    var vehicle = new Vehicle();
    vehicle.setId(id);
    vehicle.setSystemId(systemId);
    vehicle.setLat(lat);
    vehicle.setLon(lon);
    vehicle.setReserved(false);
    vehicle.setDisabled(false);
    return vehicle;
  }

  private static Station station(String id, String systemId, double lat, double lon) {
    var station = new Station();
    station.setId(id);
    station.setSystemId(systemId);
    station.setLat(lat);
    station.setLon(lon);
    return station;
  }

  private static VehicleSpatialIndexId vehicleIndexId(Vehicle vehicle) {
    var indexId = new VehicleSpatialIndexId();
    indexId.setId(vehicle.getId());
    indexId.setSystemId(vehicle.getSystemId());
    indexId.setFormFactor(FormFactor.SCOOTER);
    indexId.setPropulsionType(PropulsionType.ELECTRIC);
    indexId.setReserved(false);
    indexId.setDisabled(false);
    return indexId;
  }

  private static StationSpatialIndexId stationIndexId(Station station) {
    var indexId = new StationSpatialIndexId();
    indexId.setId(station.getId());
    indexId.setSystemId(station.getSystemId());
    indexId.setAvailableFormFactors(List.of(FormFactor.SCOOTER));
    indexId.setAvailablePropulsionTypes(List.of(PropulsionType.ELECTRIC));
    return indexId;
  }
}
