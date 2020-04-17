package com.nucleus.web.scheduler;

import java.util.List;

import com.nucleus.core.scheduler.service.SchedulerVO;

public class SchedulerForm {

    private List<SchedulerVO> jobList;
    private int               positiion;
    private String selectedJob;

    public List<SchedulerVO> getJobList() {
        return jobList;
    }

    public void setJobList(List<SchedulerVO> jobList) {
        this.jobList = jobList;
    }

    public int getPositiion() {
        return positiion;
    }

    public void setPositiion(int positiion) {
        this.positiion = positiion;
    }

    public String getSelectedJob() {
        return selectedJob;
    }

    public void setSelectedJob(String selectedJob) {
        this.selectedJob = selectedJob;
    }


}
