package com.nucleus.finnone.pro.communicationgenerator.job;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

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
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestScheduler;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventRequestSchedulerMapping;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;

@Named("communicationEventRequestSchedulerListener")
public class CommunicationEventRequestSchedulerListener extends
        GenericEventListener {

    @Inject
    @Named("schedulerService")
    private SchedulerService schedulerService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;

    private static final String COMMUNICATION_EVENT_REQUEST_SCHEDULER = "CommunicationEventRequestScheduler";
    private static final String MODULE_SOURCE_RPODUCT = "module";
    private static final String EVENT_CODE_LIST = "eventCodeList";

    @Override
    public boolean canHandleEvent(Event event) {
        if (event instanceof MakerCheckerEvent
                && (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED
                        || event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED || event
                        .getEventType() == EventTypes.MAKER_CHECKER_DELETE|| event
                                .getEventType() == EventTypes.MAKER_CHECKER_DELETION_APPROVED)) {
            MakerCheckerEvent makerCheckerEvent = (MakerCheckerEvent) event;
            EntityId communicationEventRequestSchedulerEntityId = makerCheckerEvent
                    .getOwnerEntityId();
            if (communicationEventRequestSchedulerEntityId.getEntityClass() != null
                    && CommunicationEventRequestScheduler.class
                            .isAssignableFrom(communicationEventRequestSchedulerEntityId
                                    .getEntityClass())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        CommunicationEventRequestScheduler communicationEventRequestScheduler = baseMasterService
                .findById(CommunicationEventRequestScheduler.class,
                        (((MakerCheckerEvent) event).getOwnerEntityId())
                                .getLocalId());
        if (notNull(communicationEventRequestScheduler)) {
            SchedulerVO schedulerVO = convertToVO(communicationEventRequestScheduler);
            if (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED) {
                Map<String, Object> jobDataMap = new HashMap<String, Object>();
                jobDataMap.put(MODULE_SOURCE_RPODUCT,
                        communicationEventRequestScheduler.getSourceProduct());
                List<EventCode> eventCodeList = getEventCodeList(communicationEventRequestScheduler
                        .getCommunicationEventRequestSchedulerMappings());
                jobDataMap.put(EVENT_CODE_LIST, eventCodeList);
                try {
                    schedulerService.addJob(schedulerVO,
                            CommunicationEventRequestSchedulerJob.class,
                            jobDataMap);
                } catch (SchedulerException e) {
                    logAndThrowschedulerExceptionMessage(
                            communicationEventRequestScheduler
                                    .getSchedulerName(),
                            e);
                } catch (ParseException e) {
                    logAndThrowschedulerParseExceptionMessage(
                            communicationEventRequestScheduler
                                    .getSchedulerName(),
                            e);

                }
            } else if (event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED) {
                try {
                    schedulerService.updateJob(schedulerVO);
                } catch (SchedulerException e) {
                    logAndThrowschedulerExceptionMessage(
                            communicationEventRequestScheduler
                                    .getSchedulerName(),
                            e);

                } catch (ParseException e) {
                    logAndThrowschedulerParseExceptionMessage(
                            communicationEventRequestScheduler
                                    .getSchedulerName(),
                            e);

                }
            } else {
                try {

                    schedulerService.deleteJob(schedulerVO);
                } catch (SchedulerException e) {
                    logAndThrowschedulerExceptionMessage(
                            communicationEventRequestScheduler
                                    .getSchedulerName(),
                            e);

                }
            }

        }
    }

    protected SchedulerVO convertToVO(
            CommunicationEventRequestScheduler communicationEventRequestScheduler) {
        SchedulerVO schedulerVO = new SchedulerVO();
        schedulerVO.setJobName(communicationEventRequestScheduler
                .getSchedulerName());
        schedulerVO.setJobGroup(COMMUNICATION_EVENT_REQUEST_SCHEDULER);
        schedulerVO.setCronExpression(communicationEventRequestScheduler
                .getCronExpression());
        schedulerVO.setStartDate(new DateTime());
        schedulerVO.setEndDate(communicationEventRequestScheduler.getEndDate());
        if (communicationEventRequestScheduler.isActiveFlag()) {
            schedulerVO.setStatus(NeutrinoJob.JOB_STATUS_RUNNING);
        } else {
            schedulerVO.setStatus(NeutrinoJob.JOB_STAUS_PAUSED);
        }
        return schedulerVO;
    }

    protected List<EventCode> getEventCodeList(
            List<CommunicationEventRequestSchedulerMapping> communicationEventRequestSchedulerMappings) {
        List<EventCode> eventCodeList = new ArrayList<EventCode>();
        if (hasElements(communicationEventRequestSchedulerMappings)) {
            for (CommunicationEventRequestSchedulerMapping communicationEventRequestSchedulerMapping : communicationEventRequestSchedulerMappings) {
                eventCodeList.add(communicationEventRequestSchedulerMapping
                        .getEventCode());
            }
        }
        return eventCodeList;
    }

    protected Message logAndThrowschedulerExceptionMessage(
            String communicationEventRequestSchedulerName, Exception e) {
        Message message = new Message(
                CommunicationGeneratorConstants.SCHEDULER_EXCEPTION_MESSAGE,
                Message.MessageType.ERROR,
                COMMUNICATION_EVENT_REQUEST_SCHEDULER,
                communicationEventRequestSchedulerName);
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
            String communicationEventRequestSchedulerName, Exception e) {
        Message message = new Message(
                CommunicationGeneratorConstants.SCHEDULER_PARSE_EXCEPTION_MESSAGE,
                Message.MessageType.ERROR,
                COMMUNICATION_EVENT_REQUEST_SCHEDULER,
                communicationEventRequestSchedulerName);
        BaseLoggers.flowLogger.debug(message.getI18nCode());
        throw ExceptionBuilder
                .getInstance(BusinessException.class)
                .setOriginalException(e)
                .setMessage(message)
                .setSeverity(
                        ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue())
                .build();
    }

}
