package com.nucleus.core.scheduler.service;

import com.nucleus.core.scheduler.Entity.NeutrinoJobEntity;
import com.nucleus.service.BaseService;

public interface JobService extends BaseService {

    public void saveOrUpdateJob(SchedulerVO schedulerVO);

    public NeutrinoJobEntity findJob(String jobName, String jobGroup);

    public SchedulerVO populateNotificationStatus(SchedulerVO schedulerVO);

}
