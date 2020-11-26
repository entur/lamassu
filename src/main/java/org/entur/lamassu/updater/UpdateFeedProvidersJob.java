package org.entur.lamassu.updater;


import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class UpdateFeedProvidersJob extends QuartzJobBean {

    @Autowired
    private UpdateFeedProvidersService updateFeedProvidersService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        updateFeedProvidersService.update();
    }
}
