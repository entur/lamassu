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

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.entur.gbfs.validation.model.FileValidationResult;
import org.entur.gbfs.validation.model.ValidationResult;
import org.entur.lamassu.model.validation.ShortFileValidationResult;
import org.entur.lamassu.model.validation.ShortValidationResult;
import org.entur.lamassu.service.FeedProviderService;
import org.redisson.api.RListMultimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/validation")
public class ValidationController {

  private final FeedProviderService feedProviderService;
  private final RListMultimap<String, ValidationResult> validationResultsCache;

  @Autowired
  public ValidationController(
    FeedProviderService feedProviderService,
    RListMultimap<String, ValidationResult> validationResultsCache
  ) {
    this.feedProviderService = feedProviderService;
    this.validationResultsCache = validationResultsCache;
  }

  @GetMapping("/systems")
  public Map<String, ShortValidationResult> getValidationResultForAllSystems() {
    Map<String, ShortValidationResult> systems = new HashMap<>();
    feedProviderService
      .getFeedProviders()
      .forEach(feedProvider -> {
        var validationResults = validationResultsCache.get(feedProvider.getSystemId());
        var validationResult = validationResults.get(validationResults.size() - 1);
        if (validationResult != null) {
          systems.put(
            feedProvider.getSystemId(),
            mapToShortValidationResult(validationResult)
          );
        }
      });
    return systems;
  }

  @GetMapping("/systems/{systemId}")
  public List<ShortValidationResult> getValidationResultsForSystem(
    @PathVariable String systemId
  ) {
    var validationResults = validationResultsCache.getAll(systemId);
    return validationResults
      .stream()
      .map(this::mapToShortValidationResult)
      .collect(Collectors.toList());
  }

  @GetMapping("/systems/{systemId}/{index}")
  public ValidationResult getValidationResultForSystem(
    @PathVariable String systemId,
    @PathVariable int index
  ) {
    var validationResults = validationResultsCache.get(systemId);
    return validationResults.get(index);
  }

  private ShortValidationResult mapToShortValidationResult(
    ValidationResult validationResult
  ) {
    ShortValidationResult shortResult = new ShortValidationResult();
    shortResult.setSummary(validationResult.summary());
    shortResult.setFiles(mapToShortFileValidationResults(validationResult.files()));
    return shortResult;
  }

  private Map<String, ShortFileValidationResult> mapToShortFileValidationResults(
    Map<String, FileValidationResult> files
  ) {
    return files
      .entrySet()
      .stream()
      .map(entry ->
        new AbstractMap.SimpleEntry<>(
          entry.getKey(),
          mapToShortFileValidationResult(entry.getValue())
        )
      )
      .collect(
        Collectors.toMap(
          AbstractMap.SimpleEntry::getKey,
          AbstractMap.SimpleEntry::getValue
        )
      );
  }

  private ShortFileValidationResult mapToShortFileValidationResult(
    FileValidationResult value
  ) {
    ShortFileValidationResult shortFileValidationResult = new ShortFileValidationResult();
    shortFileValidationResult.setFile(value.file());
    shortFileValidationResult.setErrorsCount(value.errorsCount());
    shortFileValidationResult.setErrors(value.errors());
    shortFileValidationResult.setRequired(value.required());
    shortFileValidationResult.setExists(value.exists());
    shortFileValidationResult.setVersion(value.version());
    return shortFileValidationResult;
  }
}
