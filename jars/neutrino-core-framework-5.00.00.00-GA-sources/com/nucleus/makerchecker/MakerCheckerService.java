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

import com.nucleus.approval.ApprovalTask;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.service.BaseService;
import com.nucleus.user.User;

import java.util.List;
import java.util.Map;

/**
 * This interface provides methods for Application controller and workflow engine to 
 * 1. persist/update/delete a Master Entity's state.   
 * 2. Initiate workflow process (under workflow engine) for a given entity.
 * 3. Update user Task ( under workflow engine and application schema) status when a user action is taken on an entity. 
 * 4. Update associated entities for a given entity when workflow process (under workflow engine)  is completed. 
 */
public interface MakerCheckerService extends BaseService {

    /**
     * This method is to be called by Controller when a new entity is added or an existing entity is modified by the maker.
     * @param changedEntity. 
     * @param user
     * @return new created entity.
     */
    public BaseMasterEntity masterEntityChangedByUser(BaseMasterEntity changedEntity, User user);

    /**
     * This method is to be called called by Controller when a new entity is marked for deletion by by Maker.
     * @param changedEntity
     * @param userEntityId
     */
    void masterEntityMarkedForDeletion(BaseMasterEntity changedEntity, EntityId userEntityId);

    /**
     * This method is to be called called by Controller when a clone is requested for a master entity.
     * @param masterEntityToClone
     * @param userEntityId
     */
    public void createMasterEntityClone(BaseMasterEntity masterEntityToClone, EntityId userEntityId);

    /**
     * Saves the existing version and initiates the workflow.
     * @param changedEntity
     * @param user
     */
    public BaseMasterEntity saveAndSendForApproval(BaseMasterEntity changedEntity, User user);

    /**
     * Initiates the work flow for a given Entity.
     * @param changedEntityId
     * @param userEntityId
     * @return
     */
    public Long startMakerCheckerFlow(EntityId changedEntityId, EntityId userEntityId);

    /**
     * Initiates the 'Auto Approval' work flow for a given Entity.
     * @param changedEntityId
     * @param userEntityId
     * @return
     */
    public Long startAutoApprovalFlow(EntityId changedEntityId, EntityId userEntityId);

    /**
     * Creates a new task for {@link MakerCheckerApprovalFlow}. 
     * @param approvalTask The approval task to be created in system.
     * @param workflowProcessInstanceId The instance id of workflow which corresponds to {@link MakerCheckerApprovalFlow} for
     * which the task is to be created.  
     * @return id of created {@link ApprovalTask}
     */
    public Long createCheckerApprovalTask(ApprovalTask approvalTask, String workflowProcessInstanceId);

    /**
     * Creates a new modification task for maker for currently running {@link MakerCheckerApprovalFlow}.
     * This method should be called by workflow listener when Checker sends back the Maker suggested changes 
     * and a new maker is to be allocated to the task. 
     * @param approvalTask The approval task to be created.
     * @param workflowProcessInstanceId The instance id of workflow which corresponds to {@link MakerCheckerApprovalFlow} for
     * which the task is to be created.  
     * @return id of created {@link ApprovalTask}
     */
    public Long createEntityModificationTaskForNewMaker(ApprovalTask approvalTask, String workflowProcessInstanceId);

    /**
     * This method is called by workflow engine when the flow is completed by Approvers approval. 
     * @param approvalFlowId
     * @param reviewerId
     */
    public void terminateFlowByApproval(Long approvalFlowId, Long reviewerId);

    /**
     * This method is called by workflow engine when the flow is terminated by Approver's rejecetion. 
     * @param approvalFlowId
     * @param reviewerId
     */
    public void terminateFlowByDecline(Long approvalFlowId, Long reviewerId);

    /**
     * This method is called by Controller when an Approver(checker) takes a workflow action on an entity.  
     * @param taskId
     * @param actionTaken
     * @param userEntityId
     */

    public void completeTaskWithCheckerDecision(Long taskId, String actionTaken, EntityId userEntityId);
    
    public MakerCheckerApprovalFlow inProgressWorkFlow(EntityId lastApprovedEntityId);
    
    public BaseEntity updateBaseEntityLifeCycleData(BaseEntity baseEntity, User user);
    void completeTaskWithCheckerDecisionInExistingTransaction(Long taskId, String actionTaken, EntityId userEntityId);

    <T extends BaseMasterEntity> T getMasterEntityWithActionsById(Class<T> entityClass, Long id, String userUri);

    void setNonWorkflowEntityAction(String entityName, BaseMasterEntity bma,
                                    Boolean isAuthorozedMakerForEntity);

    <T extends BaseMasterEntity> List<T> setApplicableWorkFlowActions(Boolean isAuthorizedMakerForEntity,
                                                                      Boolean isAuthorizedCheckerForEntity, Class<T> entityClass, String userUri, List<T> baseMasterEntities,
                                                                      Map<Long, BaseMasterEntity> _idToEntities);
}