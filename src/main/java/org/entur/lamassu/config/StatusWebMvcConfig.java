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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Configuration for serving the public status UI as a Single Page Application.
 * Available on all instances (not restricted to leader profile).
 */
@Configuration
@ConditionalOnProperty(name = "org.entur.lamassu.enable-status-ui", havingValue = "true")
public class StatusWebMvcConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    // Handle /status/ui and /status/ui/ by forwarding to index.html
    registry.addViewController("/status/ui").setViewName("forward:/status/ui/index.html");
    registry
      .addViewController("/status/ui/")
      .setViewName("forward:/status/ui/index.html");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
      .addResourceHandler("/status/ui/**")
      .addResourceLocations("classpath:/static/status/ui/")
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
              : new ClassPathResource("/static/status/ui/index.html");
          }
        }
      );
  }
}
