package com.nucleus.finnone.pro.communicationgenerator.job;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.quartz.SchedulerException;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.scheduler.NeutrinoJob;
import com.nucleus.core.scheduler.service.SchedulerService;
import com.nucleus.core.scheduler.service.SchedulerVO;
import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEventListener;
import com.nucleus.event.MakerCheckerEvent;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.AdHocEventLogSchedule;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.AdHocEventLogScheduleMapping;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;

@Named("adHocEventLogScheduleListener")
public class AdHocEventLogScheduleListener extends GenericEventListener {

	@Inject
	@Named("schedulerService")
	private SchedulerService schedulerService;

	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;

	private static final String ADHOC_EVENT_LOG_SCHEDULE = "AdHocEventLogSchedule";
	private static final String MODULE_SOURCE_RPODUCT = "module";
	private static final String EVENT_CODE_LIST = "eventCodeList";
	private static final String GENERATE_MERGED_FILE = "generateMergedFile";

	@Override
	public boolean canHandleEvent(Event event) {
		if (event instanceof MakerCheckerEvent
				&& (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED
						|| event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED || event
						.getEventType() == EventTypes.MAKER_CHECKER_DELETE
				|| event.getEventType() == EventTypes.MAKER_CHECKER_DELETION_APPROVED)) {
			MakerCheckerEvent makerCheckerEvent = (MakerCheckerEvent) event;
			EntityId adHocEventLogScheduleEntityId = makerCheckerEvent
					.getOwnerEntityId();
			if (adHocEventLogScheduleEntityId.getEntityClass() != null
					&& AdHocEventLogSchedule.class
							.isAssignableFrom(adHocEventLogScheduleEntityId
									.getEntityClass())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void handleEvent(Event event) {
		AdHocEventLogSchedule adHocEventLogSchedule = baseMasterService
				.findById(AdHocEventLogSchedule.class,
						(((MakerCheckerEvent) event).getOwnerEntityId())
								.getLocalId());
		if (notNull(adHocEventLogSchedule)) {
			runSchedulerJob(adHocEventLogSchedule, event);
		}
	}

	protected SchedulerVO convertToVO(
			AdHocEventLogSchedule adHocEventLogSchedule) {
		SchedulerVO schedulerVO = new SchedulerVO();
		schedulerVO.setJobName(adHocEventLogSchedule.getSchedulerName());
		schedulerVO.setJobGroup(ADHOC_EVENT_LOG_SCHEDULE);
		schedulerVO
				.setCronExpression(adHocEventLogSchedule.getCronExpression());
		schedulerVO.setStartDate(new DateTime());
		schedulerVO.setEndDate(adHocEventLogSchedule.getEndDate());
		if (adHocEventLogSchedule.isActiveFlag()) {
			schedulerVO.setStatus(NeutrinoJob.JOB_STATUS_RUNNING);
		} else {
			schedulerVO.setStatus(NeutrinoJob.JOB_STAUS_PAUSED);
		}
		return schedulerVO;
	}

	protected List<EventCode> getEventCodeList(
			List<AdHocEventLogScheduleMapping> adHocEventLogScheduleMappings) {
		List<EventCode> eventCodeList = new ArrayList<EventCode>();
		if (hasElements(adHocEventLogScheduleMappings)) {
			for (AdHocEventLogScheduleMapping adHocEventLogScheduleMapping : adHocEventLogScheduleMappings) {
				eventCodeList.add(adHocEventLogScheduleMapping.getEventCode());
			}
		}
		return eventCodeList;
	}

	protected Message logAndThrowschedulerExceptionMessage(
			String adHocEventLogScheduleName, Exception e) {
		Message message = new Message(
				CommunicationGeneratorConstants.SCHEDULER_EXCEPTION_MESSAGE,
				Message.MessageType.ERROR, ADHOC_EVENT_LOG_SCHEDULE,
				adHocEventLogScheduleName);
		BaseLoggers.flowLogger.debug(message.getI18nCode());
		throw ExceptionBuilder
				.getInstance(BusinessException.class)
				.setOriginalException(e)
				.setMessage(message)
				.setSeverity(
						ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue())
				.build();
	}

	protected void logAndThrowschedulerParseExceptionMessage(
			String adHocEventLogScheduleName, Exception e) {
		Message message = new Message(
				CommunicationGeneratorConstants.SCHEDULER_PARSE_EXCEPTION_MESSAGE,
				Message.MessageType.ERROR, ADHOC_EVENT_LOG_SCHEDULE,
				adHocEventLogScheduleName);
		BaseLoggers.flowLogger.debug(message.getI18nCode());
		throw ExceptionBuilder
				.getInstance(BusinessException.class)
				.setOriginalException(e)
				.setMessage(message)
				.setSeverity(
						ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue())
				.build();
	}

	protected void runSchedulerJob(AdHocEventLogSchedule adHocEventLogSchedule,
			Event event) {
		SchedulerVO schedulerVO = convertToVO(adHocEventLogSchedule);
		try {
			if (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED) {
				Map<String, Object> jobDataMap = new HashMap<String, Object>();
				jobDataMap.put(MODULE_SOURCE_RPODUCT,
						adHocEventLogSchedule.getSourceProduct());
				List<EventCode> eventCodeList = getEventCodeList(adHocEventLogSchedule
						.getAdHocEventLogScheduleMappings());
				jobDataMap.put(EVENT_CODE_LIST, eventCodeList);
				jobDataMap.put(GENERATE_MERGED_FILE,
						adHocEventLogSchedule.getGenerateMergedFile());
				schedulerService.addJob(schedulerVO,
						AdHocEventLogScheduleJob.class, jobDataMap);
			} else if (event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED) {
				schedulerService.updateJob(schedulerVO);
			} else {
				schedulerService.deleteJob(schedulerVO);
			}

		} catch (SchedulerException e) {
			logAndThrowschedulerExceptionMessage(
					adHocEventLogSchedule.getSchedulerName(), e);
		} catch (ParseException e) {
			logAndThrowschedulerParseExceptionMessage(
					adHocEventLogSchedule.getSchedulerName(), e);

		}

	}
}
