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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.grid.IGridService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.user.UserService;

/**
 * The Class MasterController.
 *
 * @author Nucleus Software Exports Limited
 */
@Named("masterGridService")
public class MasterGridServiceImpl implements IGridService {

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;

    @Inject
    @Named("userService")
	protected UserService       userService;

    private static final String ERROR_MSG= "Exception in rendering data to Grid";
    
    private static final String USER_NAME_HTML_TAG_SMALL="<small class='small-display-block'>[" ;
	private static final String USER_NAME_HTML_TAG_END_SMALL="]</small>" ;

    @SuppressWarnings({ "rawtypes" })
    @Override
    public Map<String, Object> loadPaginatedData(Class entityName, String userUri, Long parentId, Integer iDisplayStart,
            Integer iDisplayLength, String sortColName, String sortDir) {
    	GridVO gridVO = new GridVO();
        gridVO.setiDisplayStart(iDisplayStart);
        gridVO.setiDisplayLength(iDisplayLength);
        gridVO.setSortDir(sortDir);
        gridVO.setSortColName(sortColName);
        gridVO.setSearchMap(null);
    	return loadPaginatedData(gridVO, entityName, userUri, parentId);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<String, Object> findEntity(Class entityClass, String userUri,Integer iDisplayStart,
            Integer iDisplayLength, Map<String, Object> queryMap) {
        Map<String, Object> searchRecordMap = new HashMap<String, Object>();
        List<Object> searchRecords = baseMasterService.findEntity(entityClass, iDisplayStart, iDisplayLength, queryMap);
        for (Object bm : searchRecords) {
            BaseMasterEntity singleEntity = (BaseMasterEntity) bm;
            String createdBy = null;
            String reviewedBy = null;

            if (singleEntity.getEntityLifeCycleData() != null) {

            	if ( StringUtils.isNotBlank(singleEntity.getEntityLifeCycleData().getLastUpdatedByUri()) )
            		createdBy = getLastUpdatedByEntityId(singleEntity);
            	else
            		createdBy = getCreatedByEntityId(singleEntity);

            	reviewedBy = getReviewedByEntityId(singleEntity);
                singleEntity.addProperty("uuid", singleEntity.getEntityLifeCycleData().getUuid());
            }
            if (createdBy != null) {
                singleEntity.addProperty("createdBy", createdBy);
            }
            if (reviewedBy != null) {
                singleEntity.addProperty("reviewedBy", reviewedBy);
            }
            singleEntity.addProperty("approvalStatus",
                    MakerCheckerWebUtils.getApprovalStatus(singleEntity.getApprovalStatus()));
/*
            if (singleEntity.getEntityLifeCycleData().getSystemModifiableOnly() != null
                    && singleEntity.getEntityLifeCycleData().getSystemModifiableOnly()) {
                singleEntity.addProperty("actions", "");
            }*/
        }
        List<Object> recordCount = baseMasterService.findEntity(entityClass,null,null,queryMap);
        Integer totalRecordCount = baseMasterService.getTotalRecordSize(entityClass, userUri);
        searchRecordMap.put("searchRecordList", searchRecords);
        searchRecordMap.put("searchRecordListSize", recordCount.size());
        searchRecordMap.put("totalRecordListSize", totalRecordCount);

        return searchRecordMap;
    }

	private String getCreatedByEntityId(BaseMasterEntity singleEntity) {
		String createdBy = null;
		try {
			EntityId createorEntityId = singleEntity.getEntityLifeCycleData().getCreatedByEntityId();
			if (createorEntityId != null) {
				createdBy = userService.getUserNameByUserId(createorEntityId.getLocalId());
			}
		} catch (Exception e) {
			createdBy = null;
			BaseLoggers.exceptionLogger.error(ERROR_MSG, e);
		}
		return createdBy;
	}

	private String getLastUpdatedByEntityId(BaseMasterEntity singleEntity) {
		String lastUpdatedBy = null;
		try {
			EntityId lastUpdatedByEntityId = EntityId.fromUri(singleEntity.getEntityLifeCycleData().getLastUpdatedByUri());

			if (lastUpdatedByEntityId != null) {
				lastUpdatedBy = userService.getUserNameByUserId(lastUpdatedByEntityId.getLocalId());
			}
		} catch (Exception e) {
			lastUpdatedBy = null;
			BaseLoggers.exceptionLogger.error(ERROR_MSG, e);
		}
		return lastUpdatedBy;
	}

	private String getReviewedByEntityId(BaseMasterEntity singleEntity) {
		String reviewedBy = null;
		try {
			EntityId revieworEntityId = singleEntity.getMasterLifeCycleData().getReviewedByEntityId();
			if (revieworEntityId != null) {
				reviewedBy = userService.getUserNameByUserId(revieworEntityId.getLocalId());
			}
		} catch (Exception e) {
			reviewedBy = null;
			BaseLoggers.exceptionLogger.error(ERROR_MSG, e);
		}
		return reviewedBy;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> loadPaginatedData(GridVO gridVO, Class entityName, String userUri, Long parentId) {
        Map<String, Object> returnMap = new HashMap<String, Object>();

        Map<String, Object> searchMap = gridVO.getSearchMap();
        boolean isWorkflow = false;
        boolean isDynamicWorkflow = false;
        if("WorkflowConfiguration".equalsIgnoreCase(entityName.getSimpleName())) {
            if(searchMap!= null && !searchMap.isEmpty() && searchMap.containsKey("isDynamicWorkflow")) {
                isWorkflow = true;
                isDynamicWorkflow = Boolean.parseBoolean(searchMap.get("isDynamicWorkflow").toString());
            }
        }

        List<Object> bmeList = baseMasterService.loadPaginatedData(gridVO, entityName, userUri);
        Integer recordCount;
        if(isWorkflow) {
            recordCount = baseMasterService.getWorkflowTotalRecordSize(entityName, userUri, isDynamicWorkflow);
        } else {
            recordCount = baseMasterService.getTotalRecordSize(entityName, userUri);
        }
        gridVO.setiDisplayStart(null);
        gridVO.setiDisplayLength(null);
        Long searchRecordsCount;
        if (ValidatorUtils.hasNoEntry(gridVO.getSearchMap())) {
        	searchRecordsCount = (long) recordCount;
        } else {
        	searchRecordsCount = baseMasterService.getSearchRecordsCount(gridVO, entityName, userUri);
        	if (searchRecordsCount == null) {
            	searchRecordsCount = 0l;
            }
        }
        try {
        	StringBuilder createdByUserFullName = new StringBuilder();
        	StringBuilder reviewedByUserFullName = new StringBuilder();
        	StringBuilder createdBy = new StringBuilder();
        	StringBuilder reviewedBy = new StringBuilder();
        	
            for (Object o : bmeList) {
                BaseMasterEntity singleEntity = (BaseMasterEntity) o;

                if (singleEntity.getEntityLifeCycleData() != null) {
                	EntityId creatorEntityId;

                	if ( StringUtils.isNotBlank(singleEntity.getEntityLifeCycleData().getLastUpdatedByUri()) )
                		creatorEntityId = EntityId.fromUri(singleEntity.getEntityLifeCycleData().getLastUpdatedByUri());
                	else
                		creatorEntityId = singleEntity.getEntityLifeCycleData().getCreatedByEntityId();

                    if (creatorEntityId != null){
                    	createdBy 				= createdBy.append(userService.getUserNameByUserId(creatorEntityId.getLocalId()));
        				createdByUserFullName 	= createdByUserFullName.append(userService.getUserFullNameForUserId(creatorEntityId.getLocalId()));
                    }
                    EntityId revieworEntityId = singleEntity.getMasterLifeCycleData().getReviewedByEntityId();
                    
                    if (revieworEntityId != null){
                    	reviewedBy 				= reviewedBy.append(userService.getUserNameByUserId(revieworEntityId.getLocalId()));
        				reviewedByUserFullName 	= reviewedByUserFullName.append(userService.getUserFullNameForUserId(revieworEntityId.getLocalId()));
                    }
                    singleEntity.addProperty("uuid", singleEntity.getEntityLifeCycleData().getUuid());
                }
                singleEntity.addProperty("createdBy",createdBy.length()>0? createdByUserFullName.toString() + USER_NAME_HTML_TAG_SMALL+ createdBy.toString()+ USER_NAME_HTML_TAG_END_SMALL:"");
				singleEntity.addProperty("createdByUserWithFullName", createdBy.toString() + " [" +createdByUserFullName.toString()+ "]");
				singleEntity.addProperty("reviewedBy",reviewedBy.length()>0?reviewedByUserFullName.toString() + USER_NAME_HTML_TAG_SMALL+reviewedBy.toString()+ USER_NAME_HTML_TAG_END_SMALL:"");
				singleEntity.addProperty("reviewedByUserWithFullName", reviewedBy.toString() + " [" +reviewedByUserFullName.toString()+ "]");
				singleEntity.addProperty("approvalStatus",MakerCheckerWebUtils.getApprovalStatus(singleEntity.getApprovalStatus()));
				
				createdBy.setLength(0);
				reviewedBy.setLength(0);
				createdByUserFullName.setLength(0);
				reviewedByUserFullName.setLength(0);
            }
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error(ERROR_MSG, e);
        }
        returnMap.put("entityList", bmeList);
        returnMap.put("recordCount", searchRecordsCount.intValue());
        returnMap.put("totalRecordCount", recordCount);
        return returnMap;
    }

}
