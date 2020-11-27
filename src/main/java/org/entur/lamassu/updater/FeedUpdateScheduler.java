package org.entur.lamassu.updater;

import org.quartz.JobBuilder;
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
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Scheduler feedUpdateQuartzScheduler;

    @Value("${org.entur.lamassu.feedupdateinterval:60}")
    private int feedUpdateInterval;

    public void start() {
        try {
            JobDetail jobDetail = buildJobDetail();
            Trigger trigger = buildJobTrigger(jobDetail);
            feedUpdateQuartzScheduler.scheduleJob(jobDetail, trigger);
            logger.info("Scheduled feed updater.");
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            feedUpdateQuartzScheduler.clear();
            logger.info("Cleared feed update scheduler");
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private JobDetail buildJobDetail() {
        return JobBuilder.newJob(UpdateFeedProvidersJob.class)
                .withIdentity("updateFeedProviders")
                .build();
    }

    private Trigger buildJobTrigger(JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(feedUpdateInterval).repeatForever().withMisfireHandlingInstructionFireNow())
                .build();
    }

}
