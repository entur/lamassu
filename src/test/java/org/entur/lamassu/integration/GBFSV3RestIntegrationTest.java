package org.entur.lamassu.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public class GBFSV3RestIntegrationTest extends AbstractIntegrationTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void testFeedProviderDiscovery() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3beta/manifest.json").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.datasets[1].system_id").value("testozon"));
  }

  @Test
  public void testGBFS() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3beta/testozon/gbfs").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.last_updated").value("2020-11-30T09:15:10.000+00:00"));
  }

  @Test
  @Ignore("gbfs_versions intentionally not mapped")
  public void testGBFSVersions() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3beta/testozon/gbfs_versions").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.versions[0].version").value("2.1"));
  }

  @Test
  public void testSystemInformation() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3beta/testozon/system_information").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.system_id").value("testozon"));
  }

  @Test
  public void testVehicleTypes() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3beta/testozon/vehicle_types").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.vehicle_types[0].vehicle_type_id")
          .value("OZO:VehicleType:abc123")
      );
  }

  @Test
  public void testVehicleStatus() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3beta/testozon/vehicle_status").contentType("application/json")
      )
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
  public void testSystemRegions() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3beta/testozon/system_regions").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.regions[0].region_id").value("OZO:Region:3"));
  }

  @Test
  public void testSystemPricingPlans() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3beta/testozon/system_pricing_plans").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.plans[0].plan_id").value("OZO:PricingPlan:bike_plan_1")
      );
  }

  @Test
  public void testStationInformation() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3beta/testozon/station_information").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.stations[0].station_id").value("OZO:Station:pga"));
  }

  @Test
  public void testStationStatus() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3beta/testozon/station_status").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.stations[1].station_id").value("OZO:Station:station2"));
  }

  @Test
  public void testSystemAlerts() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3beta/testozon/system_alerts").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.alerts[0].alert_id").value("OZO:Alert:21"))
      .andExpect(jsonPath("$.data.alerts[0].station_ids[0]").value("OZO:Station:123"));
  }

  @Test
  public void testGeofencingZones() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v3beta/testozon/geofencing_zones").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.geofencing_zones.features[0].properties.name[0].text")
          .value("NE 24th/NE Knott")
      );
  }

  @Test
  public void testUnknownProviderResponds404() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3beta/foobar/gbfs").contentType("application/json"))
      .andExpect(status().isNotFound());
  }

  @Test
  public void testUnsupportedFeedResponds400() throws Exception {
    mockMvc
      .perform(get("/gbfs/v3beta/testozon/foobar").contentType("application/json"))
      .andExpect(status().isBadRequest());
  }
}
