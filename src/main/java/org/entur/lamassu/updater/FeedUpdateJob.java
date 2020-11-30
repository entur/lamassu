package org.entur.lamassu.updater;

import org.entur.lamassu.model.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class FeedUpdateJob extends QuartzJobBean {
    @Autowired
    FeedUpdateService feedUpdateService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        var data = jobExecutionContext.getJobDetail().getJobDataMap();
        FeedProvider feedProvider = (FeedProvider) data.get("feedProvider");
        GBFS discoveryFeed = (GBFS) data.get("discoveryFeed");
        GBFSFeedName feedName = (GBFSFeedName) data.get("feedName");
        feedUpdateService.fetchFeed(feedProvider, discoveryFeed, feedName);
    }
}
