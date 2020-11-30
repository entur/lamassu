package org.entur.lamassu.updater;


import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class FeedUpdateJob extends QuartzJobBean {

    @Autowired
    private FeedUpdateService feedUpdateService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        feedUpdateService.update();
    }
}
