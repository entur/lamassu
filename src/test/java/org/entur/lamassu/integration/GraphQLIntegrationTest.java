package org.entur.lamassu.integration;

import static org.springframework.graphql.test.tester.GraphQlTester.Response;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.WebGraphQlTester;

class GraphQLIntegrationTest extends AbstractIntegrationTestBase {

  @Autowired
  private WebGraphQlTester graphQlTester;

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
}
