package org.entur.lamassu.util;

import java.util.Objects;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

public class YamlPropertySourceFactory implements PropertySourceFactory {

  @Override
  public PropertySource<?> createPropertySource(
    String s,
    EncodedResource encodedResource
  ) {
    var factory = new YamlPropertiesFactoryBean();
    factory.setResources(encodedResource.getResource());

    var properties = factory.getObject();

    if (properties != null) {
      return new PropertiesPropertySource(
        Objects.requireNonNull(encodedResource.getResource().getFilename()),
        properties
      );
    } else {
      return null;
    }
  }
}
