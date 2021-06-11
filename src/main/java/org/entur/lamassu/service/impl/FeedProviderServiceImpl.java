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

package org.entur.lamassu.service.impl;

import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.mapper.TranslationMapper;
import org.entur.lamassu.model.discovery.FeedProvider;
import org.entur.lamassu.model.entities.Operator;
import org.entur.lamassu.service.FeedProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FeedProviderServiceImpl implements FeedProviderService {
    private final Map<String, FeedProvider> feedProvidersBySystemSlug;
    private final Map<String, FeedProvider> feedProvidersBySystemId;
    private final List<FeedProvider> feedProviders;
    private final TranslationMapper translationMapper;

    @Autowired
    public FeedProviderServiceImpl(FeedProviderConfig feedProviderConfig, TranslationMapper translationMapper) {
        feedProviders = feedProviderConfig.getProviders();
        feedProvidersBySystemSlug = feedProviders.stream()
                .collect(Collectors.toMap(FeedProvider::getSystemSlug, fp -> fp));
        feedProvidersBySystemId = feedProviders.stream()
                .collect(Collectors.toMap(FeedProvider::getSystemId, fp -> fp));
        this.translationMapper = translationMapper;
    }

    @Override
    public List<FeedProvider> getFeedProviders() {
        return feedProviders;
    }

    @Override
    public List<Operator> getOperators() {
        return getFeedProviders().stream()
                .map(this::mapOperator)
                .distinct()
                .collect(Collectors.toList());
    }

    private Operator mapOperator(FeedProvider feedProvider) {
        var operator = new Operator();
        operator.setId(feedProvider.getOperatorId());
        operator.setName(
                translationMapper.mapSingleTranslation(
                        feedProvider.getLanguage(),
                        feedProvider.getOperatorName()
                )
        );
        return operator;
    }

    @Override
    public FeedProvider getFeedProviderBySystemSlug(String systemSlug) {
       return feedProvidersBySystemSlug
               .get(systemSlug);
    }

    @Override
    public FeedProvider getFeedProviderBySystemId(String systemId) {
        return feedProvidersBySystemId
                .get(systemId);
    }
}
