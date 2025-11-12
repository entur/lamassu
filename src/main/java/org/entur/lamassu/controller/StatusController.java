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

import java.util.List;
import java.util.stream.Collectors;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.dto.PublicFeedProviderStatus;
import org.entur.lamassu.model.dto.PublicFeedProviderStatusMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST API for feed provider status information.
 * Does not require authentication. Does not expose sensitive data.
 */
@RestController
@RequestMapping("/status")
@ConditionalOnProperty(
  name = "org.entur.lamassu.enable-status-endpoints",
  havingValue = "true"
)
public class StatusController {

  private final FeedProviderConfig feedProviderConfig;
  private final PublicFeedProviderStatusMapper mapper;

  public StatusController(
    FeedProviderConfig feedProviderConfig,
    PublicFeedProviderStatusMapper mapper
  ) {
    this.feedProviderConfig = feedProviderConfig;
    this.mapper = mapper;
  }

  /**
   * Get all feed providers with public status information.
   * @return List of public feed provider status (no sensitive data)
   */
  @GetMapping("/feed-providers")
  public ResponseEntity<List<PublicFeedProviderStatus>> getAllFeedProviders() {
    List<PublicFeedProviderStatus> publicStatuses = feedProviderConfig
      .getProviders()
      .stream()
      .map(mapper::toPublicStatus)
      .collect(Collectors.toList());
    return ResponseEntity.ok(publicStatuses);
  }

  /**
   * Get a single feed provider's public status by system ID.
   * @param systemId The system identifier
   * @return Public feed provider status or 404 if not found
   */
  @GetMapping("/feed-providers/{systemId}")
  public ResponseEntity<PublicFeedProviderStatus> getFeedProviderBySystemId(
    @PathVariable String systemId
  ) {
    var provider = feedProviderConfig.getProviderBySystemId(systemId);
    if (provider == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(mapper.toPublicStatus(provider));
  }
}
