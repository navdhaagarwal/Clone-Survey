package com.nucleus.master;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ReflectionUtils;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.entity.CloneOptions;
import com.nucleus.entity.EntityLifeCycleData;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserInfo;


public class BaseMasterUtils {

    public static final String I18N_ERR_INVLD_CHLD_STT	= "error.master.approval.invalidChildState";
    public static final String I18N_ERR_UUID_NULL		= "error.master.record.invalidUuid";
    public static final String MODIFICATION_OPERATION 	= "M";
   
    private BaseMasterUtils() {
        throw new UnsupportedOperationException("Instantiation of the class is not allowed.");
    }
    
    private static void historyCloner(List<? extends BaseMasterEntity> originalList, List<? extends BaseMasterEntity> changedList, CloneOptions cloneOptions) {
    	for (BaseMasterEntity item : originalList) {
    		BaseMasterEntity originalRecord=findByUuid(changedList, item.getEntityLifeCycleData().getUuid());
    		if (originalRecord != null) {
    			item.copyFrom(originalRecord, cloneOptions);    
    		}
    	}
    }
    
    private static void updateFoundRecord(BaseMasterEntity item, List<? extends BaseMasterEntity> originalList,
    		BaseMasterEntity originalRecord, CloneOptions cloneOptions) {
        if (item.getApprovalStatus() == ApprovalStatus.CHILD_DELETED) {
            updateRecordForDeletion(originalList,originalRecord,cloneOptions);
        } else if (item.getApprovalStatus() == ApprovalStatus.CHILD_MODIFIED) {            
            originalRecord.populateFrom(item, cloneOptions);
        }
    }
    
    public static <T extends BaseMasterEntity> void mergeModificationsToOrigionalEntity(List<T> originalList, List<T> changedList, CloneOptions cloneOptions) {
        if (cloneOptions.getCloneOptionAsBoolean(CloneOptionConstants.DONT_CLONE_CHILD_KEY)) {
            return ;
        }
        if (ValidatorUtils.hasNoElements(changedList)) {
            return ;
        }
        if (cloneOptions.getCloneOptionAsBoolean(CloneOptionConstants.UPDATE_EXISTING_CHILD_ONLY_KEY)) {
            historyCloner(originalList,changedList,cloneOptions);
            return ; 
        }
        for (BaseMasterEntity item : changedList) {
            BaseMasterEntity originalRecord = findByUuid(originalList, item.getEntityLifeCycleData().getUuid());
            if (originalRecord != null) {
                updateFoundRecord(item, originalList, originalRecord, cloneOptions);
            } else if (item.getApprovalStatus() == ApprovalStatus.CHILD_ADDED) {
                originalRecord = (BaseMasterEntity)item.cloneYourself(cloneOptions);
                originalList.add((T) originalRecord);
            } else {
                BaseLoggers.exceptionLogger.error("Child of entity which is getting populated from modified is in invalid state. It's approval status should be in (13,14,15)");
                Message message = new Message(I18N_ERR_INVLD_CHLD_STT,Message.MessageType.ERROR,"Child of entity which is getting populated from modified is in invalid state. It's approval status should be in (13,14,15)");
                throw ExceptionBuilder
                	.getInstance(BusinessException.class)
                	.setMessage(message)
                	.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue())
                	.build();
            }
        }
    }
    
    private static void updateRecordForDeletion(List<? extends BaseMasterEntity> originalList, BaseMasterEntity originalRecord, CloneOptions cloneOptions) {
        if (cloneOptions.getCloneOptionAsBoolean(CloneOptionConstants.SOFT_DELETE_CHILD_KEY)) {
            originalRecord.setApprovalStatus(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
        } else {
            originalList.remove(originalRecord);
        }
    }

    public static  BaseMasterEntity findByUuid(List<? extends BaseMasterEntity> originalList, final String uuid) {

        return (BaseMasterEntity)CollectionUtils.find(originalList, arg -> ((BaseEntity) arg).getEntityLifeCycleData().getUuid().equalsIgnoreCase(uuid));
    }
    
    public static BaseMasterEntity cloneAndUpdateChildWithStatusBeforeMakingChanges(BaseMasterEntity baseMasterEntity, int approvalStatus) {
        BaseMasterEntity clonedEntity = (BaseMasterEntity)baseMasterEntity.cloneYourself(CloneOptionConstants.MAKER_CHECKER_CLONING_OPTION);
        clonedEntity.setApprovalStatus(approvalStatus);
        return clonedEntity;
    }
    
    public static UserInfo getCurrentUser() {
        UserInfo userInfo = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null && null != securityContext.getAuthentication()) {
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (UserInfo.class.isAssignableFrom(principal.getClass())) {
                userInfo = (UserInfo) principal;
            }
        }
        return userInfo;
    }
    
    /**
     * Returns the merged records having modified child, newly added, deleted 
     * also already existing child records. Parent record will contain records 
     * in <code>ApprovalStatus.DELETED_APPROVED_IN_HISTORY</code> .
     *  
     * @param className		Class instance of parent (e.g. Industry.class)
     * @param id			id of the Record.
     * @return
     */
    public static BaseMasterEntity getMergeEditedRecords(Class<? extends BaseMasterEntity> className, Long id) {
    	BaseMasterService baseMasterService = NeutrinoSpringAppContextUtil.getBeanByName("baseMasterService", BaseMasterService.class);
    	BaseMasterEntity modifiedRecord = baseMasterService.getMasterEntityWithActionsById(className, id, getCurrentUser().getUserEntityId().getUri());
        if (modifiedRecord.getApprovalStatus() == ApprovalStatus.APPROVED
        		|| modifiedRecord.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED
        		|| modifiedRecord.getApprovalStatus() == ApprovalStatus.APPROVED_DELETED)
        {
            BaseMasterEntity entityObj = (BaseMasterEntity)modifiedRecord.cloneYourself(CloneOptionConstants.COPY_WITH_ID_AND_UUID_AND_STATUS);
            entityObj.setMasterLifeCycleData(modifiedRecord.getMasterLifeCycleData());
            entityObj.setEntityLifeCycleData(modifiedRecord.getEntityLifeCycleData());
            entityObj.setViewProperties(modifiedRecord.getViewProperties());
            return entityObj;
        }          
        BaseMasterEntity originalRecord = baseMasterService.getLastApprovedEntityByUnapprovedEntityId(modifiedRecord.getEntityId());
        if (originalRecord == null) {
            return modifiedRecord;
        }
        BaseMasterEntity clonedRecord = (BaseMasterEntity)originalRecord.cloneYourself(CloneOptionConstants.COPY_WITH_ID_AND_UUID_AND_STATUS);
        clonedRecord.copyFrom(modifiedRecord, CloneOptionConstants.COPY_WITH_ID_AND_UUID_AND_STATUS);
        clonedRecord.setMasterLifeCycleData(modifiedRecord.getMasterLifeCycleData());
        clonedRecord.setEntityLifeCycleData(modifiedRecord.getEntityLifeCycleData());
        clonedRecord.setViewProperties(modifiedRecord.getViewProperties());
        return clonedRecord;
    }

	/**
	 * After changes in parent class this method should be called to get 
	 *  the updated list of child records.
	 * 
	 * @param parentRecord
	 * @param childPropertyName
	 * @param changedList
	 * @return
	 */
	public static List getUpdatedChildRecordsFromChangedChildList(BaseMasterEntity parentRecord,
    		String childPropertyName, List<? extends BaseMasterEntity> changedList) {
		return getUpdatedChildRecordsFromChangedChildList(parentRecord,childPropertyName,changedList,null);
	}
	
    /**
     * In case of Parent child bidirectional relationship on edit this method should be called 
     *  to get updated list of child records.
     *  
     * @param parentRecord Parent class object
     * @param childPropertyName name of child property in parent class
     * @param changedList List of child records
     * @param parentPropertyName Name of Parent property in child class
     * @return updated list of child records
     */
	@SuppressWarnings("unchecked")
	public static List getUpdatedChildRecordsFromChangedChildList(BaseMasterEntity parentRecord,
    		String childPropertyName, List<? extends BaseMasterEntity> changedList, String parentPropertyName ) {
    	List<BaseMasterEntity> resultList = new ArrayList<>();
    	
    	if (parentRecord.getId() == null && ValidatorUtils.hasElements(changedList)) {
    		for (BaseMasterEntity entity : changedList) {
    			entity.setApprovalStatus(ApprovalStatus.CHILD_ADDED);
    			resultList.add(entity);
    		}
    		return resultList;
    	}
    	if (parentRecord.getId() == null) {
    		return resultList;
    	}
        BaseMasterEntity clonedParentRecord = getMergeEditedRecords(parentRecord.getClass(), parentRecord.getId());
        int parentStatus = clonedParentRecord.getApprovalStatus();
        Field field = ReflectionUtils.findField(parentRecord.getClass(), childPropertyName);
        field.setAccessible(true);
		List<BaseMasterEntity> originalList = (List<BaseMasterEntity>) ReflectionUtils.getField(field, clonedParentRecord);
        if (((parentStatus == ApprovalStatus.UNAPPROVED_ADDED)
        	|| parentStatus == ApprovalStatus.CLONED) && ValidatorUtils.hasElements(changedList)) {
        	BaseMasterEntity originalRecord = null;
            for (BaseMasterEntity item : changedList) {
            	originalRecord = findById(originalList, item.getId());
            	if (originalRecord != null) {
            		item.setEntityLifeCycleData(originalRecord.getEntityLifeCycleData());
            	}
                item.setApprovalStatus(ApprovalStatus.CHILD_ADDED);
                resultList.add(item);
            }
			if (parentPropertyName != null) {
				BaseMasterEntity changedItemWithGivenId = null;
				for (BaseMasterEntity originalRecordItem : originalList) {
					changedItemWithGivenId = findById(changedList, originalRecordItem.getId());
					if (changedItemWithGivenId == null) {
						Field fieldChild = ReflectionUtils.findField(originalRecordItem.getClass(), parentPropertyName);
						fieldChild.setAccessible(true);
						ReflectionUtils.setField(fieldChild, originalRecordItem, null);
					}
				}
			}
            return resultList;
        }
        if (parentStatus == ApprovalStatus.UNAPPROVED_MODIFIED) {
    		//add records with deleted status.
    		addDeletedRecords(originalList, parentRecord.getClass(), parentRecord.getId(), childPropertyName);
    	}
        if (parentStatus == ApprovalStatus.APPROVED || parentStatus == ApprovalStatus.UNAPPROVED_MODIFIED) {
            addChildRecordsWithUpdatedStatus(resultList, originalList, changedList);
        }
        return resultList;
	}
	private static  BaseMasterEntity findById(List<? extends BaseMasterEntity> originalList, final Long id) {

        return (BaseMasterEntity)CollectionUtils.find(originalList, arg -> ((BaseEntity) arg).getId().equals(id));
    }
	
	@SuppressWarnings("unchecked")
    private static void addDeletedRecords(List<BaseMasterEntity> originalList, 
    		Class<? extends BaseMasterEntity> className, Long id, String childPropertyName) {
    	BaseMasterService baseMasterService = NeutrinoSpringAppContextUtil.getBeanByName("baseMasterService", BaseMasterService.class);
    	BaseMasterEntity parentRecord = baseMasterService.getMasterEntityWithActionsById(className, id, getCurrentUser().getUserEntityId().getUri());
        Field field = ReflectionUtils.findField(parentRecord.getClass(), childPropertyName);
    	field.setAccessible(true);
		List<BaseMasterEntity> childList = (List<BaseMasterEntity>) ReflectionUtils.getField(field, parentRecord);
    	if (ValidatorUtils.hasNoElements(childList)) {
    		return ;
    	}
        for (BaseMasterEntity childRecord : childList) {
        	if (childRecord.getApprovalStatus() == ApprovalStatus.CHILD_DELETED
        		|| childRecord.getApprovalStatus() == ApprovalStatus.DELETED_APPROVED_IN_HISTORY) {
        		originalList.add(childRecord);
        	}
        }
	}

	private static void addChildRecordsWithUpdatedStatus(List<BaseMasterEntity> resultList,
            List<? extends BaseMasterEntity> originalList,
            List<? extends BaseMasterEntity> changedList) {
    	if (ValidatorUtils.hasNoElements(changedList)) {
    		return ;
    	}
    	Map<Long, BaseMasterEntity> clonedMap = getMapFromListWithId(originalList);
    	for (BaseMasterEntity changedChild : changedList) {
    		if (changedChild.getId() == null) {
    			// New Record Added
    			changedChild.setApprovalStatus(ApprovalStatus.CHILD_ADDED);
    			resultList.add(changedChild);
    		} else if (MODIFICATION_OPERATION
    				.equalsIgnoreCase(changedChild.getOperationType())) {
    			// Existing Modified
    			addModifiedChildRecord(resultList, clonedMap, changedChild);
    			clonedMap.remove(changedChild.getId());
    		} else if (changedChild.getOperationType() == null) {
    			BaseMasterEntity removedEntity = clonedMap.remove(changedChild.getId());
    			if (removedEntity != null && (removedEntity.getApprovalStatus() != ApprovalStatus.APPROVED
    				&& removedEntity.getApprovalStatus() != ApprovalStatus.APPROVED_MODIFIED)) {
        			resultList.add(removedEntity);
    			}
    		}
    	}
    	markForDeletion(resultList, clonedMap);
    }
    
    private static void markForDeletion(List<BaseMasterEntity> resultList,
    		Map<Long, BaseMasterEntity> clonedMap) {
    	for (Map.Entry<Long, BaseMasterEntity> record : clonedMap.entrySet()) {
    		BaseMasterEntity item = record.getValue();
            BaseMasterEntity clonedRecord;
    		if (item.getApprovalStatus() == ApprovalStatus.APPROVED) {
            	clonedRecord = (BaseMasterEntity) item.cloneYourself(CloneOptionConstants.CHILD_CLONING_OPTION);
            	clonedRecord.setApprovalStatus(ApprovalStatus.CHILD_DELETED);
    			resultList.add(clonedRecord);
    		} else if(item.getApprovalStatus() != ApprovalStatus.DELETED_APPROVED_IN_HISTORY
    				&& item.getApprovalStatus() != ApprovalStatus.CHILD_ADDED
    				&& item.getApprovalStatus() != ApprovalStatus.CHILD_MODIFIED) {
    			item.setApprovalStatus(ApprovalStatus.CHILD_DELETED);
    			resultList.add(item);
    		}
    	}
    }
    
    private static void addModifiedChildRecord(
            List<BaseMasterEntity> resultList,
            Map<Long, BaseMasterEntity> clonedMap, BaseMasterEntity changedChild) {
        BaseMasterEntity originalRecord = clonedMap.get(changedChild.getId());
        int status = originalRecord.getApprovalStatus();
        changedChild.getEntityLifeCycleData().setUuid(originalRecord.getUuid());
        EntityLifeCycleData cycleData = originalRecord.getEntityLifeCycleData();
        if (cycleData == null || cycleData.getUuid() == null) {
              BaseLoggers.exceptionLogger.error("UUID should not be null ");
              Message message = new Message(I18N_ERR_UUID_NULL, Message.MessageType.ERROR, "UUID should not be null ");
              throw ExceptionBuilder
              	.getInstance(BusinessException.class)
              	.setMessage(message)
              	.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue())
              	.build();
        }
        changedChild.getEntityLifeCycleData().setUuid(cycleData.getUuid());
        BaseMasterEntity clonedRecord;
        if (status == ApprovalStatus.APPROVED) {
        	clonedRecord = (BaseMasterEntity) changedChild.cloneYourself(CloneOptionConstants.CHILD_CLONING_OPTION);
        	clonedRecord.setApprovalStatus(ApprovalStatus.CHILD_MODIFIED);
        } else {
        	//Added/Modified in previous step
            changedChild.setApprovalStatus(originalRecord.getApprovalStatus());
            clonedRecord = changedChild;
        }
        resultList.add(clonedRecord);
    }
    
    private static Map<Long, BaseMasterEntity> getMapFromListWithId(
            List<? extends BaseMasterEntity> originalList) {
        Map<Long, BaseMasterEntity> map = new HashMap<>();
        if (ValidatorUtils.hasNoElements(originalList))
            return map;
        for (BaseMasterEntity item : originalList) {
            map.put(item.getId(), item);
        }
        return map;
    }
}