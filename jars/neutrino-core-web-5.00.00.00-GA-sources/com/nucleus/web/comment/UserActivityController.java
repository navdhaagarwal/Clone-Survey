/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.cas.parentChildDeletionHandling.BaseMasterDependency;
import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import com.nucleus.cas.parentChildDeletionHandling.DependencyUsageVO;
import com.nucleus.cas.parentChildDeletionHandling.ParentChildDeleteHandlingService;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.EventService;
import com.nucleus.event.EventTypes;
import com.nucleus.event.FormatType;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.makerchecker.UnapprovedEntityData;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.master.audit.MasterChangeVO;
import com.nucleus.master.audit.service.MasterChangeAuditLogGenerator;
import com.nucleus.reason.ReasonVO;
import com.nucleus.tag.service.TagService;
import com.nucleus.user.User;
import com.nucleus.user.UserAuditLog;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.utility.ControllerConstants;

/**
 * @author Nucleus Software Exports Limited This class is to retrieve the
 *         associated events either with user or with entity and displaying them
 *         in a user readable format.
 */
@Controller
@RequestMapping(value = "/useractivity")
public class UserActivityController extends BaseController {

    @Inject
    @Named("eventService")
    private EventService        eventService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService   baseMasterService;

    @Inject
    @Named("userService")
    private UserService         userService;

    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore   userManagementServiceCore;

    @Inject
    @Named("masterConfigurationRegistry")
    private MasterConfigurationRegistry masterConfigurationRegistry;

    @Inject
    @Named("parentChildDeleteHandlingService")
    private ParentChildDeleteHandlingService parentChildDeleteHandlingService;

    @Inject
    @Named("masterChangeAuditLogGenerator")
    private MasterChangeAuditLogGenerator auditGenerator;
    
    @Inject
    @Named("tagService")
    private TagService tagService;

    
    private final static String        userEntityUri         = "com.nucleus.user.User";

    private static final String USER_CREATE_EVENT     = "Create";
    private static final String USER_EDIT_EVENT       = "Edit";
    private static final String USER_INACTIVATE_EVENT = "Inactivate";

    /**
     * This Method will return locale specific view to render user activities
     * associated with an entity in activity panel.
     * 
     * @param map
     * @param entityUri
     * @param id
     * @param locale
     * @return
     */
    
    @RequestMapping(value = "/retrieveactivity/{id}")
    public String retrieveActivity(ModelMap map, @RequestParam("currentEntityUri") String entityUri, @PathVariable Long id,
            Locale locale) {
        List<Map<String, Object>> activityInfoList = new ArrayList<Map<String, Object>>();
        if (StringUtils.isNotBlank(entityUri)) {
        	
        	String currentEntityUri = null;
        	try
        	{
        		Entity entity = entityDao.find((Class<? extends Entity>) Class.forName(entityUri), id);
        		currentEntityUri = (entity != null)? entity.getClass().getName(): entityUri;
        	}
        	catch (ClassNotFoundException e)
        	{ 	
        		currentEntityUri = entityUri; 
        	}
        	List<Event> eventList = eventService.getAllEventsByOwnerEntityUri(currentEntityUri + ":" + id);
        	
            //List<Event> eventList = eventService.getAllEventsByOwnerEntityUri(entityUri + ":" + id);
            if (eventList != null && !eventList.isEmpty()) {
                for (Event event : eventList) {
                    if (StringUtils.isNotBlank(event.getAssociatedUserUri())) {
                        Map<String, Object> singleActivityInfo = new HashMap<String, Object>();
                        long userId = EntityId.fromUri(event.getAssociatedUserUri()).getLocalId();
                        String activityMessage;
                        if(event.getEventType() == EventTypes.WORKFLOW_SAVE_EVENT){
                            activityMessage = event.getPersistentProperty("MESSAGE");
                        } else{
                            activityMessage = eventService.getEventTypeStringRepresentation(event, locale, FormatType.ACTIVITY_STREAM);
                        }
                        singleActivityInfo.put("userId", userId);
                        singleActivityInfo.put("activityMessage", activityMessage);
                        singleActivityInfo.put("dateTime", event.getEventTimestamp());
                        singleActivityInfo.put("ReasonForActiveInactve",event.getPersistentProperty("ReasonForActiveInactve"));

                        activityInfoList.add(singleActivityInfo);
                    }
                }
            }
            map.put("activityInfoList", activityInfoList);
        }
        return "activitypage";
    }

    /**
     * This Method will return locale specific view to render user activities
     * associated with an entity in activity panel.
     * 
     * @param map
     * @param entityUri
     * @param id
     * @param locale
     * @return
     */
    @RequestMapping(value = "/getParentId/{id}")
    public @ResponseBody
    String getParentEntityId(ModelMap map, @RequestParam("currentEntityUri") String entityUri, @PathVariable Long id,
            Locale locale) {
        Long originalId = null;
        EntityId entityId = EntityId.fromUri(entityUri + ":" + id.toString());
        if (entityId == null || !BaseMasterEntity.class.isAssignableFrom(entityId.getEntityClass())) {
            originalId = id;
        } else {
            BaseMasterEntity originalMasterEntity = baseMasterService.getLastApprovedEntityByUnapprovedEntityId(entityId);
            if (originalMasterEntity == null) {
                originalId = id;
            } else {
                originalId = originalMasterEntity.getId();
            }

        }
        return originalId.toString();
    }

    @PreAuthorize("hasAuthority('MAKER_USER') or hasAuthority('VIEW_USER') or hasAuthority('CHECKER_USER')")
    @RequestMapping(value = "/retrieveUserActivity/{id}")
    public String retrieveUserActivity(ModelMap map, @RequestParam("currentEntityUri") String entityUri,
            @PathVariable Long id, Locale locale) {
        List<Map<String, Object>> activityInfoList = new ArrayList<Map<String, Object>>();
        if (StringUtils.isNotBlank(entityUri)) {
            if (entityUri.equalsIgnoreCase(userEntityUri)) {
            	User user = baseMasterService.findById(User.class, id);
                List<UnapprovedEntityData> userAuditDetails = userService.fetchAuditLogOfUserByUserUUID(user.getEntityLifeCycleData().getUuid());
                List<Map<String, Object>> userIdAndApprovalStatusDetails = userService.getUserIdAndApprovalStatusByUUID(user.getEntityLifeCycleData().getUuid());
                Map<Long, Integer> userIdAndApprovalStatusMap = new HashMap<Long, Integer>();
                for(Map<String, Object> tempMap:userIdAndApprovalStatusDetails){
                		userIdAndApprovalStatusMap.put(Long.parseLong(tempMap.get("id").toString()),Integer.parseInt(tempMap.get("approvalStatus").toString()));
                }
                
                if (CollectionUtils.isNotEmpty(userAuditDetails)) {
                    for (UnapprovedEntityData singleAuditLog : userAuditDetails) {
							Long changedUserId = singleAuditLog.getChangedEntityId().getLocalId();
							Integer status = userIdAndApprovalStatusMap.get(changedUserId);
							if (status != null && (status == ApprovalStatus.UNAPPROVED_HISTORY || status == ApprovalStatus.UNAPPROVED)) {
								continue;
							}
							
                            Map<String, Object> singleActivityInfo = new HashMap<String, Object>();
                                if (singleAuditLog.getEntityLifeCycleData() != null) {
                                    singleActivityInfo.put("dateTime", singleAuditLog.getEntityLifeCycleData()
                                            .getCreationTimeStamp());
                                }
                                if(null != singleAuditLog.getOriginalEntityId()){
                                	singleActivityInfo.put("activityMessage", ControllerConstants.USER_UPDATED_EVENT);
                                	singleActivityInfo.put("originalUserId", singleAuditLog.getOriginalEntityId().getLocalId());
                                }else{
                                	singleActivityInfo.put("activityMessage", ControllerConstants.USER_CREATED_EVENT);
                                	singleActivityInfo.put("originalUserId", singleAuditLog.getOriginalEntityId());
                                }
                                long userId = singleAuditLog.getUserEntityId().getLocalId();
                                singleActivityInfo.put("userEntityId", userId);
                                singleActivityInfo.put("changedUserId", changedUserId);
                                activityInfoList.add(singleActivityInfo);
                            }
                    }

                    List<UserAuditLog> userAuditLogList=userManagementServiceCore.getUserAuditLogListByUserId(id);
                    List<ReasonVO> reasonVOList=new ArrayList<>();
                    for(UserAuditLog userAuditLog:userAuditLogList){
                        ReasonVO reasonVO=new ReasonVO();
                        if(userAuditLog!=null){
                            if(userAuditLog.getUserEvent()!=null){
                                reasonVO.setUserEvent(userAuditLog.getUserEvent());
                            }
                            if(null != userAuditLog.getBlockReason()){
                                reasonVO.setName(userAuditLog.getBlockReason().getName());
                            }else if(null != userAuditLog.getInactiveReason()){
                                reasonVO.setName(userAuditLog.getInactiveReason().getName());
                            } else if(null != userAuditLog.getActiveReason()){
                                reasonVO.setName(userAuditLog.getActiveReason().getName());
                            }
                            if(userAuditLog.getEntityLifeCycleData().getCreationTimeStamp()!=null){
                                reasonVO.setCreationTimeStamp(userAuditLog.getEntityLifeCycleData().getCreationTimeStamp());
                            }
                            if(userAuditLog.getUserEntityId()!=null){
                                reasonVO.setUserEntityId(userAuditLog.getUserEntityId());
                            }
                        }
                        reasonVOList.add(reasonVO);
                    }
                    if(CollectionUtils.isNotEmpty(reasonVOList)) {
                        Collections.sort(reasonVOList, new Comparator<ReasonVO>() {
                            @Override
                            public int compare(ReasonVO o1, ReasonVO o2) {
                                return o1.getCreationTimeStamp().compareTo(o2.getCreationTimeStamp());
                            }
                        });
                    }
                    map.put("reasonVOList",reasonVOList);

                }
                map.put("activityInfoList", activityInfoList);
            }
        return "activityPageForUser";
    }

    @RequestMapping(value = "/getActivityAccordion/")
	public String getActivityAccordion(ModelMap map, @RequestParam("currentEntityUri") String entityUri,@RequestParam(value = "masterId", required = false) String masterId) {
		Object noteObject = null;
        Class<?> entityClass=null;
		try {
			java.lang.reflect.Method method = ClassLoader.class.getDeclaredMethod("findLoadedClass",
					new Class[] { String.class });
			method.setAccessible(true);
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			noteObject = method.invoke(classLoader, "com.nucleus.note.Note");
		} catch (Exception e) {
			// Nothing To Do Here
		}
		if(masterId!=null && !masterId.isEmpty()){
            String entityPath=masterConfigurationRegistry.getEntityClass(masterId);
            if(entityPath!=null && !entityPath.isEmpty()){
                try {
                    entityClass=Class.forName(entityPath);
                } catch (ClassNotFoundException e) {
                    BaseLoggers.exceptionLogger.error(e.getMessage());
                }
                // show usage tab
                if(entityClass!=null && BaseMasterDependency.isConfigPresent() ) {
                    DeletionPreValidator deletionPreValidator = AnnotationUtils.findAnnotation(entityClass, DeletionPreValidator.class);
                    if(deletionPreValidator !=null){
                        map.put("showUsage",true);
                    }
                }
                // show audit tag
                if(entityClass!=null && auditGenerator.isMasterChildAUditEnabld() && auditGenerator.isAuditable(entityClass)){
                	 map.put("showAudit",true);
                }
            }
        }
		
		map.put("currentEntityClassName", entityUri);
		map.put("isNoteAvailable", noteObject != null);
		
        String finalExcludedCharacters= tagService.getExcludedSpecialCharactersInTag();
        map.put("specialCharacters",finalExcludedCharacters);
		
		return "userActivityAccordion";
	}

    @RequestMapping(value = "/getUsageTabContent")
    public String getUsageTabContent(ModelMap map, @RequestParam(value = "masterId") String masterId ,@RequestParam("id") Long id) {
        List<DependencyUsageVO> dependencyUsageVOList = new ArrayList<>();
        dependencyUsageVOList=parentChildDeleteHandlingService.prepareDependencyData(masterId,id);
        map.put("dependencyList",dependencyUsageVOList);
        return "parentChildUsage";
    }

    @RequestMapping(value = "/getAuditTabContent")
    public String getAuditTabContent(ModelMap map, @RequestParam(value = "masterId") String masterId ,@RequestParam("id") Long id) {
        List<MasterChangeVO> masterAuditVOList = new ArrayList<>();
        masterAuditVOList=auditGenerator.getAuditDetailByEntity(masterId,id);
        map.put("AuditList",masterAuditVOList);
        return "masterAuditView";
    }
    
}
