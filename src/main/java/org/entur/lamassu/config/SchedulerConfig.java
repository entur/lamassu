package org.entur.lamassu.config;

import org.entur.lamassu.util.AutowiringSpringBeanJobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.PostConstruct;

@Configuration
@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class SchedulerConfig {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void init() {
        logger.debug("QuartzConfig initialized.");
    }

    @Bean
    public SchedulerFactoryBean feedUpdateQuartzScheduler(@Autowired ApplicationContext applicationContext) {
        SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();

        quartzScheduler.setOverwriteExistingJobs(true);
        quartzScheduler.setSchedulerName("lamassu-quartz-scheduler");

        // custom job factory of spring with DI support for @Autowired!
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        quartzScheduler.setJobFactory(jobFactory);

        return quartzScheduler;
    }
}
