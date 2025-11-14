/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.entur.lamassu.TestLamassuApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles({ "test", "leader" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(
  classes = TestLamassuApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@TestPropertySource(
  properties = {
    "org.entur.lamassu.enable-admin-endpoints=true",
    "org.entur.lamassu.enable-status-endpoints=true",
  }
)
class SecurityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testStatusEndpointsArePublic() throws Exception {
    // Status endpoints should not require authentication
    mockMvc.perform(get("/status/feed-providers")).andExpect(status().isOk());
  }

  @Test
  void testAdminEndpointsRequireAuthentication() throws Exception {
    // Admin endpoints should require authentication
    mockMvc.perform(get("/admin/feed-providers")).andExpect(status().isUnauthorized());
  }

  @Test
  void testValidationEndpointsArePublic() throws Exception {
    // Validation endpoints should be public (existing behavior)
    mockMvc.perform(get("/validation/systems")).andExpect(status().isOk());
  }
}
