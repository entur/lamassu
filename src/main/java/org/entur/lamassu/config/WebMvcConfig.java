/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package org.entur.lamassu.config;

import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Configuration for serving the admin UI single-page application.
 * Configures resource handling to support client-side routing by falling back
 * to index.html for non-existent resources while serving actual static files normally.
 */
@Configuration
@Profile("leader")
@ConditionalOnProperty(name = "org.entur.lamassu.enable-admin-ui", havingValue = "true")
public class WebMvcConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    // Handle /admin/ui and /admin/ui/ by forwarding to index.html
    registry.addViewController("/admin/ui").setViewName("forward:/admin/ui/index.html");
    registry.addViewController("/admin/ui/").setViewName("forward:/admin/ui/index.html");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
      .addResourceHandler("/admin/ui/**")
      .addResourceLocations("classpath:/static/admin/ui/")
      .resourceChain(true)
      .addResolver(
        new PathResourceResolver() {
          @Override
          protected Resource getResource(String resourcePath, Resource location)
            throws IOException {
            Resource requestedResource = location.createRelative(resourcePath);

            // If the resource exists and is readable (e.g., .js, .css, images), serve it
            // Otherwise, fall back to index.html to support SPA client-side routing
            return requestedResource.exists() && requestedResource.isReadable()
              ? requestedResource
              : new ClassPathResource("/static/admin/ui/index.html");
          }
        }
      );
  }
}
