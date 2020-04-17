/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.master;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.cas.parentChildDeletionHandling.BaseMasterDependency;
import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.makerchecker.MasterApprovalFlowConstants;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.master.BaseMasterUtils;
import com.nucleus.master.marker.HistoryOptimizable;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;

import net.bull.javamelody.MonitoredWithSpring;

/**
 * The Class MasterController.
 *
 * @author Nucleus Software Exports Limited
 */
@Transactional
@Controller
@RequestMapping(value = "master/{xmlFilePath}")
public class MasterActionController extends BaseController {

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService         makerCheckerService;

    @Inject
    @Named("masterConfigurationRegistry")
    private MasterConfigurationRegistry masterConfigurationRegistry;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService           baseMasterService;

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    private static ConcurrentHashMap<String, String>   threadSafeMap = new ConcurrentHashMap<String, String>();

    @Inject
    @Named("masterXMLDocumentBuilder")
    private CommonFileIOMasterGridLoad  commonFileIOMasterGridLoad;

    private static final String FAIL_MESSAGE="Parent records found for: ";
    private static final String NOTIF_MAP_SUCCESS_KEY="success";
    private static final String NOTIF_MAP_FAILURE_KEY="failure";



    /**
     * Post task action.
     *
     * @param action string for e.g : Approve, Reject
     * @param taskid the taskid
     * @param request the request
     * @return String
     * @description Controller method to complete the task
     */

    @PreAuthorize(" hasAuthority('CHECKER_'+#xmlFilePath.toUpperCase())")
    @RequestMapping(value = "/completeTask", method = RequestMethod.POST)
    @MonitoredWithSpring(name = "MAC_POST_ACTION_TAKEN")
    public @ResponseBody
    Map<String, Object> postTaskAction(@RequestParam(value="action") String action, @RequestParam(value="taskIds[]") Long[] taskid, @PathVariable("xmlFilePath") String xmlFilePath,
                                       HttpServletRequest request) {
        String msg = "";
        String successMsg = "";
        Map<String, Object> responseMap = new HashMap<>();
        /*
         * Iterate over the action taken array and pass task id and action to
         * completeTaskWithCheckerDecision.
         */
        try {
            successMsg = commonFileIOMasterGridLoad.getResourceBundleFileReader(request, "msg.taskCompletion");
        } catch (Exception e) {
            throw new SystemException(e);
        }
        try {
            for (int i = 0 ; i < taskid.length ; i++) {

                makerCheckerService.completeTaskWithCheckerDecision(taskid[i], action, getUserDetails().getUserReference()
                        .getEntityId());
            }
            msg = successMsg + " " + action;
            responseMap.put("validationResposne", "valid");
            responseMap.put("message", msg);
        } catch (Exception e) {
            int index = ExceptionUtils.indexOfThrowable(e, BusinessException.class);
            if (index != -1) {
                final List<Throwable> list = ExceptionUtils.getThrowableList(e);
                BusinessException businessException = (BusinessException) list.get(list.size() - 1);
                List<String> messageValues  = getWebMessageValuesList(businessException.getMessages(),request);
                responseMap.put("validationResposne", "invalid");
                responseMap.put("message", messageValues);
            } else {
                responseMap.put("validationResposne", "error");
                BaseLoggers.exceptionLogger.error(e.getMessage(),e);
            }

        } finally {
            flushCurrentTransaction();
            return responseMap;
        }
    }

    /**
     * Start task allocation workflow.
     *
     * @param Id the id
     * @param masterID the master id
     * @param request the request
     * @return String
     * @throws ClassNotFoundException the class not found exception
     * @description Action method to send for approval maker records
     */
    @PreAuthorize("hasAuthority('MAKER_'+#xmlFilePath.toUpperCase()) ")
    @RequestMapping(value = "/{masterID}/approval" ,method=RequestMethod.POST)
    @MonitoredWithSpring(name = "MAC_START_TASK_ACTION_WORKFLOW")
    public @ResponseBody
    String startTaskAllocationWorkflow(@RequestParam("recordIds[]") Long[] Id, @PathVariable("masterID") String masterID,@PathVariable("xmlFilePath") String xmlFilePath,
                                       HttpServletRequest request) throws ClassNotFoundException {
        String msg = "";
        String successMsg = "";
        try {
            setEntityPath(masterID, masterConfigurationRegistry.getEntityClass(masterID));
            Class<BaseMasterEntity> entityName = getMasterEntityClass(masterID);
            UserInfo currentUser = getUserDetails();
            EntityId updatedById = currentUser.getUserEntityId();
            successMsg = commonFileIOMasterGridLoad.getResourceBundleFileReader(request, "msg.sentForApproval");
            for (Long id : Id) {
                if (id != null) {
                    EntityId masterEntityId = new EntityId(entityName, id);
                    validateOperation(entityName, id, MasterApprovalFlowConstants.sendForApproval,
                            MasterApprovalFlowConstants.AUTOAPPROVAL_WORKFLOW_DEFINITION_ID,
                            MasterApprovalFlowConstants.autoApproval,
                            MasterApprovalFlowConstants.WORKFLOW_DEFINITION_ID,
                            MasterApprovalFlowConstants.CHECKER_APPROVAL_TASK_WF_ID);
                    makerCheckerService.startMakerCheckerFlow(masterEntityId, updatedById);
                }

            }

            msg = successMsg;
        } catch (Exception e) {
            throw new SystemException(e);
        }

        return msg;
    }

    /**
     * Delete master entity.
     * @param <T>
     *
     * @param Id the id
     * @param masterID the master id
     * @param request the request
     * @return String
     * @throws ClassNotFoundException the class not found exception
     * @description Action method to auto approve maker records
     */
    @PreAuthorize("hasAuthority('MAKER_'+#xmlFilePath.toUpperCase())")
    @RequestMapping(value = "/{masterID}/delete",method= RequestMethod.POST)
    public @ResponseBody
    <T> Map<String,String> deleteMasterEntity(@RequestParam("recordIds[]") Long[] Id, @PathVariable("masterID") String masterID,@PathVariable("xmlFilePath") String xmlFilePath,
                                  HttpServletRequest request) throws ClassNotFoundException {
        String msg = "";
        String successMsg = "";
        BaseMasterEntity baseEntity;
        Map<String, String> notifMessageMap=new HashMap<>();
        try {
            setEntityPath(masterID, masterConfigurationRegistry.getEntityClass(masterID));
            Class<T> entityClass = (Class<T>) getEntityClass(masterID);
            UserInfo currentUser = getUserDetails();
            EntityId updatedById = currentUser.getUserEntityId();
            StringBuilder displayNames = new StringBuilder();
            boolean isDeleteAllowed=true;
            Boolean isChildDeletionCheckEnabled=BaseMasterDependency.isConfigPresent();
                if (isChildDeletionCheckEnabled) {
                    DeletionPreValidator deletionPreValidator = AnnotationUtils.findAnnotation(entityClass, DeletionPreValidator.class);
                    baseEntity = (BaseMasterEntity) entityClass.newInstance();
                    if (deletionPreValidator != null) {
                        for (long id : Id) {

                            Map<Class, String> entityValidator = BaseMasterDependency.getDependencyGraphForEntity(entityClass);
                            if (MapUtils.isNotEmpty(entityValidator)) {
                                for (Map.Entry m : entityValidator.entrySet()) {
                                    String query = (String) m.getValue();
                                    JPAQueryExecutor jpaQueryExecutor = new JPAQueryExecutor(query);
                                    jpaQueryExecutor.addParameter("id", id);
                                    List<Object> list = entityDao.executeQuery(jpaQueryExecutor);
                                    if (CollectionUtils.isNotEmpty(list)) {
                                        isDeleteAllowed = false;
                                        break;
                                    }
                                }
                            }
                            if (!isDeleteAllowed) {
                                Object object = entityDao.find(baseEntity.getClass(), id);
                                Method getDisplayName = baseEntity.getClass().getDeclaredMethod("getDisplayName");
                                String displayName = (String) getDisplayName.invoke(object);
                                if (displayName != null && !displayName.isEmpty()) {
                                    if (displayNames.length() == 0) {
                                        displayNames.append(displayName);
                                    } else {
                                        displayNames.append(",").append(displayName);
                                    }
                                }
                            }
                        }
                    }
                    if (displayNames.length() != 0) {
                        Locale loc = RequestContextUtils.getLocale(request);
                        notifMessageMap.put(NOTIF_MAP_FAILURE_KEY, getMessageAgainstKey("label.ChildDeletion.records.found",loc) + displayNames.toString());
                        return notifMessageMap;
                    }
                }


            if(isDeleteAllowed) {
                for(long id : Id) {
                    baseEntity = (BaseMasterEntity) entityClass.newInstance();
                    baseEntity.setId(id);
                    validateOperation(baseEntity.getClass(), id, MasterApprovalFlowConstants.delete);
                    BaseMasterEntity originalEntity = entityDao.find(baseEntity.getClass(), baseEntity.getId());                    
                    int approvalStatus = originalEntity.getApprovalStatus();
                    if(approvalStatus ==ApprovalStatus.UNAPPROVED_ADDED || approvalStatus == ApprovalStatus.CLONED || approvalStatus == ApprovalStatus.UNAPPROVED_MODIFIED) {
                      successMsg = commonFileIOMasterGridLoad.getResourceBundleFileReader(request, "msg.deleted.draft");
                    }else {
                      successMsg = commonFileIOMasterGridLoad.getResourceBundleFileReader(request, "msg.deleted");
                    }
                    makerCheckerService.masterEntityMarkedForDeletion(baseEntity, updatedById);
                }
            }
            msg = successMsg;
            notifMessageMap.put(NOTIF_MAP_SUCCESS_KEY,msg);
        } catch (Exception e) {
            throw new SystemException(e);
        }
        flushCurrentTransaction();
        return notifMessageMap;
    }

    private <T extends BaseMasterEntity> void validateOperation(Class<T> entityClass,Long id,String ... operations ) {
        BaseMasterEntity persistedEntity = (BaseMasterEntity)baseMasterService.getMasterEntityWithActionsById(entityClass,
                id, getUserDetails().getUserEntityId().getUri());
        if (persistedEntity.getViewProperties() == null
                || !canPerformOperation(persistedEntity.getViewProperties(),
                operations)) {
            throw new AccessDeniedException("User is not authorised to perform this operation");
        }
    }

    @SuppressWarnings("unchecked")
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

    /**
     * Sets the entity path.
     *
     * @param entityPath the new entity path
     */
    private void setEntityPath(String masterEntity, String entityPath) {
        threadSafeMap.putIfAbsent(masterEntity, entityPath);
    }

    /**
     * Gets the entity class.
     *
     * @return the entity class
     */

    @SuppressWarnings("unchecked")
    private Class<?> getEntityClass(String keyName) {
        Class<?> entityClass;
        String entityPath = threadSafeMap.get(keyName);
        try {
            entityClass = Class.forName(entityPath);
        } catch (ClassNotFoundException e) {
            throw new SystemException(e);
        }
        return entityClass;
    }

    public int getTotalRecordsInClass(Class<Serializable> entityClass, String uri) {
        int totalRecords = baseMasterService.getTotalRecordSize(entityClass, getUserDetails().getUserEntityId().getUri());
        return totalRecords;

    }

    /**
     * Gets the Base Master entity class.
     *
     * @return the Base Master entity class for Approved Records in Maker-Checker Flow
     */
    @SuppressWarnings("unchecked")
    private Class<BaseMasterEntity> getMasterEntityClass(String masterEntity) {
        Class<BaseMasterEntity> entityClass;
        String entityPath = threadSafeMap.get(masterEntity);
        try {
            entityClass = (Class<BaseMasterEntity>) Class.forName(entityPath);
        } catch (ClassNotFoundException e) {
            throw new SystemException(e);
        }
        return entityClass;
    }

    /*method to add clone*/

    /**
     * Clone master entity.
     * @param <T>
     *
     * @param Id the id
     * @param masterID the master id
     * @param request the request
     * @return String
     * @throws ClassNotFoundException the class not found exception
     * @description Action method to make clone for master records
     */
    @PreAuthorize("hasAuthority('MAKER_'+#xmlFilePath.toUpperCase())")
    @RequestMapping(value = "/{masterID}/createClone",method= RequestMethod.POST)
    public @ResponseBody
    <T> String cloneMasterEntity(@RequestParam("recordIds[]") Long[] Id, @PathVariable("masterID") String masterID,@PathVariable("xmlFilePath") String xmlFilePath,
                                 HttpServletRequest request) throws ClassNotFoundException {

        String msg = "Cloning failed";
        String successMsg = "";
        BaseMasterEntity baseMasterEntity = null;
        try {
            successMsg = commonFileIOMasterGridLoad.getResourceBundleFileReader(request, "msg.cloned");
            String entityClassName = masterConfigurationRegistry.getEntityClass(masterID);
            Class<? extends BaseMasterEntity> entityClass = (Class<? extends BaseMasterEntity>) Class
                    .forName(entityClassName);

            UserInfo currentUser = getUserDetails();
            EntityId updatedById = currentUser.getUserEntityId();
            for (long id : Id) {
                if(HistoryOptimizable.class.isAssignableFrom(entityClass)) {
                	baseMasterEntity = BaseMasterUtils.getMergeEditedRecords(entityClass,id);
                }else {
                	baseMasterEntity = baseMasterService.getMasterEntityById(entityClass, id);
                }
                validateOperation(entityClass, id, MasterApprovalFlowConstants.CLONE);
                makerCheckerService.createMasterEntityClone(baseMasterEntity, updatedById);

            }
            msg = successMsg;
        } catch (Exception e) {
            throw new SystemException("Error while cloning object(s) of " + masterID + " object.", e);
        }
        return msg;
    }

  /*  @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    <T> String handleError(HttpServletRequest req, Exception ex) {
        BaseLoggers.exceptionLogger.error("handleError called with exception : " + ex + "message : " + ex.getMessage(), ex);
        String msg = "";
        if (ex instanceof RuntimeException){
            msg=ex.getMessage();
            msg="Cannot approve master";
        }
        return msg;


    }*/

    public String getMessageAgainstKey(String key, Locale locale) {
        String message = "";

        if (null != key && !key.equals("")) {
            message = messageSource.getMessage(key, null, key, locale);
        }
        return message;
    }
}
