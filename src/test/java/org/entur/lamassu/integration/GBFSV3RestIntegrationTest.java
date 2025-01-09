package org.entur.lamassu.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public class GBFSV3RestIntegrationTest extends AbstractIntegrationTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testFeedProviderDiscovery() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/manifest.json").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.datasets[1].system_id").value("testozon"));
  }

  @Test
  void testGBFS() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/gbfs").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.last_updated").value("2020-11-30T09:15:10.000+00:00"));
  }

  @Test
  @Disabled("gbfs_versions intentionally not mapped")
  void testGBFSVersions() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/gbfs_versions").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.versions[0].version").value("2.1"));
  }

  @Test
  void testSystemInformation() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/system_information").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.system_id").value("testozon"));
  }

  @Test
  void testVehicleTypes() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/vehicle_types").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.vehicle_types[0].vehicle_type_id")
          .value("OZO:VehicleType:abc123")
      );
  }

  @Test
  void testVehicleStatus() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/vehicle_status").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.vehicles[0].vehicle_id")
          .value("OZO:Vehicle:973a5c94-c288-4a2b-afa6-de8aeb6ae2e5")
      )
      .andExpect(
        jsonPath("$.data.vehicles[0].vehicle_type_id").value("OZO:VehicleType:abc123")
      );
  }

  @Test
  void testSystemRegions() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/system_regions").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.regions[0].region_id").value("OZO:Region:3"));
  }

  @Test
  void testSystemPricingPlans() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/system_pricing_plans").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.plans[0].plan_id").value("OZO:PricingPlan:bike_plan_1")
      );
  }

  @Test
  void testStationInformation() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3/testozon/station_information").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.stations[0].station_id").value("OZO:Station:pga"));
  }

  @Test
  void testStationStatus() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/station_status").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.stations[1].station_id").value("OZO:Station:station2"));
  }

  @Test
  void testSystemAlerts() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/system_alerts").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.alerts[0].alert_id").value("OZO:Alert:21"))
      .andExpect(jsonPath("$.data.alerts[0].station_ids[0]").value("OZO:Station:123"));
  }

  @Test
  void testGeofencingZones() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/geofencing_zones").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.geofencing_zones.features[0].properties.name[0].text")
          .value("NE 24th/NE Knott")
      );
  }

  @Test
  void testUnknownProviderResponds404() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/foobar/gbfs").contentType("application/json"))
      .andExpect(status().isNotFound());
  }

  @Test
  void testUnsupportedFeedResponds400() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3/testozon/foobar").contentType("application/json"))
      .andExpect(status().isBadRequest());
  }
}
