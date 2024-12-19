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
public class GBFSRestIntegrationTest extends AbstractIntegrationTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testFeedProviderDiscovery() throws Exception {
    mockMvc
      .perform(get("/gbfs/v2").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.systems[0].id").value("testatlantis"));
  }

  @Test
  void testGBFS() throws Exception {
    mockMvc
      .perform(get("/gbfs/v2/testatlantis/gbfs").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.last_updated").value(1606727710));
  }

  @Test
  @Disabled("gbfs_versions intentionally not mapped")
  void testGBFSVersions() throws Exception {
    mockMvc
      .perform(get("/gbfs/v2/testatlantis/gbfs_versions").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.versions[0].version").value("2.1"));
  }

  @Test
  void testSystemInformation() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v2/testatlantis/system_information").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.system_id").value("testatlantis"));
  }

  @Test
  void testVehicleTypes() throws Exception {
    mockMvc
      .perform(get("/gbfs/v2/testatlantis/vehicle_types").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.vehicle_types[0].vehicle_type_id")
          .value("TST:VehicleType:Scooter")
      );
  }

  @Test
  void testFreeBikeStatus() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v2/testatlantis/free_bike_status").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.bikes[0].bike_id").value("TST:Vehicle:1234"))
      .andExpect(
        jsonPath("$.data.bikes[0].vehicle_type_id").value("TST:VehicleType:Scooter")
      )
      .andExpect(
        jsonPath("$.data.bikes[0].pricing_plan_id").value("TST:PricingPlan:Basic")
      );
  }

  @Test
  void testSystemRegions() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v2/testatlantis/system_regions").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.regions[0].region_id").value("TST:Region:Sahara"));
  }

  @Test
  void testSystemPricingPlans() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v2/testatlantis/system_pricing_plans").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.plans[0].plan_id").value("TST:PricingPlan:Basic"));
  }

  @Test
  void testStationInformation() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v2/testatlantis/station_information").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.stations[0].station_id").value("TST:Station:1"));
  }

  @Test
  void testStationStatus() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v2/testatlantis/station_status").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.stations[1].station_id").value("TST:Station:2"));
  }

  @Test
  void testSystemHours() throws Exception {
    mockMvc
      .perform(get("/gbfs/v2/testatlantis/system_hours").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.rental_hours[0].user_types[0]").value("member"));
  }

  @Test
  void testSystemCalendar() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v2/testatlantis/system_calendar").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.calendars[0].start_month").value(1));
  }

  @Test
  void testSystemAlerts() throws Exception {
    mockMvc
      .perform(get("/gbfs/v2/testatlantis/system_alerts").contentType("application/json"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.alerts[0].alert_id").value("TST:Alert:1"))
      .andExpect(jsonPath("$.data.alerts[0].station_ids[0]").value("TST:Station:1"))
      .andExpect(jsonPath("$.data.alerts[0].region_ids[0]").value("TST:Region:1"));
  }

  @Test
  void testGeofencingZones() throws Exception {
    mockMvc
      .perform(
        get("/gbfs/v2/testatlantis/geofencing_zones").contentType("application/json")
      )
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$.data.geofencing_zones.features[0].properties.name").value("Nes")
      );
  }

  @Test
  void testUnknownProviderResponds404() throws Exception {
    mockMvc
      .perform(get("/gbfs/v2/foobar/gbfs").contentType("application/json"))
      .andExpect(status().isNotFound());
  }

  @Test
  void testUnsupportedFeedResponds400() throws Exception {
    mockMvc
      .perform(get("/gbfs/v2/testatlantis/foobar").contentType("application/json"))
      .andExpect(status().isBadRequest());
  }
}
