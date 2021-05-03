package org.entur.lamassu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class LogConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        var loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludeHeaders(true);
        return loggingFilter;
    }
}
