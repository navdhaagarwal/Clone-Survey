/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 */
package com.nucleus.core.messageSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.FlushModeType;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.grid.IGridService;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Named("messageResourceGridService")
public class MessageResourceGridServiceImpl extends BaseServiceImpl implements IGridService {
	
    @Inject
    @Named("messageResourceService")
    MessageResourceService messageResourceService;

    /**
     * Method is used to populate all labes with server side pagination supported
     */
    @Override
    public Map<String, Object> loadPaginatedData(Class entityName, String userUri, Long parentId, Integer iDisplayStart,
            Integer iDisplayLength, String sortColName, String sortDir) {
        EntityId userEntityId = EntityId.fromUri(userUri);
        if (!(getCurrentUser().getId().equals(userEntityId.getLocalId()))) {
            throw new SystemException("The user id :" + userEntityId.getLocalId()
                    + " requesting all entities. It does not match with logged in user's ID :" + getCurrentUser().getId());
        }
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT mr FROM MessageResource mr ");
        if (sortColName != null && sortDir != null ) {
        	queryBuilder.append("ORDER BY LOWER(mr.")
        				.append(sortColName + ")")
        				.append(" ")
        				.append(sortDir);
        }
        JPAQueryExecutor<MessageResource> jPAQueryExecutor = new JPAQueryExecutor<>(queryBuilder.toString());
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
        List<MessageResource> messages = entityDao.executeQuery(jPAQueryExecutor, iDisplayStart, iDisplayLength);
        Long countOfMessages = messageResourceService.getCountOfAllMessage();
        Map<String, Object> result = new HashMap<>();
        result.put("entity", messages);
        result.put("count", countOfMessages);
        return result;
    }

    @Override
    public Map<String, Object> findEntity(Class entityClass, String userUri,Integer iDisplayStart,
            Integer iDisplayLength, Map<String, Object> queryMap) {
        return null;
    }

    /**
     * Method used while searching in datatable
     * @param queryMap
     * @param iDisplayStart
     * @param iDisplayLength
     * @return
     */
    @Deprecated
    public Map<String, Object> findEntity(Map<String, Object> queryMap, Integer iDisplayStart, Integer iDisplayLength) {
        Map<String, Object> searchRecordMap = new HashMap<>();
        List<MessageResource> searchRecords = messageResourceService.findEntity(queryMap, iDisplayStart, iDisplayLength);
        Integer recordCount = searchRecords.size();
        renderAdditionalPropertyToMessageResource(searchRecords);	//adding uuid and actions.
        Integer totalRecordCount = Integer.parseInt(messageResourceService.getCountOfAllMessage().toString());
        searchRecordMap.put("searchRecordList", searchRecords);
        searchRecordMap.put("searchRecordListSize", recordCount);
        searchRecordMap.put("totalRecordListSize", totalRecordCount);
        return searchRecordMap;
    }
    
    private void renderAdditionalPropertyToMessageResource(
			List<com.nucleus.core.messageSource.MessageResource> searchRecords) {
    	for (MessageResource bm : searchRecords) {
            MessageResource singleEntity = bm;
            if (singleEntity.getEntityLifeCycleData() != null) {
                singleEntity.addProperty("uuid", singleEntity.getEntityLifeCycleData().getUuid());
            }
            if (singleEntity.getEntityLifeCycleData().getSystemModifiableOnly() != null
                    && singleEntity.getEntityLifeCycleData().getSystemModifiableOnly()) {
                singleEntity.addProperty("actions", "");
            }
        }
	}

	/**
     * Method used while searching in data table.
     * @param queryMap
     * @param iDisplayStart
     * @param iDisplayLength
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> findEntity(Map<String, Object> queryMap, Integer iDisplayStart, Integer iDisplayLength, String sortColName, String sortDir) {
        Map<String, Object> searchRecordMap = new HashMap<>();
    	StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT mr FROM MessageResource mr ")
        			.append(getSearchClause(queryMap));
        if (sortColName != null && sortDir != null ) {
        	queryBuilder.append("ORDER BY LOWER(mr.")
        				.append(sortColName + ")")
        				.append(" ")
        				.append(sortDir);
        }						
		JPAQueryExecutor<MessageResource> jPAQueryExecutor = new JPAQueryExecutor<>(queryBuilder.toString());
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        jPAQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_FLUSHMODE, FlushModeType.COMMIT);
        List<MessageResource> searchRecords = entityDao.executeQuery(jPAQueryExecutor, iDisplayStart, iDisplayLength);
        renderAdditionalPropertyToMessageResource(searchRecords);		//adding uuid and actions.
        Integer recordCount = searchRecords.size();
        Integer totalRecordCount = Integer.parseInt(messageResourceService.getCountOfAllMessage().toString());
		searchRecordMap.put("searchRecordList", searchRecords);
        searchRecordMap.put("searchRecordListSize", recordCount);
        searchRecordMap.put("totalRecordListSize", totalRecordCount);
        return searchRecordMap;
    }
    
    private String getSearchClause(Map<String, Object> queryMap) {
    	if (ValidatorUtils.hasNoEntry(queryMap)) {
    		return "";
    	}
    	StringBuilder queryBuilder = new StringBuilder();
    	queryBuilder.append(" WHERE ");
    	for (Map.Entry<String, Object> searchEntry : queryMap.entrySet()) {
    		queryBuilder.append("LOWER(mr.")
    					.append(searchEntry.getKey()+")")
    					.append(" LIKE ")
    					.append("LOWER('%"+searchEntry.getValue()+"%') ")
    					.append(" OR ");
    	}
    	queryBuilder.setLength(queryBuilder.length() - " OR ".length());
		return queryBuilder.toString();
	}

	@Override
    public Map<String, Object> loadPaginatedData(GridVO gridVO,
            Class entityName, String userUri, Long parentId) {
    	return this.loadPaginatedData(entityName, userUri, parentId,
				gridVO.getiDisplayStart(), gridVO.getiDisplayLength(),
				gridVO.getSortColName(), gridVO.getSortDir());
    }
}
