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

import org.entur.gbfs.validation.model.FileValidationResult;
import org.entur.gbfs.validation.model.ValidationResult;
import org.entur.lamassu.model.validation.ShortFileValidationResult;
import org.entur.lamassu.model.validation.ShortValidationResult;
import org.entur.lamassu.service.FeedProviderService;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/validation")
public class ValidationController {
    private final FeedProviderService feedProviderService;
    private final RMapCache<String, ValidationResult> validationResultCache;

    @Autowired
    public ValidationController(FeedProviderService feedProviderService, RMapCache<String, ValidationResult> validationResultCache) {
        this.feedProviderService = feedProviderService;
        this.validationResultCache = validationResultCache;
    }

    @GetMapping("/systems")
    public Map<String, ShortValidationResult> getValidationResultForAllSystems() {
        Map<String, ShortValidationResult> systems = new HashMap<>();
        feedProviderService.getFeedProviders()
                .forEach(feedProvider -> {
                    var validationResult = validationResultCache.get(feedProvider.getSystemId());
                    if (validationResult != null) {
                        systems.put(feedProvider.getSystemId(), mapToShortValidationResult(validationResult));
                    }
                });
        return systems;
    }

    private ShortValidationResult mapToShortValidationResult(ValidationResult validationResult) {
        ShortValidationResult shortResult = new ShortValidationResult();
        shortResult.setSummary(validationResult.getSummary());
        shortResult.setFiles(mapToShortFileValidationResults(validationResult.getFiles()));
        return shortResult;
    }

    private Map<String, ShortFileValidationResult> mapToShortFileValidationResults(Map<String, FileValidationResult> files) {
        return files.entrySet().stream().map(entry ->
                new AbstractMap.SimpleEntry<>(
                        entry.getKey(),
                        mapToShortFileValidationResult(entry.getValue())
                )
        ).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private ShortFileValidationResult mapToShortFileValidationResult(FileValidationResult value) {
        ShortFileValidationResult shortFileValidationResult = new ShortFileValidationResult();
        shortFileValidationResult.setFile(value.getFile());
        shortFileValidationResult.setErrorsCount(value.getErrorsCount());
        shortFileValidationResult.setErrors(value.getErrors());
        shortFileValidationResult.setRequired(value.isRequired());
        shortFileValidationResult.setExists(value.isExists());
        shortFileValidationResult.setVersion(value.getVersion());
        return shortFileValidationResult;
    }

    @GetMapping("/systems/{systemId}")
    public ValidationResult getValidationResultForSystem(@PathVariable String systemId) {
        return validationResultCache.get(systemId);
    }
}
