package com.nucleus.core.scheduler.service;

import java.util.List;

import javax.inject.Named;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.scheduler.NeutrinoJob;
import com.nucleus.core.scheduler.Entity.NeutrinoJobEntity;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.service.BaseServiceImpl;

@Named("jobService")
public class JobServiceImpl extends BaseServiceImpl implements JobService {

    @Override
    public void saveOrUpdateJob(SchedulerVO schedulerVO) {

        NeutrinoJobEntity job = this.findJob(schedulerVO.getJobName(), schedulerVO.getJobGroup());

        if (job == null) {
            job = new NeutrinoJobEntity();
            job.setJobName(schedulerVO.getJobName());
            job.setJobGroup(schedulerVO.getJobGroup());
            job.setEmailNotification(schedulerVO.isEmailEnabled());
            job.setSmsNotification(schedulerVO.isSmsEnabled());
            job.setUserId(getCurrentUser().getUserReference().getId());
            entityDao.persist(job);

        } else if ((null != job && null != schedulerVO.getStatus() && schedulerVO.getStatus().equals(
                NeutrinoJob.JOB_STAUS_PAUSED))) {
            job.setUserId(getCurrentUser().getUserReference().getId());
            job.setEmailNotification(false);
            job.setSmsNotification(false);
            entityDao.update(job);
        } else if (null != job) {
            job.setUserId(getCurrentUser().getUserReference().getId());
            job.setEmailNotification(schedulerVO.isEmailEnabled());
            job.setSmsNotification(schedulerVO.isSmsEnabled());
            entityDao.update(job);
        }

    }

    @Override
    public NeutrinoJobEntity findJob(String jobName, String jobGroup) {

        JPAQueryExecutor<NeutrinoJobEntity> executor = new JPAQueryExecutor<NeutrinoJobEntity>(
                "FROM NeutrinoJobEntity nj WHERE nj.jobName=:jobName AND nj.jobGroup=:jobGroup").addParameter("jobName",
                jobName).addParameter("jobGroup", jobGroup);
        List<NeutrinoJobEntity> job = entityDao.executeQuery(executor);
        if (null != job && job.size() == 1) {
            return job.get(0);
        } else if (null != job && job.size() > 1) {
            throw new SystemException("Non unique combination of jobName and jobGroup Found");
        } else
            return null;
    }

    @Override
    public SchedulerVO populateNotificationStatus(SchedulerVO schedulerVO) {

        NeutrinoJobEntity job = this.findJob(schedulerVO.getJobName(), schedulerVO.getJobGroup());

        if (job == null) {
            schedulerVO.setEmailEnabled(false);
            schedulerVO.setSmsEnabled(false);

        } else {
            if (null != job.getEmailNotification())
                schedulerVO.setEmailEnabled(job.getEmailNotification());
            if (null != job.getSmsNotification())
                schedulerVO.setSmsEnabled(job.getSmsNotification());
        }

        return schedulerVO;
    }

}
