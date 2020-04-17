package com.nucleus.master;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.ActionConfiguration;
import com.nucleus.makerchecker.ColumnConfiguration;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.persistence.ChildMasterDao;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.UserService;

import net.bull.javamelody.MonitoredWithSpring;

@Named("childMasterService")
@MonitoredWithSpring(name = "ChildMaster_Service_IMPL_")
public class ChildMasterServiceImpl extends BaseServiceImpl implements ChildMasterService {
	
	private ConcurrentHashMap<String, Class<?>> 	entityClassCache = new ConcurrentHashMap<>();

	@Inject
	@Named("baseMasterService")
	private BaseMasterService 						baseMasterService;

	@Inject
	@Named("masterConfigurationRegistry")
	private MasterConfigurationRegistry 			masterConfigurationRegistry;
	
	@Inject
	@Named("childMasterDao")
	private ChildMasterDao 							childMasterDao;
	
	@Inject
	@Named("userService")
	private UserService								userService;
	
    private static final String 					ERROR_MSG = "Exception in rendering data to Grid";

	
	private void putEntityClassInCache(String masterEntity, Class<?> entityPath) {
		entityClassCache.putIfAbsent(masterEntity, entityPath);
	}
	
	@Override
	public boolean isMatchesSearchCriteria(String searchTerm, Object columnValue) {
		if (columnValue == null) {
			return false;
		}
		String columnValueString = columnValue.toString().toUpperCase();
		return columnValueString.contains(searchTerm) || searchTerm.contains(columnValueString);
	}

	@Override
	public Map<String, Object> createDataTableFromEntityMapper(MasterConfigurationRegistry masterConfigurationRegistry,
			String childKey) {
		Map<String, Object> masterMap = new HashMap<>();
		List<ColumnConfiguration> columnConfigurationList = masterConfigurationRegistry.getColumnConfigurationList(childKey);
		List<ActionConfiguration> actionConfiguration = masterConfigurationRegistry.getActionConfigurationList(childKey);
		Boolean processingType = masterConfigurationRegistry.getProcessingType(childKey);
		masterMap.put("serverSide", processingType);
		masterMap.put("dataTableRecords", columnConfigurationList);
		masterMap.put("actionConfiguration", actionConfiguration);
		return masterMap;
	}

	private <T extends BaseMasterEntity> void renderAdditionalPropertyToGridData(List<T> childEntities) {
        try {
        	String createdBy = null;
            String reviewedBy = null;
            for (Object o : childEntities) {
                BaseMasterEntity singleEntity = (BaseMasterEntity) o;
                if (singleEntity.getEntityLifeCycleData() != null) {
                	createdBy = getCreatedByForEntity(singleEntity);
                	reviewedBy = getReviewedByForEntity(singleEntity);
                    singleEntity.addProperty("uuid", singleEntity.getEntityLifeCycleData().getUuid());
                }
                if (createdBy != null) {
                    singleEntity.addProperty("createdBy", createdBy);
                }
                if (reviewedBy != null) {
                    singleEntity.addProperty("reviewedBy", reviewedBy);
                }
            }
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error(ERROR_MSG, e);
        }
	}
	
	private String getCreatedByForEntity(BaseMasterEntity singleEntity) {
		String createdBy = null;
		EntityId createorEntityId = null;
    	if ( StringUtils.isNotBlank(singleEntity.getEntityLifeCycleData().getLastUpdatedByUri()) )
    		createorEntityId = EntityId.fromUri(singleEntity.getEntityLifeCycleData().getLastUpdatedByUri());
    	else
    		createorEntityId = singleEntity.getEntityLifeCycleData().getCreatedByEntityId();
        if (createorEntityId != null)
            createdBy = userService.getUserNameByUserId(createorEntityId.getLocalId());
		return createdBy;
	}
	
	private String getReviewedByForEntity(BaseMasterEntity singleEntity) {
		String reviewedBy = null;
		EntityId revieworEntityId = singleEntity.getMasterLifeCycleData().getReviewedByEntityId();
        if (revieworEntityId != null)
        	reviewedBy = userService.getUserNameByUserId(revieworEntityId.getLocalId());
		return reviewedBy;
	}
	
	@Override
	public Class<?> getEntityClass(String key) {
		Class<?> entityClass = null;
		String entityPath = masterConfigurationRegistry.getEntityClass(key);
		try {
			entityClass = Class.forName(entityPath);
		} catch (ClassNotFoundException e) {
			throw new SystemException(e);
		}
		return entityClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends BaseMasterEntity> Map<String, Object> getChildGridData(String parentKey, String childKey, String userUri, Long id, GridVO gridVO,
			List<ColumnConfiguration> columnConfigurationList) {
		Class<?> entityClass = entityClassCache.get(parentKey);
		if (entityClass == null) {
			entityClass = getEntityClass(parentKey);
			putEntityClassInCache(parentKey, entityClass);
		}
		T parentEntity = null;
		if (id.longValue() != 0) {
			parentEntity = childMasterDao.find((Class<T>)entityClass, id);
		}
		String childAttributeName = masterConfigurationRegistry.getAttributeNameInParentClass(childKey);
		List<T> childEntities = childMasterDao.loadPaginatedData(childAttributeName, entityClass, gridVO, parentEntity);
        Integer totalRecordCount = getTotalRecordSizeByParentEntity(childAttributeName, entityClass, parentEntity);
        Integer searchRecordsCount;
        if (ValidatorUtils.hasNoEntry(gridVO.getSearchMap())) {
        	searchRecordsCount = totalRecordCount;
        } else {
        	searchRecordsCount = childMasterDao.getSearchRecordsCount(childAttributeName, entityClass, gridVO, parentEntity);
        	if (searchRecordsCount == null) {
            	searchRecordsCount = 0;
            }
        }
        childEntities = childEntities == null ? Collections.emptyList() : childEntities;
        renderAdditionalPropertyToGridData(childEntities);
		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("parentEntity", parentEntity);
		returnMap.put("entityList", childEntities);
		returnMap.put("recordsCount", searchRecordsCount);
		returnMap.put("totalRecordsCount", totalRecordCount);
		return returnMap;
	}
	
	@Override
	public <T extends BaseMasterEntity> Integer getTotalRecordSizeByParentId(String childAttributeName, Class<T> entityClass, Long id) {
		return getTotalRecordSizeByParentEntity(childAttributeName, entityClass, childMasterDao.find(entityClass, id));
	}

	@Override
	public Integer getTotalRecordSizeByParentEntity(String childAttributeName, Class<?> entityClass,
			BaseMasterEntity parentEntity) {
		if (parentEntity == null) {
			return null;
		}
		return childMasterDao.getTotalRecordSize(childAttributeName, entityClass, parentEntity);
	}
}
