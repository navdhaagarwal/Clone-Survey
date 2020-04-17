package com.nucleus.core.scheduler.service;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.quartz.CronTrigger;
import org.quartz.TriggerKey;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.JobBuilder;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.scheduler.NeutrinoJob;
import com.nucleus.core.scheduler.SchedulerFrequencyType;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.service.BaseServiceImpl;

@Named("schedulerService")
public class SchedulerServiceImpl extends BaseServiceImpl implements SchedulerService {

    @Inject
    @Named("neutrinoScheduler")
    private SchedulerFactoryBean schedulerFactory;

    @Inject
    @Named("jobService")
    private JobService           jobService;
    private static final String TRIGGER_NOT_DEFINED_PROPERLY="Trigger is not defined properly."; 
    @Override
    public List<SchedulerVO> retrieveJobs() throws SchedulerException {
        List<SchedulerVO> jobList = new ArrayList<SchedulerVO>();
        Scheduler scheduler = null;
        if (null != schedulerFactory) {
            scheduler = schedulerFactory.getScheduler();

        }

        List<String> jobGroupNames = null;
        if (null != scheduler.getJobGroupNames()) {
            jobGroupNames = scheduler.getJobGroupNames();
            List<String> jobGroupList = new ArrayList<String>();
            jobGroupList.addAll(jobGroupNames);

            for (String jobGroup : jobGroupList) {
                List<String> jobNameListNew = new ArrayList<String>();
                
                Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroup));
                
                for(JobKey jobKey : jobKeys ){
                    jobNameListNew.add(jobKey.getGroup());
                }
                
                //jobNameListNew.addAll(Arrays.asList(scheduler.getJobNames(jobGroup)));

                for (String jobName : jobNameListNew) {
                    SchedulerVO schedulerVO = new SchedulerVO();
                    // JobDetail detail = sch.getJobDetail(jobName, jobGroup);

                    schedulerVO.setJobName(jobName);
                    schedulerVO.setJobGroup(jobGroup);
                    if (null == scheduler.getTriggersOfJob(JobKey.jobKey(jobName, jobGroup))
                            || scheduler.getTriggersOfJob(JobKey.jobKey(jobName, jobGroup)).size() == 0) {
                        schedulerVO.setStatus(NeutrinoJob.JOB_STAUS_PAUSED);
                    }

                    if (null != scheduler.getTriggersOfJob(JobKey.jobKey(jobName, jobGroup))
                            && scheduler.getTriggersOfJob(JobKey.jobKey(jobName, jobGroup)).size() > 0) {

                        for (Trigger trg : scheduler.getTriggersOfJob(JobKey.jobKey(jobName, jobGroup))) {
                            if (CronTrigger.class.isAssignableFrom(trg.getClass())) {
                                CronTrigger tr = (CronTrigger)scheduler.getTriggersOfJob(JobKey.jobKey(jobName, jobGroup)).get(0);

                                schedulerVO.setStatus(NeutrinoJob.JOB_STATUS_RUNNING);

                                schedulerVO.setStartDate(new DateTime(tr.getStartTime()));
                                schedulerVO.setEndDate(new DateTime(tr.getEndTime()));

                                String expression = tr.getCronExpression();
                                if (null != expression) {
                                    String[] exp = expression.split(" ");
                                    schedulerVO.setHourOfDay(Integer.valueOf(exp[2]));
                                    if (exp[3].equals("*") && exp[4].equals("*") && exp[5].equals("?") && exp.length == 6) {
                                        schedulerVO.setFrequency(SchedulerFrequencyType.DAILY);

                                    } else if (!exp[5].equals("?")) {
                                        schedulerVO.setFrequency(SchedulerFrequencyType.WEEKLY);
                                        schedulerVO.setDayOfWeek(Integer.valueOf(exp[5]));
                                    } else if (exp[4].equals("*") && !exp[3].equals("*")) {
                                        schedulerVO.setFrequency(SchedulerFrequencyType.MONTHLY);
                                        if (exp[3].equals("1")) {
                                            schedulerVO.setDateOfMonth("FIRST");
                                        } else if (exp[3].equals("L")) {
                                            schedulerVO.setDateOfMonth("LAST");
                                        }

                                    } else if (exp.length == 7) {
                                        schedulerVO.setFrequency(SchedulerFrequencyType.ONCE);
                                    }
                                }
                            }
                        }

                    }
                    schedulerVO = jobService.populateNotificationStatus(schedulerVO);
                    if (!jobGroup.equalsIgnoreCase(SCHEDULER_SCAN_GROUP_NAME)) {
                        jobList.add(schedulerVO);
                    }

                }

            }
        }

        return jobList;
    }

    @Override
    public void updateJob(SchedulerVO schedulerVO) throws SchedulerException, ParseException {
        Scheduler scheduler = null;
        if (null != schedulerFactory) {
            scheduler = schedulerFactory.getScheduler();
        }
        if (null == schedulerVO) {
        	return;
        }
        jobService.saveOrUpdateJob(schedulerVO);

        if (null != schedulerVO.getStatus() && schedulerVO.getStatus().equals(NeutrinoJob.JOB_STAUS_PAUSED)) {
        	unscheduleJob( schedulerVO,  scheduler);

        } else if (null != schedulerVO.getStatus() && schedulerVO.getStatus().equals(NeutrinoJob.JOB_STATUS_RUNNING)) {
        	
        	List cronTriggers = scheduler.getTriggersOfJob(JobKey.jobKey(schedulerVO.getJobName(), schedulerVO.getJobGroup()));
        	
            if (null != cronTriggers
                    && cronTriggers.size() > 0) {

            	CronTrigger cronTrigger = (CronTrigger) cronTriggers.get(0);
            	rescheduleJob(schedulerVO, cronTrigger, scheduler);

            } else if (null == cronTriggers
                    || cronTriggers.size() == 0) {
                JobDetail jd = scheduler.getJobDetail(JobKey.jobKey(schedulerVO.getJobName(), schedulerVO.getJobGroup()));

                CronTrigger cronTrigger = prepareCronTrigger(schedulerVO, scheduler);
                
                scheduler.deleteJob(JobKey.jobKey(schedulerVO.getJobName(), schedulerVO.getJobGroup()));
                if(cronTrigger.getCronExpression()!=null){
                	scheduler.scheduleJob(jd, cronTrigger);
                }else{
                	scheduler.addJob(jd, true);
                }
                

            }
        }
        

    }
    protected void unscheduleJob(SchedulerVO schedulerVO, Scheduler scheduler) throws SchedulerException{
    	List triggers = scheduler.getTriggersOfJob(JobKey.jobKey(schedulerVO.getJobName(), schedulerVO.getJobGroup()));
    	if (null != triggers
                && triggers.size() > 0) {
            CronTrigger cronTrigger = (CronTrigger) triggers.get(0);
            scheduler.unscheduleJob(TriggerKey.triggerKey(cronTrigger.getKey().getName(), cronTrigger.getKey().getGroup()));
        }
    }
    
    protected void rescheduleJob(SchedulerVO schedulerVO, CronTrigger cronTrigger, Scheduler scheduler) throws ParseException, SchedulerException{
    	
        if(schedulerVO.getCronExpression()!=null){
			  ((CronTriggerImpl)cronTrigger).setCronExpression(schedulerVO.getCronExpression());
        }else{
        	updateCronExpressionFrequencyTypeONCE(schedulerVO,cronTrigger);
            updateCronExpressionFrequencyTypeWEEKLY(schedulerVO,cronTrigger);
            updateCronExpressionFrequencyTypeMONTHLY(schedulerVO,cronTrigger);
            updateCronExpressionFrequencyTypeDAILY(schedulerVO,cronTrigger);
        }                    
        scheduler.rescheduleJob(TriggerKey.triggerKey(cronTrigger.getKey().getName(), cronTrigger.getKey().getGroup()), cronTrigger);
    }
    
    @Override
    public void deleteJob(SchedulerVO schedulerVO) throws SchedulerException {
        Scheduler scheduler = null;
        if (null != schedulerFactory) {
            scheduler = schedulerFactory.getScheduler();
        }
        if (null != schedulerVO && (null != scheduler.getJobDetail(JobKey.jobKey(schedulerVO.getJobName(), schedulerVO.getJobGroup())))) {
            scheduler.deleteJob(JobKey.jobKey(schedulerVO.getJobName(), schedulerVO.getJobGroup()));
        }

    }

//    @Override
//    public void addJob(SchedulerVO schedulerVO, Class jobClass, Map jobDataMap) throws SchedulerException, ParseException {
//        Scheduler scheduler = null;
//        if (null != schedulerFactory) {
//            scheduler = schedulerFactory.getScheduler();
//        }
//        if (!(NeutrinoJob.class.isAssignableFrom(jobClass))) {
//            throw new SystemException("Given Job Class is not a valid NeutrinoJob class");
//        } else if (scheduler.getJobDetail(schedulerVO.getJobName(), schedulerVO.getJobGroup()) != null) {
//            throw new InvalidDataException("Job already present with " + schedulerVO.getJobName() + "."
//                    + schedulerVO.getJobGroup());
//        } else {
//            JobDetail jd = new JobDetail(schedulerVO.getJobName(), schedulerVO.getJobGroup(), jobClass);
//            jd.setVolatility(false);
//            jd.setDurability(true);
//            jd.setRequestsRecovery(false);
//            jd.setJobDataMap(new JobDataMap(jobDataMap));
//            Boolean schedule = false;
//            CronTrigger ct = null;
//
//            if (null != schedulerVO.getFrequency() && null != schedulerVO.getHourOfDay()
//                    && null != schedulerVO.getStartDate()) {
//
//                if (null == scheduler.getTriggersOfJob(schedulerVO.getJobName(), schedulerVO.getJobGroup())
//                        || scheduler.getTriggersOfJob(schedulerVO.getJobName(), schedulerVO.getJobGroup()).length == 0) {
//                    ct = new CronTrigger("neutrino" + schedulerVO.getJobName() + "trigger", schedulerVO.getJobGroup(),
//                            schedulerVO.getJobName(), schedulerVO.getJobGroup());
//                } else {
//                    ct = (CronTrigger) scheduler.getTriggersOfJob(schedulerVO.getJobName(), schedulerVO.getJobGroup())[0];
//                }
//
//                if (schedulerVO.getFrequency().equalsIgnoreCase(SchedulerFrequencyType.ONCE)) {
//                    if (schedulerVO.getStartDate() != null) {
//                        int date = schedulerVO.getStartDate().getDayOfMonth();
//                        int month = schedulerVO.getStartDate().getMonthOfYear();
//                        int year = schedulerVO.getStartDate().getYear();
//                        ct.setCronExpression("0 0 " + schedulerVO.getHourOfDay() + " " + date + " " + month + " ? " + year);
//                        schedule = true;
//                    } else {
//                        schedule = false; // trigger is not defined properly
//                        BaseLoggers.webLogger.debug("trigger is not defined properly");
//                    }
//                } else if (schedulerVO.getFrequency().equalsIgnoreCase(SchedulerFrequencyType.WEEKLY)) {
//                    if (schedulerVO.getDayOfWeek() != null) {
//                        ct.setStartTime(schedulerVO.getStartDate().toDate());
//                        // ct.setEndTime(schedulerVO.getEndDate().toDate());
//                        int dayOfWeek = schedulerVO.getDayOfWeek();
//                        ct.setCronExpression("0 0 " + schedulerVO.getHourOfDay() + " * * " + dayOfWeek);
//                        schedule = true;
//                    } else {
//                        schedule = false; // trigger is not defined properly
//                        BaseLoggers.webLogger.debug("trigger is not defined properly");
//                    }
//                } else if (schedulerVO.getFrequency().equalsIgnoreCase(SchedulerFrequencyType.MONTHLY)) {
//                    ct.setStartTime(schedulerVO.getStartDate().toDate());
//                    if (null != schedulerVO.getDateOfMonth()) {
//                        if (schedulerVO.getDateOfMonth().equalsIgnoreCase("first")) {
//                            ct.setCronExpression("0 0 " + schedulerVO.getHourOfDay() + " 1 * ?");
//                        } else if (schedulerVO.getDateOfMonth().equalsIgnoreCase("last")) {
//                            ct.setCronExpression("0 0 " + schedulerVO.getHourOfDay() + " L * ?");
//                        }
//                        schedule = true;
//
//                    } else {
//                        schedule = false; // trigger is not defined properly
//                        BaseLoggers.webLogger.debug("trigger is not defined properly");
//                    }
//                } else if (schedulerVO.getFrequency().equalsIgnoreCase(SchedulerFrequencyType.DAILY)) {
//                    ct.setStartTime(schedulerVO.getStartDate().toDate());
//                    // ct.setEndTime(schedulerVO.getEndDate().toDate());
//                    ct.setCronExpression("0 0 " + schedulerVO.getHourOfDay() + " * * ?");
//                    schedule = true;
//                } else {
//                    schedule = false; // trigger is not defined properly
//                    BaseLoggers.webLogger.debug("trigger is not defined properly");
//                }
//
//            }
//            if (schedule) {
//                scheduler.scheduleJob(jd, ct);
//            } else {
//                scheduler.addJob(jd, true);
//            }
//
//        }
//
//    }
    
    @Override
    public void addJob(SchedulerVO schedulerVO, Class jobClass, Map jobDataMap) throws SchedulerException, ParseException {
        Scheduler scheduler = null;
        if (null != schedulerFactory) {
        	scheduler = schedulerFactory.getScheduler();
        }
        if (!(NeutrinoJob.class.isAssignableFrom(jobClass))) {
            throw new SystemException("Given Job Class is not a valid NeutrinoJob class");
        } else if (scheduler.getJobDetail(JobKey.jobKey(schedulerVO.getJobName(), schedulerVO.getJobGroup())) != null) {
            throw new InvalidDataException("Job already present with " + schedulerVO.getJobName() + "."
                    + schedulerVO.getJobGroup());
        } else {
        	 jobService.saveOrUpdateJob(schedulerVO);
        	 JobDetail jd = JobBuilder.newJob(jobClass).withIdentity(schedulerVO.getJobName(), schedulerVO.getJobGroup()).storeDurably().requestRecovery().usingJobData(new JobDataMap(jobDataMap)).build();
            /*JobDetail jd = new JobDetail(schedulerVO.getJobName(), schedulerVO.getJobGroup(), jobClass);
            jd.setVolatility(false);
            jd.setDurability(true);
            jd.setRequestsRecovery(false);
            jd.setJobDataMap(new JobDataMap(jobDataMap));*/
            
            CronTrigger cronTrigger = prepareCronTrigger(schedulerVO, scheduler);

            if (cronTrigger.getCronExpression()!=null) {
            	scheduler.scheduleJob(jd, cronTrigger);
            } else {
            	scheduler.addJob(jd, true);
            }

        }

    }
    
    protected CronTrigger prepareCronTrigger(SchedulerVO schedulerVO, Scheduler scheduler) throws SchedulerException, ParseException{
		
    	CronTrigger cronTrigger =null;
    	if ((null == scheduler.getTriggersOfJob(JobKey.jobKey(schedulerVO.getJobName(), schedulerVO.getJobGroup()))) || (scheduler.getTriggersOfJob(JobKey.jobKey(schedulerVO.getJobName(), schedulerVO.getJobGroup())).size() == 0)) {
			  cronTrigger = new CronTriggerImpl("neutrino" + schedulerVO.getJobName() + "trigger", schedulerVO.getJobGroup(),
		              schedulerVO.getJobName(), schedulerVO.getJobGroup());
		  } else {
			  cronTrigger = (CronTrigger) scheduler.getTriggersOfJob(JobKey.jobKey(schedulerVO.getJobName(), schedulerVO.getJobGroup())).get(0);
		  }
		  if(schedulerVO.getCronExpression()!=null){
			  ((CronTriggerImpl)cronTrigger).setCronExpression(schedulerVO.getCronExpression());
		  }else if (null != schedulerVO.getFrequency() && null != schedulerVO.getHourOfDay()
		           && null != schedulerVO.getStartDate()) {
			  updateCronExpressionFrequencyTypeONCE(schedulerVO,cronTrigger);
			  updateCronExpressionFrequencyTypeWEEKLY(schedulerVO,cronTrigger);
			  updateCronExpressionFrequencyTypeMONTHLY(schedulerVO,cronTrigger);
			  updateCronExpressionFrequencyTypeDAILY(schedulerVO,cronTrigger);
		  }
		  
		return cronTrigger;

    }
	protected void updateCronExpressionFrequencyTypeONCE(SchedulerVO schedulerVO, CronTrigger cronTrigger) throws ParseException{
		 if (schedulerVO.getFrequency().equalsIgnoreCase(SchedulerFrequencyType.ONCE)) {
             if (schedulerVO.getStartDate() != null) {
                 int date = schedulerVO.getStartDate().getDayOfMonth();
                 int month = schedulerVO.getStartDate().getMonthOfYear();
                 int year = schedulerVO.getStartDate().getYear();
                ((CronTriggerImpl)cronTrigger).setCronExpression("0 0 " + schedulerVO.getHourOfDay() + " " + date + " " + month + " ? " + year);
                 
             } else {                 
                 BaseLoggers.webLogger.debug(TRIGGER_NOT_DEFINED_PROPERLY);
             }
         }
		
	}
	
	protected void updateCronExpressionFrequencyTypeWEEKLY(SchedulerVO schedulerVO, CronTrigger cronTrigger) throws ParseException{
		 if (schedulerVO.getFrequency().equalsIgnoreCase(SchedulerFrequencyType.WEEKLY)) {
             if (schedulerVO.getDayOfWeek() != null) {
                ((CronTriggerImpl)cronTrigger).setStartTime(schedulerVO.getStartDate().toDate());
                 //cronTrigger.setEndTime(schedulerVO.getEndDate().toDate());
                 int dayOfWeek = schedulerVO.getDayOfWeek();
                ((CronTriggerImpl)cronTrigger).setCronExpression("0 0 " + schedulerVO.getHourOfDay() + " * * " + dayOfWeek);
                 
             } else {                 
                 BaseLoggers.webLogger.debug(TRIGGER_NOT_DEFINED_PROPERLY);                 
             }
         }
	}
	protected void updateCronExpressionFrequencyTypeMONTHLY(SchedulerVO schedulerVO, CronTrigger cronTrigger) throws ParseException{
		  if (schedulerVO.getFrequency().equalsIgnoreCase(SchedulerFrequencyType.MONTHLY)) {
              ((CronTriggerImpl)cronTrigger).setStartTime(schedulerVO.getStartDate().toDate());
               if (null != schedulerVO.getDateOfMonth()) {
                   if (schedulerVO.getDateOfMonth().equalsIgnoreCase("first")) {
                      ((CronTriggerImpl)cronTrigger).setCronExpression("0 0 " + schedulerVO.getHourOfDay() + " 1 * ?");
                   } else if (schedulerVO.getDateOfMonth().equalsIgnoreCase("last")) {
                      ((CronTriggerImpl)cronTrigger).setCronExpression("0 0 " + schedulerVO.getHourOfDay() + " L * ?");
                   }
                   

               } else {                   
                   BaseLoggers.webLogger.debug(TRIGGER_NOT_DEFINED_PROPERLY);                  
               }
          } 
		  
	}
	protected void updateCronExpressionFrequencyTypeDAILY(SchedulerVO schedulerVO, CronTrigger cronTrigger) throws ParseException{
		
		if (schedulerVO.getFrequency().equalsIgnoreCase(SchedulerFrequencyType.DAILY)) {
            ((CronTriggerImpl)cronTrigger).setStartTime(schedulerVO.getStartDate().toDate());
             //cronTrigger.setEndTime(schedulerVO.getEndDate().toDate());
            ((CronTriggerImpl)cronTrigger).setCronExpression("0 0 " + schedulerVO.getHourOfDay() + " * * ?");
             
         } else {
             
             BaseLoggers.webLogger.debug(TRIGGER_NOT_DEFINED_PROPERLY);
         }
	}
}
