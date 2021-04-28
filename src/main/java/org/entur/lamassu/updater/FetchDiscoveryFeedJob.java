package org.entur.lamassu.updater;

import org.entur.lamassu.model.discovery.FeedProvider;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class FetchDiscoveryFeedJob extends QuartzJobBean {

    // This works thanks to org.entur.lamassu.util.AutowiringSpringBeanJobFactory
    @Autowired
    private FeedUpdateService feedUpdateService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        var feedProvider = (FeedProvider) jobExecutionContext.getJobDetail().getJobDataMap().get("feedProvider");
        feedUpdateService.update(feedProvider);
    }
}
