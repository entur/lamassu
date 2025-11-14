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

package org.entur.lamassu.model.dto;

import org.entur.lamassu.leader.SubscriptionRegistry;
import org.entur.lamassu.leader.SubscriptionStatus;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.stereotype.Component;

/**
 * Maps FeedProvider domain models to public DTOs, filtering sensitive information.
 */
@Component
public class PublicFeedProviderStatusMapper {

  private final SubscriptionRegistry subscriptionRegistry;

  public PublicFeedProviderStatusMapper(SubscriptionRegistry subscriptionRegistry) {
    this.subscriptionRegistry = subscriptionRegistry;
  }

  /**
   * Convert FeedProvider to public DTO with subscription status.
   * Filters out url, authentication, and internal configuration fields.
   */
  public PublicFeedProviderStatus toPublicStatus(FeedProvider provider) {
    PublicFeedProviderStatus dto = new PublicFeedProviderStatus();
    dto.setSystemId(provider.getSystemId());
    dto.setOperatorId(provider.getOperatorId());
    dto.setOperatorName(provider.getOperatorName());
    dto.setCodespace(provider.getCodespace());
    dto.setVersion(provider.getVersion());
    dto.setEnabled(provider.getEnabled());

    // Get current subscription status from registry
    // Status is now backed by Redis, so all instances (leader and followers)
    // can access the current subscription status
    SubscriptionStatus status = subscriptionRegistry.getSubscriptionStatusBySystemId(
      provider.getSystemId()
    );
    dto.setSubscriptionStatus(status);

    return dto;
  }
}
