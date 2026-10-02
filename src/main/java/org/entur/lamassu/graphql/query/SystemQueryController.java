package org.entur.lamassu.graphql.query;

import java.util.List;
import java.util.Set;
import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.graphql.validation.QueryParameterValidator;
import org.entur.lamassu.model.entities.System;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class SystemQueryController {

  private final EntityReader<System> systemReader;
  private final QueryParameterValidator validationService;

  public SystemQueryController(
    EntityReader<System> systemReader,
    QueryParameterValidator validationService
  ) {
    this.systemReader = systemReader;
    this.validationService = validationService;
  }

  @QueryMapping
  public List<System> systems(@Argument List<String> ids) {
    validationService.validateSystems(ids);
    if (ids != null && !ids.isEmpty()) {
      return systemReader.getAll(Set.copyOf(ids));
    }
    return systemReader.getAll();
  }
}
