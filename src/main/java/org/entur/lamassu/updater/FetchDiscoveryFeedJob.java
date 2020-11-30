package org.entur.lamassu.updater;

import org.entur.lamassu.model.FeedProvider;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class FetchDiscoveryFeedJob extends QuartzJobBean {
    @Autowired
    private FeedUpdateService feedUpdateService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        var data = jobExecutionContext.getJobDetail().getJobDataMap();
        FeedProvider feedProvider = (FeedProvider) data.get("feedProvider");
        feedUpdateService.fetchDiscoveryFeed(feedProvider);
    }
}
