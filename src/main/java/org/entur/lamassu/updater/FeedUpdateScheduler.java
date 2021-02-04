package org.entur.lamassu.updater;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeedUpdateScheduler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Scheduler feedUpdateQuartzScheduler;


    @Value("${org.entur.lamassu.feedupdateinterval:30}")
    private int feedUpdateInterval;

    public void start() {
        try {
            JobDetail jobDetail = buildJobDetail(FetchDiscoveryFeedsJob.class, "fetchDiscoveryFeeds", new JobDataMap());
            Trigger trigger = buildJobTrigger(jobDetail, getFeedUpdateScheduleBuilder());
            feedUpdateQuartzScheduler.scheduleJob(jobDetail, trigger);
            logger.debug("Scheduled fetch discovery feeds");
        } catch (SchedulerException e) {
            logger.warn("Failed to schedule fetch discovery feeds", e);
        }
    }

    public void stop() {
        try {
            feedUpdateQuartzScheduler.clear();
            logger.info("Cleared feed update scheduler");
        } catch (SchedulerException e) {
            logger.warn("Failed to clear feed update scheduler", e);
        }
    }

    private JobDetail buildJobDetail(Class<? extends Job> jobType, String description, JobDataMap jobData) {
        return JobBuilder.newJob(jobType)
                .withIdentity(description)
                .setJobData(jobData)
                .build();
    }

    private Trigger buildJobTrigger(JobDetail jobDetail, SimpleScheduleBuilder scheduleBuilder) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .startNow()
                .withSchedule(scheduleBuilder)
                .build();
    }

    private SimpleScheduleBuilder getFeedUpdateScheduleBuilder() {
        return SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(feedUpdateInterval)
                .repeatForever()
                .withMisfireHandlingInstructionFireNow();
    }
}
