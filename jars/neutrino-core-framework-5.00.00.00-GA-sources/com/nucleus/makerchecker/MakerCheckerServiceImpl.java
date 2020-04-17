/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.makerchecker;

import static com.nucleus.approval.ProcessDrivenFlowStates.COMPLETED;
import static com.nucleus.approval.ProcessDrivenFlowStates.IN_PROGRESS;
import static com.nucleus.core.common.NeutrinoComparators.CREATION_TIME_STAMP_COMPARATOR;
import static com.nucleus.entity.ApprovalStatus.APPROVED;
import static com.nucleus.entity.ApprovalStatus.APPROVED_DELETED;
import static com.nucleus.entity.ApprovalStatus.APPROVED_DELETED_IN_PROGRESS;
import static com.nucleus.entity.ApprovalStatus.APPROVED_MODIFIED;
import static com.nucleus.entity.ApprovalStatus.UNAPPROVED;
import static com.nucleus.entity.ApprovalStatus.UNAPPROVED_ADDED;
import static com.nucleus.entity.ApprovalStatus.UNAPPROVED_HISTORY;
import static com.nucleus.entity.ApprovalStatus.UNAPPROVED_MODIFIED;
import static com.nucleus.entity.ApprovalStatus.WORFLOW_IN_PROGRESS;
import static com.nucleus.entity.CloneOptionConstants.COPY_CLONING_OPTION;
import static com.nucleus.entity.CloneOptionConstants.MAKER_CHECKER_CLONING_OPTION;
import static com.nucleus.event.EventTypes.MAKER_CHECKER_APPROVED;
import static com.nucleus.event.EventTypes.MAKER_CHECKER_CREATE_EVENT;
import static com.nucleus.event.EventTypes.MAKER_CHECKER_DELETE;
import static com.nucleus.event.EventTypes.MAKER_CHECKER_DELETION_APPROVED;
import static com.nucleus.event.EventTypes.MAKER_CHECKER_DELETION_REJECTED;
import static com.nucleus.event.EventTypes.MAKER_CHECKER_MARKED_FOR_DELETION;
import static com.nucleus.event.EventTypes.MAKER_CHECKER_REJECTED;
import static com.nucleus.event.EventTypes.MAKER_CHECKER_SEND_BACK;
import static com.nucleus.event.EventTypes.MAKER_CHECKER_SEND_FOR_APPROVAL;
import static com.nucleus.event.EventTypes.MAKER_CHECKER_UPDATED_APPROVED;
import static com.nucleus.event.EventTypes.MAKER_CHECKER_UPDATED_REJECTED;
import static com.nucleus.event.EventTypes.MAKER_CHECKER_UPDATE_EVENT;
import static com.nucleus.event.EventTypes.USER_ACTIVATED_EVENT;
import static com.nucleus.event.EventTypes.USER_BLOCKED_EVENT;
import static com.nucleus.event.EventTypes.USER_CREATED_EVENT;
import static com.nucleus.event.EventTypes.USER_INACTIVATED_EVENT;
import static com.nucleus.makerchecker.MasterApprovalFlowConstants.AUTOAPPROVAL_WORKFLOW_DEFINITION_ID;
import static com.nucleus.makerchecker.MasterApprovalFlowConstants.WORKFLOW_DEFINITION_ID;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import javax.servlet.http.HttpServletRequest;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.entity.*;
import com.nucleus.master.adminactivityreport.service.AdminActivityReportGenerator;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.standard.context.INeutrinoExecutionContextHolder;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.FacesRequestAttributes;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nucleus.approval.ApprovalTask;
import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.authenticationToken.PasswordResetToken;
import com.nucleus.authority.Authority;
import com.nucleus.authority.AuthorityCodes;
import com.nucleus.core.dynamicform.service.FormService;
import com.nucleus.core.event.EventCode;
import com.nucleus.core.event.EventExecutionResult;
import com.nucleus.core.event.service.EventExecutionService;
import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.formsConfiguration.MasterDynamicForm;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.user.event.UserEvent;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.event.EventBus;
import com.nucleus.event.EventTypes;
import com.nucleus.event.MakerCheckerEvent;
import com.nucleus.event.MakerCheckerHelper;
import com.nucleus.event.MakerCheckerPreSnapshoptEvent;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.audit.service.MasterChangeAuditLogGenerator;
import com.nucleus.master.marker.HistoryOptimizable;
import com.nucleus.persistence.BaseMasterDao;
import com.nucleus.persistence.EntityDao;
import com.nucleus.process.BPMNProcessService;
import com.nucleus.process.beans.EntityApprovalPreProcessor;
import com.nucleus.process.beans.EntityApprovalPreProcessorRegistry;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.task.Task;
import com.nucleus.task.TaskService;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserSecurityService;
import com.nucleus.user.UserService;
import com.nucleus.user.UserSessionManagerService;
import com.nucleus.user.UserStatus;

import net.bull.javamelody.MonitoredWithSpring;

/**
 * @author amit.parashar
 * @author praveen.jain
 */
@Transactional
@Named("makerCheckerService")
public class MakerCheckerServiceImpl extends BaseServiceImpl implements MakerCheckerService {

    protected static final String         MASTER_APPROVE_TASK = "masterApproveTask";

    protected static final String         USER_URI            = "userUri";

    private static final String WORKFLOW_CONFIGURATION = "WorkflowConfiguration";
    private static final String DYNAMIC_WORKFLOW_CONFIGURATION = "DynamicWorkflowConfiguration";
    private final static Integer		ORACLE_LIMIT_FOR_IN_CLAUSE_ELEMENTS	= 998;
    public static final String WORKFLOW_ACTION_MAP = "WORKFLOW_ACTION_MAP";

    @Inject
    @Named("entityDao")
    protected EntityDao                   entityDao;

    @Inject
    @Named("platformTaskService")
    protected TaskService                 taskService;

    @Inject
    @Named("baseMasterDao")
    protected BaseMasterDao               baseMasterDao;

    @Inject
    @Named("bpmnProcessService")
    protected BPMNProcessService          bpmnProcessService;

    @Inject
    @Named(value = "eventBus")
    protected EventBus                    eventBus;

    @Inject
    @Named(value = "mailService")
    protected MailService                 mailService;

    @Inject
    @Named(value = "eventExecutionService")
    protected EventExecutionService       eventExecutionService;

    @Inject
    @Named(value = "makerCheckerHelper")
    protected MakerCheckerHelper   makerCheckerHelper;

    @Inject
    @Named(value = "userService")
    protected UserService                 userService;
    
    @Inject
    @Named(value = "masterConfigurationRegistry")
    protected MasterConfigurationRegistry masterConfigurationRegistry;
    
    @Value(value = "#{'${usedefaultmakercheckerassignment}'}")
    protected boolean                     useDefaultStrategy;
    
    @Inject
    @Named("entityApprovalPreProcessorRegistry")
    private EntityApprovalPreProcessorRegistry entityApprovalPreProcessorRegistry;

    @Inject
    @Named("formConfigService")
    private FormService formService;
    
    @Inject
    @Named("masterChangeAuditLogGenerator")
    private MasterChangeAuditLogGenerator auditGenerator;

    @Inject
    @Named("adminActivityReportGenerator")
    private AdminActivityReportGenerator adminActivityReportGenerator;
    
    @Inject
    @Named(value = "userSecurityService")
    protected UserSecurityService        securityService;

    @Inject
    @Named("authenticationTokenService")
    protected AuthenticationTokenService authenticationTokenService;
    
    @Inject
    @Named("userSessionManagerService")
    private UserSessionManagerService     userSessionManagerService;

    @Inject
    @Named(value = "userSecurityService")
    private UserSecurityService     userSecurityService;

    @Inject
    @Named("neutrinoExecutionContextHolder")
    private INeutrinoExecutionContextHolder neutrinoExecutionContextHolder;
    
    public static final String GENERIC_EXCEPTION_CODE="master.generic.exception.code";
    
    @Override
    @MonitoredWithSpring(name = "MCSI_MASTER_CHANGED_BY_USR")
    public BaseMasterEntity masterEntityChangedByUser(BaseMasterEntity changedEntity, User user) {

    	BaseMasterEntity returnEntity = changedEntity;
        UnapprovedEntityData unapprovedEntityData = new UnapprovedEntityData();
        unapprovedEntityData.setUserEntityId(user.getEntityId());
        if (changedEntity.getId() != null) {            // Indicates an existing entity
            validateOperationForUser(user.getEntityId(),changedEntity.getClass(), changedEntity.getId(),MasterApprovalFlowConstants.edit);

            BaseMasterEntity originalEntity = entityDao.find(changedEntity.getClass(), changedEntity.getId());

            if (originalEntity.getApprovalStatus() == APPROVED) { // original is approved , this request is to edit the
                                                                  // approved one.
                originalEntity.setApprovalStatus(APPROVED_MODIFIED);
            } else if (originalEntity.getApprovalStatus() == UNAPPROVED_MODIFIED) { // the previous entity was not
                                                                                    // unapproved, user is just trying to
                                                                                    // save again.
                if (user.getEntityId().getUri().equals(originalEntity.getEntityLifeCycleData().getCreatedByEntityId().getUri()) ||
                        User.class.isAssignableFrom(changedEntity.getClass())) { // exactly same
                                                                                // user who last
                                                                                // saved it.
                    EntityLifeCycleData entityLifeCycleData = new EntityLifeCycleDataBuilder().setCreatedByEntityId(
                            user.getEntityId()).getEntityLifeCycleData();
                    entityLifeCycleData.setUuid(originalEntity.getEntityLifeCycleData().getUuid());
                    entityLifeCycleData.setPersistenceStatus(PersistenceStatus.ACTIVE);
                    changedEntity.setEntityLifeCycleData(entityLifeCycleData);
                    changedEntity.setApprovalStatus(originalEntity.getApprovalStatus());
                    if (changedEntity instanceof MasterDynamicForm) {
                    	formService.saveOrUpdateDynamicFormInObject(((MasterDynamicForm) changedEntity).getUiMetaDataVo(), changedEntity);
                    }
                    entityDao.update(changedEntity);
                    MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_UPDATE_EVENT, true, user.getEntityId(),
                            changedEntity,makerCheckerHelper.getEntityDescription(changedEntity.getEntityDisplayName()));
                    event.addNonWatcherToNotify(user.getEntityId().getUri());
                    eventBus.fireEvent(event);
                    return changedEntity;
                } else {
                    originalEntity.setApprovalStatus(UNAPPROVED_HISTORY);
                }

            } else if (originalEntity.getApprovalStatus() == UNAPPROVED_ADDED
                    || originalEntity.getApprovalStatus() == ApprovalStatus.CLONED) {
                // commented so that any user can update the same new(UNAPPROVED_ADDED) record created by another user
                /*if (user.getEntityId().getUri()
                        .equals(originalEntity.getEntityLifeCycleData().getCreatedByEntityId().getUri())) {*/
                EntityLifeCycleData entityLifeCycleData = originalEntity.getEntityLifeCycleData();
                // Check added to prevent creation of empty record in case only child is persisted in parent child masters
            	//Check if existing PersistenceStatus is Temp(200) then on Saving or editing the master entity it should be changed to Active(0)
                if(originalEntity.getPersistenceStatus() != null 
                		&& originalEntity.getPersistenceStatus().equals(PersistenceStatus.TEMP))                {
            		entityLifeCycleData.setPersistenceStatus(PersistenceStatus.ACTIVE);
            	}else if  (changedEntity.getPersistenceStatus() != null){
            		entityLifeCycleData.setPersistenceStatus(changedEntity.getPersistenceStatus());
            	}
                changedEntity.setEntityLifeCycleData(entityLifeCycleData);
                changedEntity.setApprovalStatus(ApprovalStatus.UNAPPROVED_ADDED);
                if (changedEntity instanceof MasterDynamicForm) {
                	formService.saveOrUpdateDynamicFormInObject(((MasterDynamicForm) changedEntity).getUiMetaDataVo(), changedEntity);
                }
                entityDao.update(changedEntity);
                
                
                MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_UPDATE_EVENT, true, user.getEntityId(),
                        changedEntity,makerCheckerHelper.getEntityDescription(changedEntity.getEntityDisplayName()));
                event.addNonWatcherToNotify(user.getEntityId().getUri());
                eventBus.fireEvent(event);
                return changedEntity;
                /*}*/
            } else{
            	InvalidDataException invalidDataException =new InvalidDataException("Invalid entity state");
            	invalidDataException.setI18nCode(GENERIC_EXCEPTION_CODE);
            	throw invalidDataException;
            }

            unapprovedEntityData.setOriginalEntityId(changedEntity.getEntityId());
            BaseMasterEntity clonedEntity;
            if(changedEntity instanceof HistoryOptimizable)
            {
            	clonedEntity = (BaseMasterEntity) changedEntity.cloneYourself(CloneOptionConstants.CHILD_CLONING_OPTION);
            }
            else
            {
            	clonedEntity = (BaseMasterEntity) changedEntity.cloneYourself(MAKER_CHECKER_CLONING_OPTION);	
            }
            
            /*            MakerCheckerPreSnapshoptEvent makerCheckerPreSnapshoptEvent = new MakerCheckerPreSnapshoptEvent(clonedEntity);
                        eventBus.fireEvent(makerCheckerPreSnapshoptEvent);*/
            clonedEntity.setApprovalStatus(UNAPPROVED_MODIFIED);
            // UI is not sending the entities with populated entity lifecycle data.
            clonedEntity.getEntityLifeCycleData().setUuid(originalEntity.getEntityLifeCycleData().getUuid());
            clonedEntity.getEntityLifeCycleData().setCreatedByEntityId(user.getEntityId());
            setIpAddress(clonedEntity);
            entityDao.persist(clonedEntity);
            if (clonedEntity instanceof MasterDynamicForm) {
                formService.saveOrUpdateDynamicFormInObject(((MasterDynamicForm) clonedEntity).getUiMetaDataVo(), clonedEntity);
              }
            returnEntity = clonedEntity;
            unapprovedEntityData.setChangedEntityId(clonedEntity.getEntityId());
            unapprovedEntityData.setRefUUId(clonedEntity.getEntityLifeCycleData().getUuid());
            MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_UPDATE_EVENT, true, user.getEntityId(),
                    originalEntity,makerCheckerHelper.getEntityDescription(originalEntity.getEntityDisplayName()));
            event.addNonWatcherToNotify(user.getEntityId().getUri());
            eventBus.fireEvent(event);

        } else {

            if(!userIsAnAuthorizedMakerForEntity(changedEntity.getClass(),getUserInfoFromUserEntityId(user.getEntityId()))){
                throw new AccessDeniedException("User is not authorised to perform this operation");
            }

            if (changedEntity.getApprovalStatus() != ApprovalStatus.CLONED) {
                changedEntity.setApprovalStatus(UNAPPROVED_ADDED);
            }
            EntityLifeCycleData entityLifeCycleData = new EntityLifeCycleDataBuilder().setCreatedByEntityId(
                    user.getEntityId()).getEntityLifeCycleData();
            // Check added to prevent creation of empty record in case only child is persisted in parent child masters
            if (changedEntity.getPersistenceStatus() != null) {
                entityLifeCycleData.setPersistenceStatus(changedEntity.getPersistenceStatus());
            }
            changedEntity.setEntityLifeCycleData(entityLifeCycleData);
            setIpAddress(changedEntity);
            entityDao.persist(changedEntity);
            if (changedEntity instanceof MasterDynamicForm) {
                formService.saveOrUpdateDynamicFormInObject(((MasterDynamicForm) changedEntity).getUiMetaDataVo(), changedEntity);
              }
            unapprovedEntityData.setChangedEntityId(changedEntity.getEntityId());
            unapprovedEntityData.setRefUUId(changedEntity.getEntityLifeCycleData().getUuid());
            MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_CREATE_EVENT, true, user.getEntityId(),
                    changedEntity,makerCheckerHelper.getEntityDescription(changedEntity.getEntityDisplayName()));
            event.addNonWatcherToNotify(user.getEntityId().getUri());
            eventBus.fireEvent(event);
        }
        entityDao.persist(unapprovedEntityData);

        return returnEntity;
    }

    @Override
    @MonitoredWithSpring(name = "MCSI_CLONE_MASTER")
    public void createMasterEntityClone(BaseMasterEntity masterEntityToClone, EntityId userEntityId) {
        try {
            validateOperationForUser(userEntityId,masterEntityToClone.getClass(), masterEntityToClone.getId(),MasterApprovalFlowConstants.CLONE);
            BaseMasterEntity clone = (BaseMasterEntity) masterEntityToClone.cloneYourself(COPY_CLONING_OPTION);
            try {
                Field field = clone.getClass().getDeclaredField("reasonActInactMap");
                field.setAccessible(true);
                ReasonsActiveInactiveMapping value = (ReasonsActiveInactiveMapping) field.get(clone);
                field.set(clone,new ReasonsActiveInactiveMapping());
            }  catch (NoSuchFieldException e) {
            //e.printStackTrace();
			//DO NOTHING. IT IS TO AVOID EXCEPTION ONLY
           }


            clone.markTemp();
            clone.getEntityLifeCycleData().setCreatedByUri(userEntityId.getUri());
            clone.setApprovalStatus(ApprovalStatus.CLONED);
            masterEntityChangedByUser(clone, getCurrentUser().getUserReference());
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error while cloning " + masterEntityToClone.getClass() + " object.", e);
        }
    }

    @Override
    @MonitoredWithSpring(name = "MCSI_MARK_MASTER_FOR_DELETION")
    public void masterEntityMarkedForDeletion(BaseMasterEntity changedEntity, EntityId userEntityId) {
        UnapprovedEntityData unapprovedEntityData = new UnapprovedEntityData();
        unapprovedEntityData.setUserEntityId(userEntityId);
        if (changedEntity.getId() != null) {

            BaseMasterEntity originalEntity = entityDao.find(changedEntity.getClass(), changedEntity.getId());
            if (originalEntity == null) {
                throw new SystemException("No entity found with given class:" + changedEntity.getClass() + " given ID:"
                        + changedEntity.getId());
            }

            validateOperationForUser(userEntityId,changedEntity.getClass(), changedEntity.getId(),MasterApprovalFlowConstants.delete);

            setIpAddress(originalEntity);
            if (originalEntity.getApprovalStatus() == APPROVED) {
                if(originalEntity instanceof User){
                    throw new AccessDeniedException("Cannot delete approved users");
                }
                originalEntity.setApprovalStatus(APPROVED_DELETED);
                unapprovedEntityData.setOriginalEntityId(changedEntity.getEntityId());
                unapprovedEntityData.setRefUUId(originalEntity.getEntityLifeCycleData().getUuid());
                entityDao.persist(unapprovedEntityData);
                MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_MARKED_FOR_DELETION, true, userEntityId,
                        originalEntity,makerCheckerHelper.getEntityDescription(originalEntity.getEntityDisplayName()));
                eventBus.fireEvent(event);
            } else if (originalEntity.getApprovalStatus() == UNAPPROVED_MODIFIED) {
                originalEntity.setApprovalStatus(UNAPPROVED_HISTORY);
                BaseMasterEntity lastApprovedEntity = baseMasterDao.getLastApprovedEntityByUnapprovedEntityId(originalEntity
                        .getEntityId());
                lastApprovedEntity.setApprovalStatus(APPROVED);
                if(lastApprovedEntity.getClass().getName().equals("com.nucleus.core.workflowconfig.entity.WorkflowConfiguration")) {
                    try {
                        Class clazz = Class.forName("com.nucleus.core.workflowconfig.entity.WorkflowConfiguration");
                        Method method = clazz.getMethod("mergeWorkflowConfiguration", BaseMasterEntity.class);
                        method.invoke(lastApprovedEntity,originalEntity);
                    } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        BaseLoggers.flowLogger.error("Exception occurred in calling mergeWorkflowConfiguration {}", e);
                    }
                }
                if(lastApprovedEntity.getClass().getName().equals("com.nucleus.core.businesspartner.entity.AgencyMaster")) {
                    try {
                        Class className = Class.forName("com.nucleus.core.businesspartner.entity.AgencyMaster");
                        Method method = className.getMethod("updateAgencyMasterData", BaseMasterEntity.class);
                        method.invoke(lastApprovedEntity,originalEntity);
                    } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        BaseLoggers.flowLogger.error("Exception occurred in calling updateAgencyMasterData {}", e);
                    }
                }
                MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_DELETE, true, userEntityId, originalEntity,
                		makerCheckerHelper.getEntityDescription(originalEntity.getEntityDisplayName()));
                eventBus.fireEvent(event);

            } else {
            	if(originalEntity instanceof User){
            		((User) originalEntity).setUserStatus(UserStatus.STATUS_DELETED);
            	}
                originalEntity.setApprovalStatus(UNAPPROVED_HISTORY);
                MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_DELETE, true, userEntityId, originalEntity,
                		makerCheckerHelper.getEntityDescription(originalEntity.getEntityDisplayName()));
                eventBus.fireEvent(event);
            }
        }

    }

    // UI will send the ID of last unapproved entity.
    @Override
    @MonitoredWithSpring(name = "MCSI_START_MAKER_CHECKER_FLOW")
    public Long startMakerCheckerFlow(EntityId lastUnapprovedEntityID, EntityId userEntityId) {

        BaseLoggers.flowLogger.debug("Staring maker checker flow on EntityId :-->" + lastUnapprovedEntityID);

        if (masterConfigurationRegistry.getEntityAutoApprovalFlag(lastUnapprovedEntityID.getEntityClass())) {
            return startAutoApprovalFlow(lastUnapprovedEntityID, userEntityId);
        }

        if (userEntityId != null) {
            validateOperationForUser(userEntityId, lastUnapprovedEntityID.getEntityClass(), lastUnapprovedEntityID.getLocalId(),
                    MasterApprovalFlowConstants.sendForApproval,
                    MasterApprovalFlowConstants.AUTOAPPROVAL_WORKFLOW_DEFINITION_ID,
                    MasterApprovalFlowConstants.autoApproval,
                    MasterApprovalFlowConstants.WORKFLOW_DEFINITION_ID,
                    MasterApprovalFlowConstants.CHECKER_APPROVAL_TASK_WF_ID);
        }

        Long apporvalFlowId;
        BaseMasterEntity lastUnapprovedeEntity = (BaseMasterEntity) baseMasterDao.find(
                lastUnapprovedEntityID.getEntityClass(), lastUnapprovedEntityID.getLocalId());
        setIpAddress(lastUnapprovedeEntity);
        boolean hasExistingWorkFlow = false;
        MakerCheckerApprovalFlow approvalFlow = inProgressWorkFlow(lastUnapprovedEntityID);
        if (approvalFlow != null) {
            hasExistingWorkFlow = true;
        } else {
            approvalFlow = new MakerCheckerApprovalFlow();
        }
        approvalFlow.setCurrentState(IN_PROGRESS);
        if (lastUnapprovedeEntity.getApprovalStatus() == APPROVED_DELETED) {
            lastUnapprovedeEntity.setApprovalStatus(APPROVED_DELETED_IN_PROGRESS);
            approvalFlow.setChangedEntityUri(lastUnapprovedeEntity.getEntityId());
            List<UnapprovedEntityData> unapprovedChanges = baseMasterDao.getAllUnapprovedVersionsOfEntityByUUID(
                    lastUnapprovedeEntity.getEntityLifeCycleData().getUuid(), lastUnapprovedeEntity);
            approvalFlow.setChangeTrail(new LinkedHashSet<UnapprovedEntityData>(unapprovedChanges));
        } else {
            lastUnapprovedeEntity.setApprovalStatus(WORFLOW_IN_PROGRESS);
            BaseMasterEntity originalEntity = baseMasterDao
                    .getLastApprovedEntityByUnapprovedEntityId(lastUnapprovedEntityID);
            List<UnapprovedEntityData> unapprovedChanges = baseMasterDao.getAllUnapprovedVersionsOfEntityByUUID(
                    lastUnapprovedeEntity.getEntityLifeCycleData().getUuid(), originalEntity);
            if (originalEntity != null) {
                approvalFlow.setChangedEntityUri(originalEntity.getEntityId());
            }
            approvalFlow.setChangeTrail(new LinkedHashSet<UnapprovedEntityData>(unapprovedChanges));
        }

        if (userEntityId != null) {
            approvalFlow.setInitiator((User) entityDao.get(userEntityId));
            lastUnapprovedeEntity.getEntityLifeCycleData().setLastUpdatedByUri(userEntityId.getUri());
        }

        String assigneeAuthorityOrUserUri = null;
        String simpleClassName = getSimpleClassName(lastUnapprovedeEntity.getClass());

        assigneeAuthorityOrUserUri = AuthorityCodes.CHECKER + "_" + simpleClassName.toUpperCase();
        Authority baseAuthority = userService.getAuthorityByCode(assigneeAuthorityOrUserUri);

        if (baseAuthority != null) {
            assigneeAuthorityOrUserUri = baseAuthority.getUri();

        } else {
            assigneeAuthorityOrUserUri = AuthorityCodes.CHECKER + "_"
                    + lastUnapprovedeEntity.getClass().getSimpleName().toUpperCase();
            Authority subAuthority = userService.getAuthorityByCode(assigneeAuthorityOrUserUri);
            assigneeAuthorityOrUserUri = subAuthority.getUri();
        }

        BaseLoggers.flowLogger.debug("useDefaultStrategy :" + useDefaultStrategy);
        if (!useDefaultStrategy) {
            Map<String, Object> contextMap = new HashMap<String, Object>();
            contextMap.put("lastUnapprovedeEntity", lastUnapprovedeEntity);
            EventExecutionResult eventExecutionResult = eventExecutionService.fireEventExecution(
                    EventCode.MASTER_APPROVE_STRATEGY, contextMap, null);
            Map<Object, Object> resultMap = eventExecutionResult.getResultMap();
            if (resultMap != null && resultMap.size() > 0 && ((Map) resultMap.get(MASTER_APPROVE_TASK)) != null) {
                String userUri = String.valueOf(((Map) resultMap.get(MASTER_APPROVE_TASK)).get(USER_URI));
                if (StringUtils.isNotBlank(userUri)) {
                    assigneeAuthorityOrUserUri = userUri;
                }
            }
        }
        if (hasExistingWorkFlow) {
            ApprovalTask task = fetchPendingApprovalTask(approvalFlow);
            Map<String, Object> variables = WorkflowParameterMapCreator.createWorkflowMap(task.getActions(), mailService);
            variables.put("assigneeAuthorityOrUserUri", assigneeAuthorityOrUserUri);
            variables.put("masterEntityUri", lastUnapprovedEntityID.getUri());
            bpmnProcessService.completeUserTask(task.getWorkflowUserTaskId(), variables);
            taskService.completeApprovalTask(task, task.getActions());
            entityDao.update(approvalFlow);
            return approvalFlow.getId();
        }

        BaseLoggers.flowLogger.debug("assigneeAuthorityOrUserUri :" + assigneeAuthorityOrUserUri);
        entityDao.persist(approvalFlow);
        Map<String, Object> variables = WorkflowParameterMapCreator.createWorkflowMap(approvalFlow, null, mailService);
        variables.put("assigneeAuthorityOrUserUri", assigneeAuthorityOrUserUri);
        variables.put("masterEntityUri", lastUnapprovedEntityID.getUri());
        variables.put("masterEntityClass", lastUnapprovedEntityID.getUri().split(":")[0]);
        String workflowProcessId = bpmnProcessService.startProcess(WORKFLOW_DEFINITION_ID, variables);
        MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_SEND_FOR_APPROVAL, true, userEntityId,
                lastUnapprovedeEntity,makerCheckerHelper.getEntityDescription(lastUnapprovedeEntity.getEntityDisplayName()));
        eventBus.fireEvent(event);
        approvalFlow.setWorkflowId(workflowProcessId);
        apporvalFlowId = approvalFlow.getId();
        return apporvalFlowId;
    }

    /**
     * 
     * Method added to support for inheritance concept
     * @param clazz
     * @return
     */

    protected String getSimpleClassName(Class clazz) {
        Class superClass = clazz.getSuperclass();

        if (superClass.getSimpleName().equalsIgnoreCase("BaseMasterEntity")) {
            return clazz.getSimpleName();

        } else {
            return getSimpleClassName(superClass);
        }

    }

    @Override
    @MonitoredWithSpring(name = "MCSI_START_AUTO_APPROVAL_FLOW")
    public Long startAutoApprovalFlow(EntityId lastUnapprovedEntityID, EntityId userEntityId) {
        BaseMasterEntity lastUnapprovedEntity = (BaseMasterEntity) baseMasterDao.find(
                lastUnapprovedEntityID.getEntityClass(), lastUnapprovedEntityID.getLocalId());

        if (!masterConfigurationRegistry.getEntityAutoApprovalFlag(lastUnapprovedEntity.getClass())) {
            throw new SystemException("The entity : " + lastUnapprovedEntity.getClass().getSimpleName()
                    + " can not be auto approved");
        }

        if (userEntityId != null) {
            validateOperationForUser(userEntityId, lastUnapprovedEntityID.getEntityClass(), lastUnapprovedEntityID.getLocalId(),
                    MasterApprovalFlowConstants.sendForApproval,
                    MasterApprovalFlowConstants.AUTOAPPROVAL_WORKFLOW_DEFINITION_ID,
                    MasterApprovalFlowConstants.autoApproval,
                    MasterApprovalFlowConstants.WORKFLOW_DEFINITION_ID,
                    MasterApprovalFlowConstants.CHECKER_APPROVAL_TASK_WF_ID);
        }

        setIpAddress(lastUnapprovedEntity);
        MakerCheckerApprovalFlow approvalFlow = null;
        if (lastUnapprovedEntity.getApprovalStatus() == APPROVED_DELETED) {
            lastUnapprovedEntity.setApprovalStatus(APPROVED_DELETED_IN_PROGRESS);
            approvalFlow = new MakerCheckerApprovalFlow();
            approvalFlow.setChangedEntityUri(lastUnapprovedEntity.getEntityId());
            List<UnapprovedEntityData> unapprovedChanges = baseMasterDao.getAllUnapprovedVersionsOfEntityByUUID(
                    lastUnapprovedEntity.getEntityLifeCycleData().getUuid(), lastUnapprovedEntity);
            approvalFlow.setChangeTrail(new LinkedHashSet<UnapprovedEntityData>(unapprovedChanges));

        } else {
            lastUnapprovedEntity.setApprovalStatus(WORFLOW_IN_PROGRESS);
            BaseMasterEntity originalEntity = baseMasterDao
                    .getLastApprovedEntityByUnapprovedEntityId(lastUnapprovedEntityID);
            List<UnapprovedEntityData> unapprovedChanges = baseMasterDao.getAllUnapprovedVersionsOfEntityByUUID(
                    lastUnapprovedEntity.getEntityLifeCycleData().getUuid(), originalEntity);
            approvalFlow = new MakerCheckerApprovalFlow();
            if (originalEntity != null) {
                approvalFlow.setChangedEntityUri(originalEntity.getEntityId());
            }
            approvalFlow.setChangeTrail(new LinkedHashSet<UnapprovedEntityData>(unapprovedChanges));
        }

        if (userEntityId != null) {
            approvalFlow.setInitiator((User) entityDao.get(userEntityId));
            lastUnapprovedEntity.getEntityLifeCycleData().setLastUpdatedByUri(userEntityId.getUri());
        }
        approvalFlow.setCurrentState(IN_PROGRESS);
        entityDao.persist(approvalFlow);
        Map<String, Object> variables = WorkflowParameterMapCreator.createWorkflowMap(approvalFlow,
                userEntityId.getLocalId(), mailService);

        String workflowProcessId = bpmnProcessService.startProcess(AUTOAPPROVAL_WORKFLOW_DEFINITION_ID, variables);
        approvalFlow.setWorkflowId(workflowProcessId);
        return approvalFlow.getId();
    }

    @Override
    public Long createCheckerApprovalTask(ApprovalTask approvalTask, String workflowProcessInstanceId) {
        BaseLoggers.flowLogger.debug("Entering into ApprovalTaskForChecker -->");
        taskService.createTask(approvalTask);
        return approvalTask.getId();
    }

    @Override
    public Long createEntityModificationTaskForNewMaker(ApprovalTask approvalTask, String workflowProcessInstanceId) {
        BaseLoggers.flowLogger.debug("Entering into Send back case -->");
        updateEntityStateOnSendBack(approvalTask);
        taskService.createTask(approvalTask);
        return approvalTask.getId();
    }

    private ApprovalTask fetchApprovalTask(Long taskId) {
        Task fetchedTask = entityDao.find(Task.class, taskId);
        if (!(fetchedTask instanceof ApprovalTask)) {
            throw new InvalidDataException("The task with id " + taskId + " is not an approval task");
        }
        return (ApprovalTask) fetchedTask;
    }

    protected ApprovalTask fetchPendingApprovalTask(MakerCheckerApprovalFlow approvalFlow) {
        List<Task> pendingtasks = taskService.getPendingTasksForWorklow(approvalFlow);
        if (pendingtasks != null && pendingtasks.get(0) != null) {
            return (ApprovalTask) pendingtasks.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void terminateFlowByApproval(Long approvalFlowId, Long reviewerId) {
        EntityId userEntityId = new EntityId(User.class, reviewerId);
        MakerCheckerApprovalFlow makerCheckerApprovalFlow = entityDao.find(MakerCheckerApprovalFlow.class, approvalFlowId);

        BaseMasterEntity initialEntity = null;
        if (makerCheckerApprovalFlow.getChangedEntityUri() != null) {
            initialEntity = entityDao.get(EntityId.fromUri(makerCheckerApprovalFlow.getChangedEntityUri()));
        }

        UnapprovedEntityData lastUnApprovedEntityData = null;
        EntityId lastUnapprovedEntityId = null;
        Set<UnapprovedEntityData> unapprovedEntityDatas = makerCheckerApprovalFlow.getChangeTrail();
        if (unapprovedEntityDatas != null && !unapprovedEntityDatas.isEmpty()) {
            lastUnApprovedEntityData = Collections.max(unapprovedEntityDatas, CREATION_TIME_STAMP_COMPARATOR);
            lastUnapprovedEntityId = lastUnApprovedEntityData.getChangedEntityId();
        }

        if (lastUnapprovedEntityId == null && initialEntity != null) {// delete scenario
            validateOperationForUserForApproval(userEntityId,makerCheckerApprovalFlow.getEntityId().getUri(),initialEntity.getEntityId().getEntityClass(),initialEntity.getId(),MasterApprovalFlowConstants.APPROVED);
        }else{
            validateOperationForUserForApproval(userEntityId,makerCheckerApprovalFlow.getEntityId().getUri(),lastUnapprovedEntityId.getEntityClass(),lastUnapprovedEntityId.getLocalId(),MasterApprovalFlowConstants.APPROVED);
        }



        makerCheckerApprovalFlow.setCurrentState(COMPLETED);

        if (unapprovedEntityDatas != null && !unapprovedEntityDatas.isEmpty()) {

            if (lastUnapprovedEntityId == null) {
                // delete scenario
                initialEntity.setApprovalStatus(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
                initialEntity.getMasterLifeCycleData().setReviewedByEntityId(userEntityId);
                initialEntity.getMasterLifeCycleData().setReviewedTimeStamp(DateUtils.getCurrentUTCTime());
                updateChildMasterApprovalStatus(initialEntity);



                MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_DELETION_APPROVED, true, userEntityId,
                        initialEntity,makerCheckerHelper.getEntityDescription(initialEntity.getEntityDisplayName()));
                EntityApprovalPreProcessor entityApprovalPreProcessor = entityApprovalPreProcessorRegistry
                        .getEntityApprovalPreProcessor(initialEntity.getEntityId().getEntityClass());

                BaseMasterEntity lastUpdatedEntity = (BaseMasterEntity) entityDao.find(initialEntity.getEntityId().getEntityClass(),
                        initialEntity.getEntityId().getLocalId());

                // EntityApprovalPreProcessor entityApprovalPreProcessor = findRegisteredEntityApprovalPreProcessor(lastUnapprovedEntityId.getEntityId());
                callEntityApprovalPreProcessor(initialEntity, lastUpdatedEntity, null, entityApprovalPreProcessor, EventTypes.MAKER_CHECKER_DELETION_APPROVED, reviewerId);



                eventBus.fireEvent(event);
                return;
            }
            BaseMasterEntity lastUpdatedEntity = (BaseMasterEntity) entityDao.find(lastUnapprovedEntityId.getEntityClass(),
                    lastUnapprovedEntityId.getLocalId());



            EntityApprovalPreProcessor entityApprovalPreProcessor = findRegisteredEntityApprovalPreProcessor(lastUpdatedEntity.getEntityId());
            if(lastUpdatedEntity.getClass().getName().equals("com.nucleus.core.workflowconfig.entity.WorkflowConfiguration")) {
                try {
                    Class clazz = lastUpdatedEntity.getClass();
                    Method method = clazz.getMethod("clearDeletedFormData", BaseEntity.class);
                    method.invoke(lastUpdatedEntity,lastUpdatedEntity);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    BaseLoggers.flowLogger.error("Exception occurred in calling clearDeletedFormData {}", e);
                }
            }
            if (initialEntity != null) {
                lastUpdatedEntity.getMasterLifeCycleData().setReviewedByEntityId(userEntityId);
                lastUpdatedEntity.getMasterLifeCycleData().setReviewedTimeStamp(DateUtils.getCurrentUTCTime());

                // hereCode To Clone Modified Record For Opti
                BaseMasterEntity clonedSourceEntity;
                if(initialEntity instanceof HistoryOptimizable)
                {
                	clonedSourceEntity = (BaseMasterEntity) lastUpdatedEntity.cloneYourself(CloneOptionConstants.CHILD_CLONING_OPTION);
                	clonedSourceEntity.copyFrom(initialEntity, CloneOptionConstants.CHILD_CLONING_OPTION_WTH_ONLY_EXSTNG_CHLD);
                }
                else
                {
                	 clonedSourceEntity= (BaseMasterEntity) initialEntity.cloneYourself(CloneOptionConstants.MAKER_CHECKER_CLONING_OPTION);
                }
               

                MakerCheckerPreSnapshoptEvent makerCheckerPreSnapshoptEvent = new MakerCheckerPreSnapshoptEvent(
                        clonedSourceEntity);
                eventBus.fireEvent(makerCheckerPreSnapshoptEvent);

                clonedSourceEntity.setApprovalStatus(UNAPPROVED_HISTORY); 
                clonedSourceEntity.getEntityLifeCycleData().setCreatedByUri(
                		initialEntity.getEntityLifeCycleData().getCreatedByUri());
                clonedSourceEntity.getEntityLifeCycleData().setCreationTimeStamp(
                		initialEntity.getEntityLifeCycleData().getCreationTimeStamp());
                clonedSourceEntity.getEntityLifeCycleData().setLastUpdatedByUri(
                		initialEntity.getEntityLifeCycleData().getLastUpdatedByUri());
                clonedSourceEntity.getEntityLifeCycleData().setLastUpdatedTimeStamp(
                		initialEntity.getEntityLifeCycleData().getLastUpdatedTimeStamp());
                clonedSourceEntity.getMasterLifeCycleData().setReviewedByUri(
                		initialEntity.getMasterLifeCycleData().getReviewedByUri());
                clonedSourceEntity.getMasterLifeCycleData().setReviewedTimeStamp(
                		initialEntity.getMasterLifeCycleData().getReviewedTimeStamp());
                clonedSourceEntity.getMasterLifeCycleData().setIp4Address(lastUpdatedEntity.getMasterLifeCycleData().getIp4Address());
                setIpAddress(initialEntity);
                entityDao.persist(clonedSourceEntity);

                initialEntity.getMasterLifeCycleData().setReviewedByEntityId(userEntityId);
                initialEntity.getMasterLifeCycleData().setReviewedTimeStamp(DateUtils.getCurrentUTCTime());

                initialEntity.getEntityLifeCycleData().setLastUpdatedByUri(
                        lastUpdatedEntity.getEntityLifeCycleData().getLastUpdatedByUri());
                
                // initialEntity = entityDao.get(initialEntity.getEntityId());
                if(initialEntity instanceof HistoryOptimizable)
                {
                    initialEntity.copyFrom(lastUpdatedEntity, CloneOptionConstants.MAKER_CHECKER_COPY_OPTN_WTH_SOFT_CHLD_DELETE);                	
                }
                else
                {
                    initialEntity.copyFrom(lastUpdatedEntity, CloneOptionConstants.MAKER_CHECKER_COPY_OPTION);
                	
                }
                initialEntity.setApprovalStatus(APPROVED);
                
                // entityDao.saveOrUpdate(initialEntity);

                UnapprovedEntityData unApprovedEntityData = Collections.max(unapprovedEntityDatas, CREATION_TIME_STAMP_COMPARATOR);
                unApprovedEntityData.setChangedEntityId(initialEntity.getEntityId());
                unApprovedEntityData.setTimestamp(DateUtils.getCurrentUTCTime());
                unApprovedEntityData.setOriginalEntityId(clonedSourceEntity.getEntityId());
                entityDao.update(unApprovedEntityData);
                callEntityApprovalPreProcessor(initialEntity, lastUpdatedEntity, clonedSourceEntity, entityApprovalPreProcessor, EventTypes.MAKER_CHECKER_APPROVED, reviewerId);
                auditGenerator.generateChangeAndSave(clonedSourceEntity, initialEntity, initialEntity.getClass(),
                        userEntityId, initialEntity.getEntityLifeCycleData().getLastUpdatedByUri());
                adminActivityReportGenerator.generateAdminActivityReportAndSave(clonedSourceEntity,initialEntity, initialEntity.getClass(),initialEntity.getEntityLifeCycleData().getLastUpdatedByUri());
                
                //check for User Master data for triggering the event
                if(lastUpdatedEntity instanceof User) {
                	User user = (User)lastUpdatedEntity;
                	
                	if(user.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS && user.getUserStatus() == UserStatus.STATUS_INACTIVE){	
    					//Fire event USER_INACTIVATE_EVENT
                		UserEvent userEvent = new UserEvent(USER_INACTIVATED_EVENT, true, getCurrentUser().getUserEntityId(), ((User)initialEntity));
                        userEvent.setUserName(((User)initialEntity).getUsername());
                        userEvent.setAssociatedUser(getCurrentUser().getDisplayName());
                        eventBus.fireEvent(userEvent);
    				}
    				if(user.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS && user.getUserStatus() == UserStatus.STATUS_LOCKED){	
    			        
    			        //invalidate the user session
    			        userSessionManagerService.invalidateUserSessionInAllModules(initialEntity.getId(), reviewerId, getIpAddress(), NeutrinoSessionInformation.LOGOUT_TYPE_BY_ADMIN);
    					
    					//delete the reset password token from initial entity
    			        PasswordResetToken passwordResetToken = ((User)initialEntity).getPasswordResetToken();
    			        ((User)initialEntity).setPasswordResetToken(null);
    			        authenticationTokenService.deleteOldToken(initialEntity.getId(), passwordResetToken);
    			        
    			        //Fire event USER_BLOCK_EVENT
    			        UserEvent userEvent = new UserEvent(USER_BLOCKED_EVENT, true, getCurrentUser().getUserEntityId(), ((User)initialEntity));
    			        userEvent.setUserName(((User)initialEntity).getUsername());
    			        eventBus.fireEvent(userEvent);
    				}
    				if(user.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS && user.getUserStatus() == UserStatus.STATUS_ACTIVE){	
    					//Fire USER_ACTIVATE_EVENT
    			        UserInfo currentUser = getCurrentUser();
    			        if (currentUser == null){
    			            currentUser = securityService.getCompleteUserFromUsername(UserSecurityService.NEUTRINO_SYSTEM_USER);
    			        }
    			        UserEvent userEvent = new UserEvent(USER_ACTIVATED_EVENT, true, currentUser.getUserEntityId(), ((User)initialEntity));
    			        userEvent.setUserName(((User)initialEntity).getUsername());
    			        userEvent.setAssociatedUser(currentUser.getDisplayName());
    			        eventBus.fireEvent(userEvent);
    				}	
                }
                
                
                entityDao.delete(lastUpdatedEntity);
                MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_UPDATED_APPROVED, true, userEntityId,
                        initialEntity,makerCheckerHelper.getEntityDescription(initialEntity.getEntityDisplayName()));

                Field field = null;
                
                try {
                    field = initialEntity.getClass().getDeclaredField("reasonActInactMap");
                    field.setAccessible(true);
                    ReasonsActiveInactiveMapping value = null;
                    value = (ReasonsActiveInactiveMapping) field.get(initialEntity);
                    if(value != null) {
                    	List<String> reasonList = new ArrayList<>();
                    	String actInactFlag = null;
                    	if(CollectionUtils.isNotEmpty(value.getMasterActiveInactiveReasons())) {
	                        for (MasterActiveInactiveReasons mst : value.getMasterActiveInactiveReasons()) {
	                            if (mst.getReasonActive() != null) {
	                                Hibernate.initialize(mst.getReasonActive());
	                                reasonList.add(mst.getReasonActive().getDescription());
	                                actInactFlag = "Reason For Active";
	                            }
	                            if (mst.getReasonInactive() != null) {
	                                Hibernate.initialize(mst.getReasonInactive());
	                                reasonList.add(mst.getReasonInactive().getDescription());
	                                actInactFlag = "Reason For Inactive";
	                            }
	                        }
                    	}
                        String rString = String.join(",",reasonList);
                        if(rString != null && !rString.isEmpty()) {
                        	event.addPersistentProperty("ReasonForActiveInactve",actInactFlag +" : "+rString);	
                        }                        
                    }
                }catch (IllegalAccessException  | NoSuchFieldException e) {
                    BaseLoggers.flowLogger.debug("Exception in fetching reasonActInactMap / No such field found for the approval record");
                }
                
                eventBus.fireEvent(event);
                /*                entityUpdateService.copyAssociatedEntitiesFromSourceToTarget(clonedSourceEntity.getUri(),
                                        initialEntity.getUri());*/

            } else {
            	BaseMasterEntity clonedEntity;
            	if(lastUpdatedEntity instanceof HistoryOptimizable)
            	{
            		clonedEntity=(BaseMasterEntity)lastUpdatedEntity.cloneYourself(CloneOptionConstants.COPY_WITH_ID_AND_UUID_SET_STTS_APPRVD);
            		clonedEntity.getEntityLifeCycleData().setPersistenceStatus(PersistenceStatus.ACTIVE);
            		clonedEntity.getEntityLifeCycleData().setCreatedByUri(
            				lastUpdatedEntity.getEntityLifeCycleData().getCreatedByUri());
            		clonedEntity.getEntityLifeCycleData().setCreationTimeStamp(
            				lastUpdatedEntity.getEntityLifeCycleData().getCreationTimeStamp());
            		clonedEntity.getEntityLifeCycleData().setLastUpdatedByUri(
            				lastUpdatedEntity.getEntityLifeCycleData().getLastUpdatedByUri());
            		clonedEntity.getEntityLifeCycleData().setLastUpdatedTimeStamp(
            				lastUpdatedEntity.getEntityLifeCycleData().getLastUpdatedTimeStamp());
            		clonedEntity=entityDao.update(clonedEntity);
            	}
            	else
            	{
            		clonedEntity=lastUpdatedEntity;
            	}           	
            	clonedEntity.setApprovalStatus(ApprovalStatus.APPROVED);
            	clonedEntity.getMasterLifeCycleData().setReviewedByEntityId(userEntityId);
            	clonedEntity.getMasterLifeCycleData().setReviewedTimeStamp(DateUtils.getCurrentUTCTime());
                callEntityApprovalPreProcessor(initialEntity, clonedEntity, null, entityApprovalPreProcessor, EventTypes.MAKER_CHECKER_APPROVED, reviewerId);
                auditGenerator.generateChangeAndSave( null,clonedEntity, clonedEntity.getClass(),userEntityId,clonedEntity.getEntityLifeCycleData().getLastUpdatedByUri());
                adminActivityReportGenerator.generateAdminActivityReportAndSave(null,clonedEntity, clonedEntity.getClass(),clonedEntity.getEntityLifeCycleData().getLastUpdatedByUri());
                MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_APPROVED, true, userEntityId,
                		clonedEntity,makerCheckerHelper.getEntityDescription(clonedEntity.getEntityDisplayName()));
                eventBus.fireEvent(event);
            }
        }
    }

    @Override
    public void terminateFlowByDecline(Long approvalFlowId, Long reviewerId) {
        EntityId userEntityId = new EntityId(User.class, reviewerId);
        MakerCheckerApprovalFlow makerCheckerApprovalFlow = entityDao.find(MakerCheckerApprovalFlow.class, approvalFlowId);

        BaseMasterEntity initialEntity = null;
        if (makerCheckerApprovalFlow.getChangedEntityUri() != null) {
            initialEntity = entityDao.get(EntityId.fromUri(makerCheckerApprovalFlow.getChangedEntityUri()));
        }

        Set<UnapprovedEntityData> unapprovedEntityDatas = makerCheckerApprovalFlow.getChangeTrail();
        UnapprovedEntityData lastUnApprovedEntityData = Collections.max(unapprovedEntityDatas, CREATION_TIME_STAMP_COMPARATOR);
        EntityId lastUnapprovedEntityId = lastUnApprovedEntityData.getChangedEntityId();

        if (lastUnapprovedEntityId == null && initialEntity != null) {// delete rejection
            validateOperationForUser(userEntityId,initialEntity.getEntityId().getEntityClass(),initialEntity.getId(),MasterApprovalFlowConstants.REJECTED);
        }else{
            validateOperationForUser(userEntityId,lastUnapprovedEntityId.getEntityClass(),lastUnapprovedEntityId.getLocalId(),MasterApprovalFlowConstants.REJECTED);
        }

        makerCheckerApprovalFlow.setCurrentState(COMPLETED);

        if (lastUnapprovedEntityId != null) {
        	EntityApprovalPreProcessor entityApprovalPreProcessor = findRegisteredEntityApprovalPreProcessor(lastUnapprovedEntityId);
            BaseMasterEntity lastUpdatedEntity = (BaseMasterEntity) entityDao.find(lastUnapprovedEntityId.getEntityClass(),
                    lastUnapprovedEntityId.getLocalId());
            lastUpdatedEntity.getMasterLifeCycleData().setReviewedByEntityId(userEntityId);
            lastUpdatedEntity.getMasterLifeCycleData().setReviewedTimeStamp(DateUtils.getCurrentUTCTime());
            lastUpdatedEntity.setApprovalStatus(UNAPPROVED);
            if (initialEntity != null) {
               if(initialEntity.getClass().getName().equals("com.nucleus.core.workflowconfig.entity.WorkflowConfiguration")) {
                   try {
                       Class clazz = Class.forName("com.nucleus.core.workflowconfig.entity.WorkflowConfiguration");
                       Method method = clazz.getMethod("mergeWorkflowConfiguration", BaseMasterEntity.class);
                       method.invoke(initialEntity,lastUpdatedEntity);
                   } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                       BaseLoggers.flowLogger.error("Exception occurred in calling mergeWorkflowConfiguration {}", e);
                   }
               }
                if(initialEntity.getClass().getName().equals("com.nucleus.core.businesspartner.entity.AgencyMaster")) {
                   try {
                       Class className = Class.forName("com.nucleus.core.businesspartner.entity.AgencyMaster");
                       Method method = className.getMethod("updateAgencyMasterData", BaseMasterEntity.class);
                       method.invoke(initialEntity,lastUpdatedEntity);
                   } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                       BaseLoggers.flowLogger.error("Exception occurred in calling updateAgencyMasterData {}", e);
                   }
               }
                initialEntity.setApprovalStatus(APPROVED);               
                callEntityApprovalPreProcessor(initialEntity, lastUpdatedEntity, null, entityApprovalPreProcessor, EventTypes.MAKER_CHECKER_REJECTED, reviewerId);
                MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_UPDATED_REJECTED, true, userEntityId,
                        lastUpdatedEntity,makerCheckerHelper.getEntityDescription(lastUpdatedEntity.getEntityDisplayName()));
                eventBus.fireEvent(event);
            } else {     
               
                callEntityApprovalPreProcessor(initialEntity, lastUpdatedEntity, null, entityApprovalPreProcessor, EventTypes.MAKER_CHECKER_REJECTED, reviewerId);
                MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_REJECTED, true, userEntityId,
                        lastUpdatedEntity,makerCheckerHelper.getEntityDescription(lastUpdatedEntity.getEntityDisplayName()));
                eventBus.fireEvent(event);
            }
           
        } else {
            // delete rejection
            initialEntity.setApprovalStatus(APPROVED);
            MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_DELETION_REJECTED, true, userEntityId,
                    initialEntity,makerCheckerHelper.getEntityDescription(initialEntity.getEntityDisplayName()));
            eventBus.fireEvent(event);
        }

    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeTaskWithCheckerDecision(Long taskId, String actionTaken, EntityId userEntityId) {
    	completeTaskWithCheckerDecision(taskId, actionTaken, userEntityId,null);
    }

    // this method can be used for webservice maker checker mode
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeTaskWithCheckerDecision(Long taskId, String actionTaken, EntityId userEntityId,String checkerUserName) {

       String initiatorUserName = "";
       String loggedinUserName  = "";
       if(StringUtils.isNotEmpty(checkerUserName)){
    	   loggedinUserName = checkerUserName;
       }else{
    	   loggedinUserName = getCurrentUser().getUsername();
       }
       
        ApprovalTask task = fetchApprovalTask(taskId);

        if (task.getApprovalFlowReference() != null && task.getApprovalFlowReference().getInitiator() != null) {
              initiatorUserName = task.getApprovalFlowReference().getInitiator().getUsername();
        }
        
        if ( initiatorUserName!= null && loggedinUserName != null && initiatorUserName.equalsIgnoreCase(loggedinUserName) ) {
              throw new AccessDeniedException("Same user ("+loggedinUserName+") can not be both maker and author.");
        }
        
        Map<String, Object> variables = WorkflowParameterMapCreator.createWorkflowMap(actionTaken,
                userEntityId.getLocalId(), mailService);
        bpmnProcessService.completeUserTask(task.getWorkflowUserTaskId(), variables);
        taskService.completeApprovalTask(task, actionTaken);
        
        flushCurrentTransaction();
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void completeTaskWithCheckerDecisionInExistingTransaction(Long taskId, String actionTaken, EntityId userEntityId) {
    	 completeTaskWithCheckerDecision( taskId,  actionTaken,  userEntityId);
    }
    @Override
    public BaseMasterEntity saveAndSendForApproval(BaseMasterEntity changedEntity, User user) {
        BaseMasterEntity returnedEntity = masterEntityChangedByUser(changedEntity, user);
        startMakerCheckerFlow(returnedEntity.getEntityId(), user.getEntityId());
        return returnedEntity;

    }

    private void updateEntityStateOnSendBack(ApprovalTask approvalTask) {

        MakerCheckerApprovalFlow approvalFlow = (MakerCheckerApprovalFlow) entityDao.get(approvalTask
                .getApprovalFlowReference().getEntityId());
        Set<UnapprovedEntityData> unapprovedEntityDatas = approvalFlow.getChangeTrail();
        UnapprovedEntityData lastUnApprovedEntityData = Collections.max(unapprovedEntityDatas, CREATION_TIME_STAMP_COMPARATOR);

        if (approvalFlow.getChangedEntityUri() == null) { // new entity add case
            BaseMasterEntity lastUnApprovedEntity = entityDao.get(lastUnApprovedEntityData.getChangedEntityId());
            lastUnApprovedEntity.setApprovalStatus(UNAPPROVED_ADDED);
            lastUnApprovedEntity.getMasterLifeCycleData().setReviewedByEntityId(getCurrentUser().getUserEntityId());
            lastUnApprovedEntity.getMasterLifeCycleData().setReviewedTimeStamp(DateUtils.getCurrentUTCTime());
        } else {
            BaseMasterEntity initialEntity = entityDao.get(EntityId.fromUri(approvalFlow.getChangedEntityUri()));
            if (initialEntity != null) {
                if (lastUnApprovedEntityData.getChangedEntityId() != null) {
                    // edit scenario
                    initialEntity.setApprovalStatus(APPROVED_MODIFIED);
                    BaseMasterEntity lastUnApprovedEntity = entityDao.get(lastUnApprovedEntityData.getChangedEntityId());
                    lastUnApprovedEntity.setApprovalStatus(UNAPPROVED_MODIFIED);
                    lastUnApprovedEntity.getMasterLifeCycleData().setReviewedByEntityId(getCurrentUser().getUserEntityId());
                    lastUnApprovedEntity.getMasterLifeCycleData().setReviewedTimeStamp(DateUtils.getCurrentUTCTime());
                    MakerCheckerEvent event = new MakerCheckerEvent(MAKER_CHECKER_SEND_BACK, true, getCurrentUser()
                            .getUserEntityId(), lastUnApprovedEntity,makerCheckerHelper.getEntityDescription(lastUnApprovedEntity.getEntityDisplayName()));
                    eventBus.fireEvent(event);
                } else {
                    // delete scenario
                    initialEntity.setApprovalStatus(APPROVED_DELETED);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public MakerCheckerApprovalFlow inProgressWorkFlow(EntityId lastApprovedEntityId) {
        NamedQueryExecutor<MakerCheckerApprovalFlow> mcApprovalFlowExecutor = new NamedQueryExecutor<MakerCheckerApprovalFlow>(
                "MakerCheckerApprovalFlow.FindApprovalFlowByEntityUri").addParameter("entityUri",
                lastApprovedEntityId.getUri()).addParameter("currentState", IN_PROGRESS);
        List<MakerCheckerApprovalFlow> approvalFlows = entityDao.executeQuery(mcApprovalFlowExecutor);
        if (approvalFlows != null && approvalFlows.size() > 0) {
            return approvalFlows.get(0);
        } else {
            if (!lastApprovedEntityId.getEntityClass().equals(BaseEntity.class)) {
                return inProgressWorkFlow(new EntityId((Class<? extends Entity>) lastApprovedEntityId.getEntityClass()
                        .getSuperclass(), lastApprovedEntityId.getLocalId()));
            } else {
                return null;
            }

        }
    }
    
    protected EntityApprovalPreProcessor findRegisteredEntityApprovalPreProcessor(EntityId lastUnapprovedEntityId){

        EntityApprovalPreProcessor entityApprovalPreProcessor = entityApprovalPreProcessorRegistry
                .getEntityApprovalPreProcessor(lastUnapprovedEntityId.getEntityClass());
        return entityApprovalPreProcessor;
    }
    
    protected void callEntityApprovalPreProcessor(BaseMasterEntity originalRecord, 
    												BaseMasterEntity toBeDeletedRecord, 
    												BaseMasterEntity toBeHistoryRecord, 
    												EntityApprovalPreProcessor entityApprovalPreProcessor,  int eventType,
    												Long reviewrId){
         if(entityApprovalPreProcessor!=null){
            if(eventType==MAKER_CHECKER_DELETION_APPROVED && originalRecord!=null){
                entityApprovalPreProcessor.handleApprovalForDeletion(originalRecord,
                        toBeDeletedRecord,
                        toBeHistoryRecord,
                        reviewrId);
            }


    		if(eventType==MAKER_CHECKER_APPROVED && originalRecord!=null){
    			entityApprovalPreProcessor.handleApprovalForModification(originalRecord, 
    												 toBeDeletedRecord, 
    												 toBeHistoryRecord, 
    												 reviewrId);
    		}
    		if(eventType==MAKER_CHECKER_APPROVED && originalRecord==null){
    			entityApprovalPreProcessor.handleApprovalForNew(originalRecord, 
    												 toBeDeletedRecord, 
    												 toBeHistoryRecord, 
    												 reviewrId);
    		}
    		if(eventType==MAKER_CHECKER_REJECTED && originalRecord!=null){
    			entityApprovalPreProcessor.handleDeclineForModification(originalRecord, 
    												 toBeDeletedRecord, 
    												 reviewrId);
    		}
    		if(eventType==MAKER_CHECKER_REJECTED && originalRecord==null){
    			entityApprovalPreProcessor.handleDeclineForNew(originalRecord, 
    												 toBeDeletedRecord, 
    												 reviewrId);
    		}
    		if(eventType==MAKER_CHECKER_SEND_BACK && originalRecord!=null){
    			entityApprovalPreProcessor.handleSendBackForModification(originalRecord, 
    												 toBeDeletedRecord, 
    												 toBeHistoryRecord, 
    												 reviewrId);
    		}
    		if(eventType==MAKER_CHECKER_SEND_BACK && originalRecord==null){
    			entityApprovalPreProcessor.handleSendBackForNew(originalRecord, 
    												 toBeDeletedRecord, 
    												 toBeHistoryRecord, 
    												 reviewrId);
    		}
    	}
    }

	@Override
	public BaseEntity updateBaseEntityLifeCycleData(BaseEntity baseEntity, User user) {
		if (baseEntity.getId() != null) {
    		EntityLifeCycleData entityLifeCycleData = new EntityLifeCycleDataBuilder().setCreatedByEntityId(
                    user.getEntityId()).getEntityLifeCycleData();    		
    		entityLifeCycleData.setPersistenceStatus(baseEntity.getEntityLifeCycleData().getPersistenceStatus());
            entityLifeCycleData.setLastUpdatedByUri(user.getEntityId().getUri());
            baseEntity.setEntityLifeCycleData(entityLifeCycleData);
    	}else{        
            EntityLifeCycleData entityLifeCycleData = new EntityLifeCycleDataBuilder().setCreatedByEntityId(
                    user.getEntityId()).getEntityLifeCycleData();
            entityLifeCycleData.setCreatedByUri(user.getEntityId().getUri());
            entityLifeCycleData.setUuid(baseEntity.getUuid());
            baseEntity.setEntityLifeCycleData(entityLifeCycleData);
    	}
    		
    	return baseEntity;
	}

	public static void setIpAddress(BaseMasterEntity baseMasterEntity){
        if(baseMasterEntity.getMasterLifeCycleData()!=null){
            baseMasterEntity.getMasterLifeCycleData().setIp4Address(getIpAddress());
        }
    }

	public static String getIpAddress(){
        try{
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if(attributes!=null && !(attributes instanceof FacesRequestAttributes)){
                HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
                return request.getRemoteAddr();
            }
        }catch (ClassCastException | IllegalStateException e){
            BaseLoggers.flowLogger.info(e.getMessage());
        }
        return null;
    }
	
	private void updateChildMasterApprovalStatus(BaseMasterEntity entity) {
		Metamodel metaModel = entityDao.getEntityManager().getMetamodel();

		if (metaModel != null) {
			EntityType<? extends BaseMasterEntity> entityType = metaModel.entity(entity.getClass());
			Set<?> attributes = entityType.getDeclaredAttributes();
			if (!attributes.isEmpty()) {
				updateChildRecordsApprovalStatus(attributes, entity);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void updateChildRecordsApprovalStatus(Set<?> attributes, BaseMasterEntity entity) {
		try {
			for (Object attribute : attributes) {
				PersistentAttributeType persistentAttributeType = ((Attribute<?, ?>) attribute)
						.getPersistentAttributeType();

				if (PersistentAttributeType.ONE_TO_MANY.equals(persistentAttributeType)) {
					String persistentAttributeName = ((Attribute<?, ?>) attribute).getName();
					Object childRecords = PropertyUtils.getProperty(entity, persistentAttributeName);

					if (isBaseMasterRecord(childRecords)) {
						for (BaseMasterEntity childRecord : (Collection<BaseMasterEntity>) childRecords) {
							childRecord.setApprovalStatus(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
						}
					}

				} else if (PersistentAttributeType.ONE_TO_ONE.equals(persistentAttributeType)) {
					String persistentAttributeName = ((Attribute<?, ?>) attribute).getName();
					Object childRecord = PropertyUtils.getProperty(entity, persistentAttributeName);

					if (childRecord instanceof BaseMasterEntity) {
						BaseMasterEntity childEntity = (BaseMasterEntity) childRecord;
						childEntity.setApprovalStatus(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
					}
				}
			}
		} catch (Exception exception) {
			BaseLoggers.exceptionLogger
					.error("Error occurred while updating child records approval status of master entity "
							+ entity.getUri() + " : " + exception);
		}
	}

	@SuppressWarnings("rawtypes")
	private boolean isBaseMasterRecord(Object childRecords) {
	
	 	return !((Collection)childRecords).isEmpty() && ((Collection)childRecords).iterator().next() instanceof BaseMasterEntity;
	
	}

    @Override
    public <T extends BaseMasterEntity> T getMasterEntityWithActionsById(Class<T> entityClass, Long id, String userUri) {
        return getMasterEntityWithActionsByIdForUserInfo(entityClass,id,getUserInfoFromUserEntityId(EntityId.fromUri(userUri)));
    }

    private UserInfo getUserInfoFromUserEntityId(EntityId userEntityId){
        //Cache this data in a thread local so as to save time?
        User user = (User) entityDao.find(userEntityId.getEntityClass(),userEntityId.getLocalId());
        return userSecurityService.getCompleteUserFromUsername(user.getUsername());
    }

    private <T extends BaseMasterEntity> T getMasterEntityWithActionsByIdForUserInfo(Class<T> entityClass, Long id, UserInfo userDetails) {

        BaseLoggers.flowLogger.debug("Getting actions for master entity : -->" + entityClass);
        EntityId userEntityId = userDetails.getUserEntityId();
        BaseMasterEntity bma = baseMasterDao.find(entityClass, id);

        List<Integer> statusList = new ArrayList<Integer>();

        Boolean isAuthorizedMakerForEntity = userIsAnAuthorizedMakerForEntity(entityClass,userDetails);

        if (isAuthorizedMakerForEntity) {
            statusList.add(ApprovalStatus.CLONED);
        }

        statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        statusList.add(ApprovalStatus.APPROVED);

        if (bma == null || !statusList.contains(bma.getApprovalStatus())) {
            throw new InvalidDataException("Url is not correct");
        }

        Boolean isAuthorizedCheckerForEntity = userIsAnAuthorizedCheckerForEntity(entityClass,userDetails);

        if (bma.getEntityLifeCycleData().getSystemModifiableOnly() == null
                || !(bma.getEntityLifeCycleData().getSystemModifiableOnly())) {
            setNonWorkflowEntityAction(entityClass.getSimpleName(), bma, isAuthorizedMakerForEntity);
        }

        List<T> baseMasterEntities = new ArrayList<T>();
        baseMasterEntities.add((T) bma);
        Map<Long, BaseMasterEntity> _idToEntities = new HashMap<Long, BaseMasterEntity>();
        _idToEntities.put(bma.getId(), bma);
        baseMasterEntities = setApplicableWorkFlowActions(isAuthorizedMakerForEntity, isAuthorizedCheckerForEntity,
                entityClass, userDetails.getUserEntityId().getUri(), baseMasterEntities, _idToEntities);
        return baseMasterEntities.get(0);

    }

	@Override
    public void setNonWorkflowEntityAction(String entityName, BaseMasterEntity bma,
                                           Boolean isAuthorozedMakerForEntity) {
        if (bma.getEntityLifeCycleData().getSystemModifiableOnly() == null
                || !(bma.getEntityLifeCycleData().getSystemModifiableOnly())) {
            List<String> actionsList = new ArrayList<String>();

            List<ActionConfiguration> actionConfigurationList = masterConfigurationRegistry.getActionConfigurationList(entityName);
            boolean isApplicableForAutoApproval = masterConfigurationRegistry.getEntityAutoApprovalFlag(entityName);

            if(bma.getApprovalStatus() == ApprovalStatus.APPROVED && isAuthorozedMakerForEntity) {

                if(CollectionUtils.isNotEmpty(actionConfigurationList)){

                    for(ActionConfiguration actionConfiguration : actionConfigurationList){
                        if((MasterApprovalFlowConstants.edit.equals(actionConfiguration.getAction())
                                || MasterApprovalFlowConstants.delete.equals(actionConfiguration.getAction())
                                || MasterApprovalFlowConstants.CLONE.equals(actionConfiguration.getAction()))
                                &&actionConfiguration.getApplicableForApprovedRecord()){
                            actionsList.add(actionConfiguration.getAction());
                        }
                    }
                }else{

                    actionsList.add(MasterApprovalFlowConstants.edit);
                    actionsList.add(MasterApprovalFlowConstants.delete);
                    actionsList.add(MasterApprovalFlowConstants.CLONE);
                }

                if(entityName.equalsIgnoreCase(DYNAMIC_WORKFLOW_CONFIGURATION)) {

                    Class clazz;
                    Boolean isSubProcess;
                    try {
                        clazz = Class.forName("com.nucleus.core.workflowconfig.entity.WorkflowConfiguration");
                        isSubProcess = (Boolean) clazz.getMethod("getIsSubProcess", new Class[] {}).invoke(bma);
                    } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
                        throw new SystemException("Error in checking sub process flag in workflowConfiguration. "+ e);
                    }
                    if(isSubProcess!= null && !isSubProcess) {
                        for (ActionConfiguration actionConfiguration : actionConfigurationList) {
                            if (actionConfiguration.getAction().equalsIgnoreCase("Start")) {
                                actionsList.add(actionConfiguration.getAction());
                            }
                        }
                    }
                }

            }else if ((bma.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED || bma.getApprovalStatus() == ApprovalStatus.UNAPPROVED_ADDED)
                    && isAuthorozedMakerForEntity) {
                if(CollectionUtils.isNotEmpty(actionConfigurationList)){
                    for(ActionConfiguration actionConfiguration : actionConfigurationList){
                        if(MasterApprovalFlowConstants.edit.equals(actionConfiguration.getAction())
                                || MasterApprovalFlowConstants.delete.equals(actionConfiguration.getAction())
                                || MasterApprovalFlowConstants.CLONE.equals(actionConfiguration.getAction())){
                            actionsList.add(actionConfiguration.getAction());
                        }
                    }
                }else{
                    actionsList.add(MasterApprovalFlowConstants.delete);
                    actionsList.add(MasterApprovalFlowConstants.edit);
                    actionsList.add(MasterApprovalFlowConstants.CLONE);

                }

                if (isApplicableForAutoApproval) {
                    actionsList.add(MasterApprovalFlowConstants.autoApproval);
                }else{
                    actionsList.add(MasterApprovalFlowConstants.sendForApproval);
                }
            }else if (bma.getApprovalStatus() == ApprovalStatus.APPROVED_DELETED && isAuthorozedMakerForEntity) {
                if(CollectionUtils.isNotEmpty(actionConfigurationList) ){
                    for(ActionConfiguration action : actionConfigurationList){
                        if(MasterApprovalFlowConstants.sendForApproval.equals(action.getAction())){
                            actionsList.add(MasterApprovalFlowConstants.sendForApproval);
                        }
                    }

                }else{
                    actionsList.add(MasterApprovalFlowConstants.sendForApproval);
                }
            }else if (bma.getApprovalStatus() == ApprovalStatus.CLONED && isAuthorozedMakerForEntity) {
                if(CollectionUtils.isNotEmpty(actionConfigurationList)){
                    for(ActionConfiguration actionConfiguration : actionConfigurationList){
                        if((MasterApprovalFlowConstants.edit.equals(actionConfiguration.getAction())
                                || MasterApprovalFlowConstants.delete.equals(actionConfiguration.getAction())
                                || MasterApprovalFlowConstants.CLONE.equals(actionConfiguration.getAction()))){
                            actionsList.add(actionConfiguration.getAction());
                        }
                    }

                }else{
                    actionsList.add(MasterApprovalFlowConstants.delete);
                    actionsList.add(MasterApprovalFlowConstants.edit);
                    actionsList.add(MasterApprovalFlowConstants.CLONE);
                }
            }
            bma.addProperty("actions", actionsList);
        }
    }

    protected <T extends BaseMasterEntity> boolean loggedInUserisAnAuthorizedMakerForEntity(Class<T> entityClass) {
        return userIsAnAuthorizedMakerForEntity(entityClass,getCurrentUser());
    }

    protected <T extends BaseMasterEntity> boolean loggedInUserisAnAuthorizedCheckerForEntity(Class<T> entityClass) {
        return userIsAnAuthorizedCheckerForEntity(entityClass,getCurrentUser());
    }

    protected <T extends BaseMasterEntity> boolean userIsAnAuthorizedMakerForEntity(Class<T> entityClass, UserInfo userInfo) {

        String authorityCode = AuthorityCodes.MAKER;

        return userIsAuthorizedMakerOrCheckerForEntity(authorityCode,entityClass,userInfo);
    }

    protected <T extends BaseMasterEntity> boolean userIsAnAuthorizedCheckerForEntity(Class<T> entityClass, UserInfo userInfo) {

        String authorityCode = AuthorityCodes.CHECKER;

        return userIsAuthorizedMakerOrCheckerForEntity(authorityCode,entityClass,userInfo);
    }


    protected <T extends BaseMasterEntity> boolean loggedInUserIsAuthorizedMakerOrCheckerForEntity(String authorityCode, Class entityClass) {
        return userIsAuthorizedMakerOrCheckerForEntity(authorityCode,entityClass,getCurrentUser());
    }

    protected <T extends BaseMasterEntity> boolean userIsAuthorizedMakerOrCheckerForEntity(String authorityCode, Class entityClass, UserInfo userInfo) {
        Set<Authority> userAuthorities = userInfo.getUserAuthorities();
        Set<String> masterAuthoritiesForAction = getAuthoritiesForMasterClassForAction(entityClass,authorityCode);

        Boolean isAuthorizedForEntity = false;
        for (Authority authority : userAuthorities) {
            if (masterAuthoritiesForAction.contains(authority.getAuthCode().toUpperCase())) {
                isAuthorizedForEntity = true;
                break;
            }
        }
        return isAuthorizedForEntity;
    }

    private static Map<String,Set<String>> getAuthoritiesForMasterClass(Class masterClass){
        Map<String,Set<String>> actionAuthorityMap = new HashMap<>();

        actionAuthorityMap.put(AuthorityCodes.MAKER,getAuthoritiesForMasterClassForAction(masterClass,AuthorityCodes.MAKER));
        actionAuthorityMap.put(AuthorityCodes.CHECKER,getAuthoritiesForMasterClassForAction(masterClass,AuthorityCodes.CHECKER));

        return actionAuthorityMap;
    }

    private static Set<String> getAuthoritiesForMasterClassForAction(Class masterClass, String action){
        Set<String> authoritiesForAction = new HashSet<>();

        if(masterClass != null && !BaseMasterEntity.class.equals(masterClass) && !BaseEntity.class.equals(masterClass)
                && !AbstractBaseEntity.class.equals(masterClass) &&  !Object.class.equals(masterClass)){

            authoritiesForAction.add(action.toUpperCase() + "_" + masterClass.getSimpleName().toUpperCase());
            authoritiesForAction.addAll(getAuthoritiesForMasterClassForAction(masterClass.getSuperclass(),action));
        }

        return authoritiesForAction;
    }

    @Override
    public <T extends BaseMasterEntity> List<T> setApplicableWorkFlowActions(Boolean isAuthorizedMakerForEntity,
                                                                             Boolean isAuthorizedCheckerForEntity, Class<T> entityClass, String userUri, List<T> baseMasterEntities,
                                                                             Map<Long, BaseMasterEntity> _idToEntities) {
        BaseLoggers.flowLogger.debug("Setting  Applicable WorkFlow Actions -->");
        List<Integer> wipStatusList = new ArrayList<>();
        wipStatusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
        wipStatusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);

        List<String> actionRelatedapplicableAuths = getApplicableAssigneeListUri(isAuthorizedMakerForEntity,
                isAuthorizedCheckerForEntity, entityClass, userUri);

        int start = 0;
        int end = 0;
        int totalSize = baseMasterEntities.size();
        while (totalSize - end > 0) {
            start = start * ORACLE_LIMIT_FOR_IN_CLAUSE_ELEMENTS;
            end = start + ORACLE_LIMIT_FOR_IN_CLAUSE_ELEMENTS;
            end = end < totalSize ?  end : totalSize;
            List<String> uuids = getUUIDs(start, end, baseMasterEntities);
            if (!actionRelatedapplicableAuths.isEmpty()) {
                List<Object[]> obs = baseMasterDao.getEntitiesWithActionsByStatus(entityClass, wipStatusList,
                        actionRelatedapplicableAuths, uuids);
                addWorkFlowRelatedActions(obs, baseMasterEntities, _idToEntities, userUri);
            }
            start++;
        }
        return baseMasterEntities;

    }

    private <T extends BaseMasterEntity> void addWorkFlowRelatedActions(List<Object[]> obs,
                                                                        List<T> baseMasterEntities, Map<Long, BaseMasterEntity> _idToEntities,
                                                                        String userUri) {
        for (Object[] baseEntitiesArr : obs) {
            BaseMasterEntity bma = (BaseMasterEntity) baseEntitiesArr[0];
            MakerCheckerApprovalFlow approvalFlow = inProgressWorkFlow(bma.getEntityId());
            if (approvalFlow != null && approvalFlow.getInitiator().getUri().equals(userUri)) {
                continue;
            }
            ApprovalTask approvalTask = (ApprovalTask) baseEntitiesArr[1];
            List<String> actionsList = new ArrayList<>();
            if (bma.getEntityLifeCycleData().getSystemModifiableOnly() != null
                    && bma.getEntityLifeCycleData().getSystemModifiableOnly()) {
                bma.addProperty("actions", new ArrayList<String>());
            } else {
                StringTokenizer possibleActionsTokenizer = new StringTokenizer(approvalTask.getActions(), ",");
                while (possibleActionsTokenizer.hasMoreTokens()) {
                    actionsList.add(possibleActionsTokenizer.nextToken());
                }
                bma.addProperty("actions", actionsList);
                if (_idToEntities.containsKey(bma.getId())) {
                    int index = baseMasterEntities.indexOf(_idToEntities.get(bma.getId()));
                    baseMasterEntities.set(index, (T) bma);
                }
            }
            bma.addProperty("taskId", approvalTask.getId());
        }
    }

    private <T extends BaseMasterEntity> List<String> getApplicableAssigneeListUri(boolean isAuthorizedMakerForEntity,
                                                                                   boolean isAuthorizedCheckerForEntity, Class entityClass, String userUri) {
        List<String> authList = new ArrayList<String>();

        Map<String,Set<String>> actionAuthorityMap = getAuthoritiesForMasterClass(entityClass);

        if (isAuthorizedMakerForEntity) {

            Set<String> makerAuthoritySet = actionAuthorityMap.get(AuthorityCodes.MAKER);

            for(String makerAuthority : makerAuthoritySet){
                Authority authority = userService.getAuthorityByCode(makerAuthority);
                if(authority != null){
                    authList.add(authority.getUri());
                }
            }
        }
        if (isAuthorizedCheckerForEntity) {
            Set<String> checkerAuthoritySet = actionAuthorityMap.get(AuthorityCodes.CHECKER);

            for(String checkerAuthority : checkerAuthoritySet){
                Authority authority = userService.getAuthorityByCode(checkerAuthority);
                if(authority != null){
                    authList.add(authority.getUri());
                }
            }
        }
        if (userUri != null) {
            authList.add(userUri);
        }
        return authList;
    }

    private <T extends BaseMasterEntity> List<String> getUUIDs(int start, int end, List<T> baseMasterEntities) {
        List<String> uuids = new ArrayList<>();
        for (T baseMasterEntity : baseMasterEntities.subList(start, end)) {
            uuids.add(baseMasterEntity.getEntityLifeCycleData().getUuid());
        }
        return uuids;
    }

    private void validateOperationForLoggedInUser(Class entityClass,Long id,String ... operations ) {
        BaseMasterEntity persistedEntity = (BaseMasterEntity)getMasterEntityWithActionsById(entityClass,
                id, getCurrentUser().getUserEntityId().getUri());
        if (persistedEntity.getViewProperties() == null
                || !canPerformOperation(persistedEntity.getViewProperties(),
                operations)) {
            throw new AccessDeniedException("User is not authorised to perform this operation");
        }
    }

    private void validateOperationForUser(EntityId userEntityId,Class entityClass,Long id,String ... operations ) {

        UserInfo userDetails = getUserInfoFromUserEntityId(userEntityId);

        BaseMasterEntity persistedEntity = (BaseMasterEntity)getMasterEntityWithActionsByIdForUserInfo(entityClass,
                id, userDetails);
        if (persistedEntity.getViewProperties() == null
                || !canPerformOperation(persistedEntity.getViewProperties(),
                operations)) {
            throw new AccessDeniedException("User is not authorised to perform this operation");
        }
    }

    private void validateOperationForUserForApproval(EntityId userEntityId,String approvalFlowUri, Class entityClass,Long id,String ... operations ) {

        UserInfo userDetails = getUserInfoFromUserEntityId(userEntityId);

        BaseMasterEntity persistedEntity = (BaseMasterEntity)getMasterEntityWithActionsByIdForUserInfo(entityClass,
                id, userDetails);
        if (persistedEntity.getViewProperties() == null
                || !canPerformOperation(persistedEntity.getViewProperties(),
                operations)) {
            if(!checkExecutionContextForActions(approvalFlowUri,operations)){
                throw new AccessDeniedException("User is not authorised to perform this operation");
            }

        }
    }

    private Boolean checkExecutionContextForActions(String approvalFlowUri,String ... operations){
        Map<String,String> approvalFlowActionsMap = (Map<String,String>)neutrinoExecutionContextHolder.getFromGlobalContext(WORKFLOW_ACTION_MAP);
        if(approvalFlowActionsMap!=null){
            if(approvalFlowActionsMap.containsKey(approvalFlowUri)){
                String actionList = approvalFlowActionsMap.get(approvalFlowUri).toString();
                for(String operation: operations){
                    if( actionList.contains(operation)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean canPerformOperation(HashMap<String, Object> viewProperties,String ... operations) {
        List<String> actions=(List<String>) viewProperties.get("actions");
        if(actions==null){
            return false;
        }
        boolean canPerformOperation=false;
        for(String operation: operations){
            if( actions.contains(operation)){
                canPerformOperation=true;
                break;
            }
        }
        return canPerformOperation;
    }
    
}