package com.nucleus.finnone.pro.communicationgenerator.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.scheduler.NeutrinoJob;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationGeneratorService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationGenerationSchedulerJob")
@DisallowConcurrentExecution
public class CommunicationGenerationSchedulerJob extends NeutrinoJob{

	private static final String MODULE_SOURCE_RPODUCT="module";
	private static final String COMMUNICATION_LIST="communicationList";
	private static final String COMMUNICATION_GENERATION_SERVICE="communicationGeneratorService";
	private static final String PRODUCT_PROCESSOR_PRE_INIT_SUPPORT_BEAN="productProcessorPreInitSupportBean";


	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		Map<String,Object> jobDataMap=context.getMergedJobDataMap();
		JobExecutionPreInitializationSupport jobExecutionPreInitializationSupport= null;
		try{
			jobExecutionPreInitializationSupport = NeutrinoSpringAppContextUtil.getBeanByName( PRODUCT_PROCESSOR_PRE_INIT_SUPPORT_BEAN, 
					JobExecutionPreInitializationSupport.class);
			jobExecutionPreInitializationSupport.preInitialize();
		}catch(BeansException ex){
			  BaseLoggers.flowLogger.info("productProcessorPreInitSupportBean not configured");
		}
		ICommunicationGeneratorService  communicationGenerationService=  NeutrinoSpringAppContextUtil.getBeanByName(
				COMMUNICATION_GENERATION_SERVICE, ICommunicationGeneratorService.class);
		try {
			Map<Object, Object> parameters = new HashMap<Object, Object>();
			parameters.put(CommunicationGeneratorConstants.SCHEDULAR_INSTANCE_ID, context.getFireInstanceId());
			communicationGenerationService.logAndGenerateCommunicationsForCommunicationRequests(
					(List<CommunicationName>) jobDataMap.get(COMMUNICATION_LIST),
					(SourceProduct) jobDataMap.get(MODULE_SOURCE_RPODUCT), parameters);
		}
		catch (BusinessException e) {
			Message message = new Message(
					CommunicationGeneratorConstants.JOB_EXCEPTION_MESSAGE,
					Message.MessageType.ERROR,
					COMMUNICATION_GENERATION_SERVICE);
			BaseLoggers.flowLogger.debug(message.getI18nCode(),e);
		}
	}

}
