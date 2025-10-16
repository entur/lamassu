package org.entur.lamassu.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving the admin UI.
 */
@Controller
@Profile("leader")
@ConditionalOnProperty(name = "org.entur.lamassu.enable-admin-ui", havingValue = "true")
public class AdminUIController {

  /**
   * Serves the admin UI for feed provider management.
   * Forwards all paths under /admin/ui to index.html to support SPA routing.
   * Static files (with extensions) are forwarded to /admin/** without the /ui/ part.
   *
   * @param request The HTTP servlet request
   * @return The forward path
   */
  @GetMapping({ "/admin/ui", "/admin/ui/**" })
  public String adminUI(HttpServletRequest request) {
    String path = request.getRequestURI();

    // Check if the path contains a file extension (e.g., .js, .css, .json, .woff)
    int lastSlash = path.lastIndexOf('/');
    int lastDot = path.lastIndexOf('.');

    if (lastDot > lastSlash && lastDot > 0) {
      // This is a static file request - forward to /admin/** (strip /ui/ from path)
      String staticPath = path.replace("/admin/ui/", "/admin/");
      return "forward:" + staticPath;
    }

    // This is a route navigation - forward to index.html for SPA routing
    return "forward:/admin/index.html";
  }
}
