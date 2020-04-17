package com.nucleus.core.scheduler.service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.quartz.SchedulerException;

import com.nucleus.service.BaseService;

public interface SchedulerService extends BaseService {
    
    public static final String SCHEDULER_SCAN_GROUP_NAME = "JobSchedulingDataLoaderPlugin";

    public List<SchedulerVO> retrieveJobs() throws SchedulerException;

    public void updateJob(SchedulerVO schedulerVO) throws SchedulerException, ParseException;

    public void deleteJob(SchedulerVO schedulerVO) throws SchedulerException;

    public void addJob(SchedulerVO schedulerVO, Class jobClass, Map jobDataMap) throws SchedulerException, ParseException;
}
