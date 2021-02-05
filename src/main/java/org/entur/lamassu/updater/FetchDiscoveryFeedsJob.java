package org.entur.lamassu.updater;


import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class FetchDiscoveryFeedsJob extends QuartzJobBean {

    private final FeedUpdateService feedUpdateService;

    @Autowired
    public FetchDiscoveryFeedsJob(FeedUpdateService feedUpdateService) {
        this.feedUpdateService = feedUpdateService;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        feedUpdateService.fetchDiscoveryFeeds();
    }
}
