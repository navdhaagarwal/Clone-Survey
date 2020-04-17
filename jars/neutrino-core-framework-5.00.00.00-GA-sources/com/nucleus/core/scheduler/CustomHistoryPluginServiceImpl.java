package com.nucleus.core.scheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.nucleus.core.scheduler.Entity.NeutrinoJobEntity;
import com.nucleus.core.scheduler.service.JobService;
import com.nucleus.core.scheduler.service.SchedulerService;
import com.nucleus.notificationMaster.service.NotificationMasterService;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.UserProfile;
import com.nucleus.user.UserService;

@Named(value = "customHistoryPluginService")
@MonitoredWithSpring(name = "customHistoryPluginService_IMPL_")
public class CustomHistoryPluginServiceImpl extends BaseServiceImpl implements CustomHistoryPluginService,
        ApplicationContextAware {

    final static String                      logDatePattern = "dd-MM-yyyy HH:mm:ss.SSS";

    private static NotificationMasterService notificationMasterService;

    private static JobService                jobService;

    private static UserService               userService;

    /* (non-Javadoc) @see com.nucleus.core.scheduler.CustomHistoryPluginService#triggerCompletedService(org.quartz.Trigger, org.quartz.JobExecutionContext) */
    @Override
    public void triggerCompletedService(Trigger trigger, JobExecutionContext context) {
        String jobName = trigger.getKey().getName();
        String jobGroup = trigger.getKey().getGroup();

        if (jobGroup.equalsIgnoreCase(SchedulerService.SCHEDULER_SCAN_GROUP_NAME))
            return;

        NeutrinoJobEntity jobDetailEntity = getJobService().findJob(jobName, jobGroup);
        if (jobDetailEntity != null && (jobDetailEntity.getEmailNotification() || jobDetailEntity.getSmsNotification())) {
            if (jobDetailEntity.getEmailNotification()) {
                Date startTime = context.getFireTime();
                String currentTreadName = Thread.currentThread().getName();

                Long timeDiff = context.getNextFireTime() != null ? (context.getNextFireTime().getTime() - context
                        .getFireTime().getTime()) : (context.getPreviousFireTime() != null ? (context.getFireTime()
                        .getTime() - context.getPreviousFireTime().getTime()) : null);

                sendEmailNotification(trigger, startTime, currentTreadName, timeDiff, jobDetailEntity.getUserId());
            }

            if (jobDetailEntity.getSmsNotification()) {
                String logfile = trigger.getKey().toString() + " is completed";
                sendSMSNotification(logfile, jobDetailEntity);
            }
        }
    }

    private void sendSMSNotification(String messageBody, NeutrinoJobEntity jobDetailEntity) {
        try {
            String phoneNumber = userService.getUserMobileNumber(jobDetailEntity.getUserId()).getPhoneNumber();
            if (phoneNumber == null || phoneNumber.isEmpty())
                return;
            Set<String> phoneList = new HashSet<String>();
            phoneList.add(phoneNumber);
            notificationMasterService.processSMSNotificationTask(phoneList, messageBody, null);
        } catch (Exception e) {

        }
    }

    public void sendEmailNotification(Trigger trigger, Date startTime, String currentTreadName, Long timeDiff, Long userId) {
        String currentLog = null;
        try {
            currentLog = getCurrentLog(currentTreadName, startTime, timeDiff, trigger.getKey().toString());
        } catch (Exception e) {
            // no logs, no mails :)
            return;
        }
        Map<Object, Object> cmap = new HashMap<Object, Object>();

        try {
            String userMailById = userService.getUserMailById(userId);
            if (userMailById == null || userMailById.isEmpty())
                return;
            Set<String> mailList = new HashSet<String>();
            mailList.add(userMailById);
            String subject = "[" + trigger.getKey().getGroup() + "." + trigger.getKey().getName() + "] Status";
            notificationMasterService.sendManualEmail(subject, currentLog, mailList, null);
        } catch (Exception e) {

        }
    }

    private String getCurrentLog(String currentTreadName, Date firstOccurSeq, Long timeDiff, String fullName)
            throws IOException {
        StringBuffer currentLog = new StringBuffer();
        File file;
        FileReader reader = null;
        BufferedReader br = null;
        try {
            file = new File("target/logs/quartz-scheduler.log");

            reader = new FileReader(file);
            br = new BufferedReader(reader);

            // to reach the starting of log
            String line;
            do {
                line = br.readLine();
                // log line already contains thread name and trigger fullname
                if (line.contains(currentTreadName) && line.contains(fullName)) {
                    boolean flag = true;
                    // to check if same thread is used to trigger same trigger, checking by timedifference
                    if (timeDiff != null) {
                        Date lineOccur = new SimpleDateFormat(logDatePattern).parse(line.substring(0,
                                logDatePattern.length()));
                        long tempDiff = Math.abs(lineOccur.getTime() - firstOccurSeq.getTime());
                        if (tempDiff > timeDiff) {
                            flag = false;
                        }
                    }
                    if (flag) {
                        break;
                    }
                }

            } while (line != null);

            if (line == null) {
                // no log present
            }

            while (line != null) {

                try {
                    //parsable. Not an exception
                    new SimpleDateFormat(logDatePattern).parse(line.substring(0, logDatePattern.length()));
                    if(!line.contains(currentTreadName)){
                        break;
                    }
                } catch (Exception e) {
                    //exception log will be read
                }

                currentLog.append(line + "<br/>");
                line = br.readLine();
            }

            // use currentLog;

        } catch (FileNotFoundException e) {
            // do nothing.
        } catch (IOException e) {
            // no nothing.
        } catch (Exception e) {
            // do nothing.
        } finally {
        	if(br != null)
            br.close();
        	if(reader != null)
            reader.close();
        }
        return currentLog.toString();
    }

    /* (non-Javadoc) @see com.nucleus.core.scheduler.CustomHistoryPluginService#triggerFiredService(org.quartz.Trigger, org.quartz.JobExecutionContext) */
    @Override
    public void triggerFiredService(Trigger trigger, JobExecutionContext context) {
        String jobName = trigger.getKey().getName();
        String jobGroup = trigger.getKey().getGroup();
        if (jobGroup.equalsIgnoreCase(SchedulerService.SCHEDULER_SCAN_GROUP_NAME))
            return;
        NeutrinoJobEntity jobDetailEntity = getJobService().findJob(jobName, jobGroup);
        if (jobDetailEntity != null && jobDetailEntity.getSmsNotification().booleanValue()) {
            String logfile = trigger.getKey().toString() + " is started";
            sendSMSNotification(logfile, jobDetailEntity);
        }
    }

    public static JobService getJobService() {
        return jobService;
    }

    public static void setJobService(JobService jobService) {
        CustomHistoryPluginServiceImpl.jobService = jobService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        notificationMasterService = (NotificationMasterService) applicationContext.getBean("notificationMasterService");
        jobService = (JobService) applicationContext.getBean("jobService");
        userService = (UserService) applicationContext.getBean("userService");
    }
}
