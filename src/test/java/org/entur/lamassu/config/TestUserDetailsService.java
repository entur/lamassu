package org.entur.lamassu.config;

import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Test user details service that configures an admin user for integration tests.
 * This configuration is only active when the "test" profile is active.
 */
@Configuration
@Profile("test")
public class TestUserDetailsService {

  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails adminUser = User
      .builder()
      .username("admin")
      .password(passwordEncoder.encode("admin"))
      .roles("ADMIN")
      .build();

    return new InMemoryUserDetailsManager(Collections.singletonList(adminUser));
  }
}
