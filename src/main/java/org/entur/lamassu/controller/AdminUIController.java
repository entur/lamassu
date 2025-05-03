package org.entur.lamassu.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving the admin UI.
 */
@Controller
@Profile("leader")
@ConditionalOnProperty(name = "org.entur.lamassu.admin-ui.enabled", havingValue = "true")
public class AdminUIController {

  /**
   * Serves the admin UI for feed provider management.
   *
   * @return The admin UI HTML page
   */
  @GetMapping("/admin/ui")
  public String adminUI() {
    return "redirect:/admin/index.html";
  }
}
