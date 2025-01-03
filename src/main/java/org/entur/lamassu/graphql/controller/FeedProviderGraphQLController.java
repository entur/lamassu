package org.entur.lamassu.graphql.controller;

import java.util.Collection;
import org.entur.lamassu.model.entities.Operator;
import org.entur.lamassu.service.FeedProviderService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class FeedProviderGraphQLController extends BaseGraphQLController {

  private final FeedProviderService feedProviderService;

  public FeedProviderGraphQLController(FeedProviderService feedProviderService) {
    super(feedProviderService);
    this.feedProviderService = feedProviderService;
  }

  @QueryMapping
  public Collection<String> codespaces() {
    return feedProviderService.getCodespaces();
  }

  @QueryMapping
  public Collection<Operator> operators() {
    return feedProviderService.getOperators();
  }
}
