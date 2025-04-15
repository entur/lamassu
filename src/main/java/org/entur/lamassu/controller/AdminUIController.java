package org.entur.lamassu.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving the admin UI.
 */
@Controller
@Profile("leader")
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
