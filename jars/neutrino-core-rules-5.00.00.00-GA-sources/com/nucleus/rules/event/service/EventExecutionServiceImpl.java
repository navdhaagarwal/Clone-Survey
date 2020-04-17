/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.event.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;


import com.nucleus.core.event.interfaceRun.InterfaceWorker;
import com.nucleus.core.event.letterGeneration.LetterGenerationWorker;
import com.nucleus.eventInterfaceCode.EventInterfaceCode;
import com.nucleus.letterMaster.LetterType;
import com.nucleus.rules.model.eventDefinition.*;
import com.nucleus.rules.service.RuleGroupEvaluationService;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.event.EventDefinition;
import com.nucleus.core.event.EventExecutionResult;
import com.nucleus.core.event.EventTask;
import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.core.event.NeutrinoEventWorker;
import com.nucleus.core.event.RuleInvocationEventWorker;
import com.nucleus.core.event.RuleMatrixEventWorker;
import com.nucleus.core.event.adhocTask.AdhocTaskEventWorker;
import com.nucleus.core.event.assignment.AssignmentEventWorker;
import com.nucleus.core.event.assignment.TaskAllocationEventWorker;
import com.nucleus.core.event.notification.NotificationEventWorker;
import com.nucleus.core.event.ruleValidation.RuleValidationEventWorker;
import com.nucleus.core.event.rulebasednotification.RuleBasedNotificationEventWorker;
import com.nucleus.core.event.service.EventExecutionService;
import com.nucleus.core.event.service.EventExecutionVO;
import com.nucleus.notificationMaster.NotificationMaster;
import com.nucleus.notificationMaster.NotificationMasterType;
import com.nucleus.rules.eventdefinition.service.EventDefinitionService;
import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.model.RuleInvocationMapping;
import com.nucleus.rules.model.assignmentMatrix.AssignmentMaster;
import com.nucleus.rules.model.assignmentMatrix.TaskAssignmentMaster;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixMaster;
import com.nucleus.standard.context.INeutrinoExecutionContextHolder;
import com.nucleus.user.UserInfo;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Transactional
public class EventExecutionServiceImpl implements EventExecutionService {

    @Inject
    private NeutrinoEventPublisher neutrinoEventPublisher;

    @Inject
    @Named(value = "eventDefinitionService")
    private EventDefinitionService eventDefinitionService;
    
    
    @Inject
    @Named("neutrinoExecutionContextHolder")
    protected INeutrinoExecutionContextHolder         neutrinoExecutionContextHolder;

    @Inject
    @Named("ruleGroupEvaluationService")
    private RuleGroupEvaluationService ruleGroupEvaluationService;


    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public EventExecutionResult executeEventDefinition(EventDefinition eventDefinition, Map contextObjectMap,
            EventExecutionVO eventExecutionVO,boolean auditingEnabled,boolean purgingRequired) {

        Map<Object, Object> resultMap = new HashMap<>();
        if (eventDefinition == null || contextObjectMap == null || contextObjectMap.size() == 0) {
            return null;
        }
        List<EventTask> eventTaskList = eventDefinition.getEventTaskList();

        if (eventTaskList != null && !eventTaskList.isEmpty()) {
            if (eventDefinition.getOrderingRequired() != null && eventDefinition.getOrderingRequired() ) {
            	// fix for sorting on managed collections
            	List<EventTask> sortedEventTaskList = new ArrayList<>();
            	sortedEventTaskList.addAll(eventTaskList);
                eventDefinitionService.sortEventTaskList(sortedEventTaskList);
                eventTaskList = sortedEventTaskList;
            }
            final String uuid = UUID.randomUUID().toString();
            resultMap.put(EventCode.EVENT_EXECUTION_RESULT_TRANSACTION_ID, uuid);

            for (EventTask eventTask : eventTaskList) {

                if (eventTask instanceof NotificationTask) {
                    NotificationTask notificationTask = (NotificationTask) eventTask;
                    NotificationMaster notificationMaster = notificationTask.getNotificationMaster();
					
					if(notificationMaster.getNotificationMasterType()!=null
                            && NotificationMasterType.WARNING_TYPE_NOTIFICATION.equalsIgnoreCase(notificationMaster.getNotificationMasterType().getCode())) {
                        contextObjectMap.put("NotificationUser",getUserDetails());
                    }

					contextObjectMap.put("EventDefinition", eventDefinition);

                    if (notificationTask.getIsRuleBased()) {

                        RuleBasedNotificationEventWorker neutrinoRuleBasedNotEventWorker = new RuleBasedNotificationEventWorker(
                                "Notification Rule Based Event");
                        neutrinoRuleBasedNotEventWorker.setName("Event: " + eventDefinition.getCode() + " - Task: "
                                + eventTask.getCode());
                        neutrinoRuleBasedNotEventWorker.setUuid(uuid);
                        neutrinoRuleBasedNotEventWorker.setNotificationMaster(notificationMaster);
                        neutrinoRuleBasedNotEventWorker.setRuleGroup(notificationTask.getRuleGroup());
                        neutrinoRuleBasedNotEventWorker.setMap(contextObjectMap);
                        neutrinoRuleBasedNotEventWorker.setAuditingEnabled(auditingEnabled);
                        neutrinoRuleBasedNotEventWorker.setPurgingRequired(purgingRequired);
                        if (eventExecutionVO != null) {
                            neutrinoRuleBasedNotEventWorker.setMetadata(eventExecutionVO.getMetadata());
                        }
                        neutrinoEventPublisher.publish(neutrinoRuleBasedNotEventWorker);
                        continue;

                    }

                    NotificationEventWorker notificationEventWorker = new NotificationEventWorker("Notification Event");
                    notificationEventWorker.setName("Event: " + eventDefinition.getCode() + " - Task: "
                            + eventTask.getCode());
                    notificationEventWorker.setNotificationMaster(notificationMaster);
                    notificationEventWorker.setMap(contextObjectMap);

                    if (eventExecutionVO != null) {
                        notificationEventWorker.setMetadata(eventExecutionVO.getMetadata());
                    }

                    neutrinoEventPublisher.publish(notificationEventWorker);
                    continue;
                }

                if(eventTask instanceof LetterGenerationTask){
                    LetterGenerationTask letterGenerationTask = (LetterGenerationTask) eventTask;
                    LetterType letterType = letterGenerationTask.getLetterType();
                    LetterGenerationWorker letterGenerationWorker = new LetterGenerationWorker("Letter Generation Worker");
                    letterGenerationWorker.setName("Event: " + eventDefinition.getCode() + " - Task: " + eventTask.getCode());
                    letterGenerationWorker.setLetterType(letterType);
                    contextObjectMap.put("letterGenerationUser",getUserDetails());
                    letterGenerationWorker.setContextMap(contextObjectMap);
                    if(letterGenerationTask.getIsRuleBased()){
                        letterGenerationWorker.setRuleGroup(letterGenerationTask.getRuleGroup());
                        letterGenerationWorker.setUuid(uuid);
                        letterGenerationWorker.setAuditingEnabled(auditingEnabled);
                        letterGenerationWorker.setPurgingRequired(purgingRequired);
                        letterGenerationWorker.setRuleBased(letterGenerationTask.getIsRuleBased());
                    }
                    neutrinoEventPublisher.publish(letterGenerationWorker);
                }

                if(eventTask instanceof InterfaceTask){
                    InterfaceTask interfaceTask = (InterfaceTask) eventTask;
                    EventInterfaceCode eventInterfaceCode = interfaceTask.getInterfaceCode();
                    InterfaceWorker interfaceWorker = new InterfaceWorker("CAS Interface Worker");
                    interfaceWorker.setName("Event: " + eventDefinition.getCode() + " - Task: " + eventTask.getCode());
                    interfaceWorker.setEventInterfaceCode(eventInterfaceCode);
                    contextObjectMap.put("InterfaceRunnerUser",getUserDetails());
                    interfaceWorker.setContextMap(contextObjectMap);
                    if(interfaceTask.getIsRuleBased()){
                        interfaceWorker.setRuleGroup(interfaceTask.getRuleGroup());
                        interfaceWorker.setUuid(uuid);
                        interfaceWorker.setAuditingEnabled(auditingEnabled);
                        interfaceWorker.setPurgingRequired(purgingRequired);
                        interfaceWorker.setRuleBased(interfaceTask.getIsRuleBased());
                    }
                    neutrinoEventPublisher.publish(interfaceWorker);
                }

                if (eventTask instanceof AssignmentTask) {
                    AssignmentMaster assignmentMaster = ((AssignmentTask) eventTask).getAssignment();
                    AssignmentEventWorker worker = new AssignmentEventWorker("Assignment Event");
                    worker.setName("Event: " + eventDefinition.getCode() + " - Task: " + eventTask.getCode());
                    worker.setAssignmentMaster(assignmentMaster);
                    worker.setContextmap(contextObjectMap);
                    if(eventTask.getIsRuleBased()){
                    	worker.setRuleGroup(((AssignmentTask) eventTask).getRuleGroup());
                    	worker.setIsRuleBased(true);
                    	worker.setUuid(uuid);
                    	worker.setAuditingEnabled(auditingEnabled);
                    	worker.setPurgingRequired(purgingRequired);
                    }
                    neutrinoEventPublisher.publish(worker);
                    continue;
                }

                if (eventTask instanceof AllocationTask) {
                    TaskAssignmentMaster taskAssignmentMaster = ((AllocationTask) eventTask).getTaskAssignment();
                    TaskAllocationEventWorker worker = new TaskAllocationEventWorker("Task Assignment Event");
                    worker.setTaskAssignmentMaster(taskAssignmentMaster);
                    worker.setName("Event: " + eventDefinition.getCode() + " - Task: " + eventTask.getCode());
                    worker.setContextMap(contextObjectMap);
                    if(eventTask.getIsRuleBased()){
                    	worker.setRuleGroup(((AllocationTask) eventTask).getRuleGroup());
                    	worker.setIsRuleBased(true);
                    	worker.setUuid(uuid);
                    	worker.setAuditingEnabled(auditingEnabled);
                    	worker.setPurgingRequired(purgingRequired);
                    }
                    neutrinoEventPublisher.publish(worker);
                    resultMap.put(eventTask.getCode(), worker.getResultMap());
                    continue;
                }

                if (eventTask instanceof RuleInvocationMappingTask) {
                    RuleInvocationMapping ruleInvocationMapping = ((RuleInvocationMappingTask) eventTask)
                            .getRuleInvocationMapping();
                    RuleInvocationEventWorker neutrinoRuleEventWorker = new RuleInvocationEventWorker(
                            "RuleInvocationMapping");
                    neutrinoRuleEventWorker.setInvocationPoint(ruleInvocationMapping.getInvocationPoint());
                    neutrinoRuleEventWorker.setAuditingEnabled(auditingEnabled);
                    neutrinoRuleEventWorker.setPurgingRequired(purgingRequired);
                    neutrinoRuleEventWorker.setMap(contextObjectMap);
                    neutrinoRuleEventWorker.setName("Event: " + eventDefinition.getCode() + " - Task: "
                            + eventTask.getCode());
                    if(eventTask.getIsRuleBased()){
                        neutrinoRuleEventWorker.setRuleGroup(((RuleInvocationMappingTask) eventTask).getRuleGroup());
                        neutrinoRuleEventWorker.setIsRuleBased(true);
                        neutrinoRuleEventWorker.setUuid(uuid);
                        neutrinoRuleEventWorker.setAuditingEnabled(auditingEnabled);
                        neutrinoRuleEventWorker.setPurgingRequired(purgingRequired);
                    }
                    neutrinoEventPublisher.publish(neutrinoRuleEventWorker);
                    resultMap.put(eventTask.getCode(), neutrinoRuleEventWorker.getRuleInvocationResult());
                    continue;
                }

                if (eventTask instanceof RuleValidationTask) {
                    RuleGroup ruleGroup = ((RuleValidationTask) eventTask).getRuleGroup();
                    RuleValidationEventWorker neValidationEventWorker = new RuleValidationEventWorker(
                            "RuleInvocationMapping Validation");
                    neValidationEventWorker.setRuleGroup(ruleGroup);
                    neValidationEventWorker.setAuditingEnabled(auditingEnabled);
                    neValidationEventWorker.setPurgingRequired(purgingRequired);
                    neValidationEventWorker.setMap(contextObjectMap);
                    neValidationEventWorker.setName("Event: " + eventDefinition.getCode() + " - Task: "
                            + eventTask.getCode());
                    neValidationEventWorker.setUuid(uuid);
                    neutrinoEventPublisher.publish(neValidationEventWorker);
                    resultMap.put(eventTask.getCode(), neValidationEventWorker.getRuleGroupResult());

                }

                if (eventTask instanceof EventAdhocTask) {
                    if (((EventAdhocTask) eventTask).getIsRuleBased()) {
                        String name = "Event: " + eventDefinition.getCode() + " - Task: " + eventTask.getCode();
                        Boolean result = this.ruleGroupEvaluationService.executeRuleGroup(((EventAdhocTask)
                                        eventTask).getRuleGroup(), contextObjectMap,
                                uuid, name, auditingEnabled, purgingRequired);
                        if (null == result || !result)
                            continue;
                    }
                    AdhocTaskEventWorker adhocTaskEventWorker = new AdhocTaskEventWorker("Adhoc Task Event");
                    adhocTaskEventWorker.setAssignee(((EventAdhocTask) eventTask).getAssignee());
                    adhocTaskEventWorker.setDescription(eventTask.getDescription());
                    adhocTaskEventWorker.setTitle(((EventAdhocTask) eventTask).getTitle());
                    adhocTaskEventWorker.setOwner(((EventAdhocTask) eventTask).getOwner());

                    if (((EventAdhocTask) eventTask).getDueDate() != null) {
                        adhocTaskEventWorker.setDueDate(((EventAdhocTask) eventTask).getDueDate().toDate());
                    }

                    adhocTaskEventWorker.setTaskType(((EventAdhocTask) eventTask).getTaskType());
                    adhocTaskEventWorker.setTaskSubType(((EventAdhocTask) eventTask).getTaskSubType());
                    adhocTaskEventWorker.setPriority(((EventAdhocTask) eventTask).getPriority());
                    adhocTaskEventWorker.setTeamUri(((EventAdhocTask) eventTask).getTeamUri());
                    adhocTaskEventWorker.setName("Event: " + eventDefinition.getCode() + " - Task: " + eventTask
                            .getCode());
                    neutrinoEventPublisher.publish(adhocTaskEventWorker);

                }
                if (eventTask instanceof RuleMatrixTask) {
                    RuleMatrixMaster ruleMatrixMaster = ((RuleMatrixTask) eventTask).getRuleMatrix();
                    RuleMatrixEventWorker worker = new RuleMatrixEventWorker("Rule Matrix Event");
                    worker.setName("Event: " + eventDefinition.getCode() + " - Task: " + eventTask.getCode());
                    worker.setRuleMatrixMaster(ruleMatrixMaster);
                    worker.setContextmap(contextObjectMap);
                    if(eventTask.getIsRuleBased()){
                    	worker.setRuleGroup(((RuleMatrixTask) eventTask).getRuleGroup());
                    	worker.setIsRuleBased(true);
                    	worker.setUuid(uuid);
                    	worker.setAuditingEnabled(auditingEnabled);
                    	worker.setPurgingRequired(purgingRequired);
                    }
                    neutrinoEventPublisher.publish(worker);
                    continue;
                }
            }
        }

        return new EventExecutionResult(resultMap);
    }
    
    
    /**
     * @deprecated (Since GA2.5, To Support configurable auditing)
     */
    @Deprecated
    @Override
    public EventExecutionResult executeEventDefinition(EventDefinition eventDefinition, Map contextObjectMap,
            EventExecutionVO eventExecutionVO) {
    	
    	return executeEventDefinition(eventDefinition, contextObjectMap, eventExecutionVO, true, false);
    }
    
    @Override
    public EventExecutionResult fireEventExecution(String eventExecutionPoint, Map contextObjectMap,
            EventExecutionVO eventExecutionVO ) {
        EventDefinition eventDefinition = null;
        EventExecutionResult	eventExecutionResult = null;
        eventDefinition = eventDefinitionService.getEventDefinitionByCode(eventExecutionPoint);
        if(isNull(eventExecutionVO)){
        	eventExecutionResult = executeEventDefinition(eventDefinition, contextObjectMap, eventExecutionVO);
        }else {
        	eventExecutionResult = executeEventDefinition(eventDefinition, contextObjectMap, eventExecutionVO,eventExecutionVO.isAuditingEnabled(),eventExecutionVO.isPurgingRequired());
        }
        return eventExecutionResult;

    }
	
	public UserInfo getUserDetails() {
        
       UserInfo  userInfo= neutrinoExecutionContextHolder.getLoggedInUserDetails();
       if(userInfo!=null){
           return userInfo;
       }
       SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null && null != securityContext.getAuthentication()) {
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (UserInfo.class.isAssignableFrom(principal.getClass())) {
                userInfo = (UserInfo) principal;
            }
        }
        return userInfo;
    }


}
