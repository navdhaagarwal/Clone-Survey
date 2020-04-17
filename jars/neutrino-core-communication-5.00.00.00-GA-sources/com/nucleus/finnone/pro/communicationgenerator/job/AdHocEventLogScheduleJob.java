package com.nucleus.finnone.pro.communicationgenerator.job;

import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.event.EventCode;
import com.nucleus.core.scheduler.NeutrinoJob;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.IAdHocEventLogCriteriaService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.SourceProduct;

@Named("adHocEventLogScheduleJob")
public class AdHocEventLogScheduleJob extends NeutrinoJob {

    private static final String MODULE_SOURCE_RPODUCT = "module";
    private static final String EVENT_CODE_LIST = "eventCodeList";
    private static final String ADHOC_EVENT_LOG_CRITERIA_SERVICE = "adHocEventLogCriteriaService";
    private static final String PRODUCT_PROCESSOR_PRE_INIT_SUPPORT_BEAN = "productProcessorPreInitSupportBean";
    private static final String GENERATE_MERGED_FILE= "generateMergedFile";

    
    @Override
    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {

        Map<String, Object> jobDataMap = context.getMergedJobDataMap();
        JobExecutionPreInitializationSupport jobExecutionPreInitializationSupport = null;
        try {
            jobExecutionPreInitializationSupport = NeutrinoSpringAppContextUtil
                    .getBeanByName(PRODUCT_PROCESSOR_PRE_INIT_SUPPORT_BEAN,
                            JobExecutionPreInitializationSupport.class);
            jobExecutionPreInitializationSupport.preInitialize();
        } catch (BeansException ex) {
            BaseLoggers.flowLogger.info(
                    "productProcessorPreInitSupportBean not configured", ex);
        }
        IAdHocEventLogCriteriaService adHocEventLogCriteriaService = NeutrinoSpringAppContextUtil
                .getBeanByName(ADHOC_EVENT_LOG_CRITERIA_SERVICE,
                        IAdHocEventLogCriteriaService.class);
        try {
            adHocEventLogCriteriaService
                    .logApplicableCommunicationEventsBasedOnEventCodesInBatch(
                            (List<EventCode>) jobDataMap.get(EVENT_CODE_LIST),
                            (SourceProduct) jobDataMap.get(MODULE_SOURCE_RPODUCT),
                            (Boolean)jobDataMap.get(GENERATE_MERGED_FILE));
        } 
        catch (BusinessException e) {
            Message message = new Message(
                    CommunicationGeneratorConstants.JOB_EXCEPTION_MESSAGE,
                    Message.MessageType.ERROR, ADHOC_EVENT_LOG_CRITERIA_SERVICE);
            BaseLoggers.flowLogger.error(message.getI18nCode(), e);
        }
        catch(Exception exception)
        {
            BaseLoggers.flowLogger.error("Exception "+exception);
        }

    }

}
