package org.entur.lamassu.updater;


import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

public class FetchDiscoveryFeedsJob extends QuartzJobBean {

    // This works thanks to org.entur.lamassu.util.AutowiringSpringBeanJobFactory
    @Autowired
    private FeedUpdateService feedUpdateService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        feedUpdateService.fetchDiscoveryFeeds();
    }
}
