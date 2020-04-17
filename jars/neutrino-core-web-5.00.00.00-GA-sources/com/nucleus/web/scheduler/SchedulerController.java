package com.nucleus.web.scheduler;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.quartz.SchedulerException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nucleus.core.scheduler.service.SchedulerService;
import com.nucleus.core.scheduler.service.SchedulerVO;
import com.nucleus.web.common.controller.BaseController;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
public class SchedulerController extends BaseController {

    @Inject
    @Named("schedulerService")
    SchedulerService schedulerService;

    @PreAuthorize("hasAuthority('JOB_SCHEDULER')")
    @RequestMapping("/loadscheduler")
    public String loadJobs(ModelMap map) throws SchedulerException {
        SchedulerForm schedulerForm = new SchedulerForm();
        schedulerForm.setJobList(schedulerService.retrieveJobs());
        map.put("schedulerForm", schedulerForm);
        return "scheduler";
    }

    @PreAuthorize("hasAuthority('JOB_SCHEDULER')")
    @RequestMapping("/addscheduler")
    public String addJobs(ModelMap map, @ModelAttribute("schedulerVO") SchedulerVO schedulerVO) throws SchedulerException,
    ParseException {
        SchedulerForm schedulerForm = new SchedulerForm();
        // schedulerForm.setJobList(schedulerService.retrieveJobs());
        // schedulerService.addJob(schedulerVO, NewJob.class);
        map.put("schedulerForm", schedulerForm);
        return "scheduler";
    }

    @PreAuthorize("hasAuthority('JOB_SCHEDULER')")
    @RequestMapping("/updateJob/{number}")
    public String updateJob(ModelMap map, @ModelAttribute("schedulerForm") SchedulerForm schedulerForm,
            @PathVariable Integer number) throws SchedulerException, ParseException {
        schedulerService.updateJob(schedulerForm.getJobList().get(number));
        return loadJobs(map);
    }

    @PreAuthorize("hasAuthority('JOB_SCHEDULER')")
    @RequestMapping("/loadJob/{jobName}")
    public String loadJob(ModelMap map,@PathVariable String jobName) throws SchedulerException, ParseException{

        SchedulerForm schedulerForm = new SchedulerForm();
        schedulerForm.setSelectedJob(jobName);
        List<SchedulerVO> jobList=schedulerService.retrieveJobs();
        List<SchedulerVO> jobListNew= new ArrayList<SchedulerVO>();
        for(SchedulerVO job:jobList)
        {
            if(job.getJobName().equalsIgnoreCase(jobName))
            {
                jobListNew.add(job);
            }
        }
        schedulerForm.setJobList(jobListNew);
        map.put("schedulerForm", schedulerForm);

        return "jobList";

    }

}
