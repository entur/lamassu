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

package org.entur.lamassu.service;

import java.util.List;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.Operator;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.OperatorFilter;

public interface FeedProviderService {
  List<FeedProvider> getFeedProviders();
  List<Operator> getOperators(OperatorFilter operatorFilter);
  FeedProvider getFeedProviderBySystemId(String systemId);
}
