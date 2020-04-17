package com.nucleus.finnone.pro.communicationgenerator.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.event.EventCode;
import com.nucleus.core.scheduler.NeutrinoJob;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationEventLoggerService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationEventRequestSchedulerJob")
@DisallowConcurrentExecution
public class CommunicationEventRequestSchedulerJob extends NeutrinoJob{
	
	private static final String MODULE_SOURCE_RPODUCT="module";
	private static final String EVENT_CODE_LIST="eventCodeList";
	private static final String COMMUNICATION_EVENT_LOGGER_SERVICE="communicationEventLoggerService";
	private static final String PRODUCT_PROCESSOR_PRE_INIT_SUPPORT_BEAN="productProcessorPreInitSupportBean";
	
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {

		Map<String, Object> jobDataMap = context.getMergedJobDataMap();
		JobExecutionPreInitializationSupport jobExecutionPreInitializationSupport = null;
		try {
			jobExecutionPreInitializationSupport = NeutrinoSpringAppContextUtil
					.getBeanByName(PRODUCT_PROCESSOR_PRE_INIT_SUPPORT_BEAN, JobExecutionPreInitializationSupport.class);
			jobExecutionPreInitializationSupport.preInitialize();
		} catch (BeansException ex) {
			BaseLoggers.flowLogger.info("productProcessorPreInitSupportBean not configured");
		}
		ICommunicationEventLoggerService communicationEventLoggerService = NeutrinoSpringAppContextUtil
				.getBeanByName(COMMUNICATION_EVENT_LOGGER_SERVICE, ICommunicationEventLoggerService.class);
		List<EventCode> EventCodeList = (List<EventCode>) jobDataMap.get(EVENT_CODE_LIST);
		Map<Object, Object> parameters = new HashMap<Object, Object>();
		parameters.put(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID, context.getFireInstanceId());
		for (EventCode eventCode : EventCodeList) {
			List<EventCode> proxyEventCodeList = new ArrayList<EventCode>();
			proxyEventCodeList.add(eventCode);
			communicationEventLoggerService.logAndGenerateCommRequestsForLoggedEvents(proxyEventCodeList,
					(SourceProduct) jobDataMap.get(MODULE_SOURCE_RPODUCT), parameters);
		}

	}

}
