package org.entur.lamassu.integration;

import org.entur.lamassu.TestLamassuApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = TestLamassuApplication.class,
        properties = "scheduling.enabled=false",
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
/*@TestPropertySource(
        properties = {
                "spring.autoconfigure.exclude=graphql.kickstart.spring.web.boot.GraphQLWebAutoConfiguration,graphql.kickstart.spring.web.boot.GraphQLWebsocketAutoConfiguration"
        }
)*/
public class GBFSRestIntegrationTest extends AbstractIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testFeedProviderDiscovery() throws Exception {
        mockMvc.perform(get("/gbfs")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operators[0].name").value("atlantis"));
    }

    @Test
    public void testGBFS() throws Exception {
        mockMvc.perform(get("/gbfs/atlantis/gbfs")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.last_updated").value(1606727710));
    }

    @Test
    public void testGBFSVersions() throws Exception {
        mockMvc.perform(get("/gbfs/atlantis/gbfs_versions")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.versions[0].version").value("2.1"));
    }

    @Test
    public void testSystemInformation() throws Exception {
        mockMvc.perform(get("/gbfs/atlantis/system_information")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.system_id").value("TST:System:Test"));
    }

    @Test
    public void testVehicleTypes() throws Exception {
        mockMvc.perform(get("/gbfs/atlantis/vehicle_types")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.vehicle_types[0].vehicle_type_id").value("TST:VehicleType:Scooter"));
    }

    @Test
    public void testFreeBikeStatus() throws Exception {
        mockMvc.perform(get("/gbfs/atlantis/free_bike_status")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bikes[0].bike_id").value("TST:Scooter:1234"));
    }

    @Test
    public void testSystemRegions() throws Exception {
        mockMvc.perform(get("/gbfs/atlantis/system_regions")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.regions[0].region_id").value("TST:Region:Sahara"));
    }

    @Test
    public void testSystemPricingPlans() throws Exception {
        mockMvc.perform(get("/gbfs/atlantis/system_pricing_plans")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plans[0].plan_id").value("TST:PricingPlan:Basic"));
    }

    @Test
    public void testStationInformation() throws Exception {
        mockMvc.perform(get("/gbfs/atlantis/station_information")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stations[0].station_id").value("TST:Station:1"));
    }

    @Test
    public void testStationStatus() throws Exception {
        mockMvc.perform(get("/gbfs/atlantis/station_status")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stations[1].station_id").value("TST:Station:2"));
    }

    @Test
    public void testSystemHours() throws Exception {
        mockMvc.perform(get("/gbfs/atlantis/system_hours")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rental_hours[0].user_types[0]").value("member"));
    }

    @Test
    public void testSystemCalendar() throws Exception {
        mockMvc.perform(get("/gbfs/atlantis/system_calendar")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.calendars[0].start_month").value(1));
    }

    @Test
    public void testSystemAlerts() throws Exception {
        mockMvc.perform(get("/gbfs/atlantis/system_alerts")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alerts[0].alert_id").value("TST:Alert:1"));
    }

    @Test
    public void testGeofencingZones() throws Exception {
        mockMvc.perform(get("/gbfs/atlantis/geofencing_zones")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.geofencing_zones.features[0].properties.name").value("Nes"));
    }
}
