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
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationScheduler;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationSchedulerMapping;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;

@Named("communicationGenerationSchedulerListener")
public class CommunicationGenerationSchedulerListener extends
        GenericEventListener {

    @Inject
    @Named("schedulerService")
    private SchedulerService schedulerService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;

    private static final String COMMUNICATION_GENERATION_SCHEDULER = "CommunicationGenerationScheduler";
    private static final String MODULE_SOURCE_RPODUCT = "module";
    private static final String COMMUNICATION_LIST = "communicationList";

    @Override
    public boolean canHandleEvent(Event event) {
        if (event instanceof MakerCheckerEvent
                && (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED
                        || event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED || event
                        .getEventType() == EventTypes.MAKER_CHECKER_DELETE|| event
                                .getEventType() == EventTypes.MAKER_CHECKER_DELETION_APPROVED)) {
            MakerCheckerEvent makerCheckerEvent = (MakerCheckerEvent) event;
            EntityId communicationGenerationSchedulerEntityId = makerCheckerEvent
                    .getOwnerEntityId();
            if (communicationGenerationSchedulerEntityId.getEntityClass() != null
                    && CommunicationGenerationScheduler.class
                            .isAssignableFrom(communicationGenerationSchedulerEntityId
                                    .getEntityClass())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        CommunicationGenerationScheduler communicationGenerationScheduler = baseMasterService
                .findById(CommunicationGenerationScheduler.class,
                        (((MakerCheckerEvent) event).getOwnerEntityId())
                                .getLocalId());
        if (notNull(communicationGenerationScheduler)) {
            SchedulerVO schedulerVO = convertToVO(communicationGenerationScheduler);
            if (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED) {
                Map<String, Object> jobDataMap = new HashMap<String, Object>();
                jobDataMap.put(MODULE_SOURCE_RPODUCT,
                        communicationGenerationScheduler.getSourceProduct());
                List<CommunicationName> communicationList = getCommunicationList(communicationGenerationScheduler
                        .getCommunicationGenerationSchedulerMappings());
                jobDataMap.put(COMMUNICATION_LIST, communicationList);
                try {
                    schedulerService.addJob(schedulerVO,
                            CommunicationGenerationSchedulerJob.class,
                            jobDataMap);
                } catch (SchedulerException e) {
                    logAndThrowschedulerExceptionMessage(
                            communicationGenerationScheduler.getSchedulerName(),
                            e);
                } catch (ParseException e) {
                    logAndThrowschedulerParseExceptionMessage(
                            communicationGenerationScheduler.getSchedulerName(),
                            e);
                }
            } else if (event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED) {
                try {
                    schedulerService.updateJob(schedulerVO);
                } catch (SchedulerException e) {
                    logAndThrowschedulerExceptionMessage(
                            communicationGenerationScheduler.getSchedulerName(),
                            e);

                } catch (ParseException e) {
                    logAndThrowschedulerParseExceptionMessage(
                            communicationGenerationScheduler.getSchedulerName(),
                            e);

                }
            } else {
                try {
                    schedulerService.deleteJob(schedulerVO);
                } catch (SchedulerException e) {
                    logAndThrowschedulerExceptionMessage(
                            communicationGenerationScheduler.getSchedulerName(),
                            e);

                }
            }

        }
    }

    protected SchedulerVO convertToVO(
            CommunicationGenerationScheduler communicationGenerationScheduler) {
        SchedulerVO schedulerVO = new SchedulerVO();
        schedulerVO.setJobName(communicationGenerationScheduler
                .getSchedulerName());
        schedulerVO.setJobGroup(COMMUNICATION_GENERATION_SCHEDULER);
        schedulerVO.setCronExpression(communicationGenerationScheduler
                .getCronExpression());
        schedulerVO.setStartDate(new DateTime());
        schedulerVO.setEndDate(communicationGenerationScheduler.getEndDate());
        if (communicationGenerationScheduler.isActiveFlag()) {
            schedulerVO.setStatus(NeutrinoJob.JOB_STATUS_RUNNING);
        } else {
            schedulerVO.setStatus(NeutrinoJob.JOB_STAUS_PAUSED);
        }
        return schedulerVO;
    }

    protected List<CommunicationName> getCommunicationList(
            List<CommunicationGenerationSchedulerMapping> communicationGenerationSchedulerMappings) {
        List<CommunicationName> communicationList = new ArrayList<CommunicationName>();
        if (hasElements(communicationGenerationSchedulerMappings)) {
            for (CommunicationGenerationSchedulerMapping communicationGenerationSchedulerMapping : communicationGenerationSchedulerMappings) {
                communicationList.add(communicationGenerationSchedulerMapping
                        .getCommunication());
            }
        }
        return communicationList;
    }

    protected Message logAndThrowschedulerExceptionMessage(
            String communicationGenerationSchedulerName, Exception e) {
        Message message = new Message(
                CommunicationGeneratorConstants.SCHEDULER_EXCEPTION_MESSAGE,
                Message.MessageType.ERROR, COMMUNICATION_GENERATION_SCHEDULER,
                communicationGenerationSchedulerName);
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
            String communicationGenerationSchedulerName, Exception e) {
        Message message = new Message(
                CommunicationGeneratorConstants.SCHEDULER_PARSE_EXCEPTION_MESSAGE,
                Message.MessageType.ERROR, COMMUNICATION_GENERATION_SCHEDULER,
                communicationGenerationSchedulerName);
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
