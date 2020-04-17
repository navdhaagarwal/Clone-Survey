/**
 * 
 */
package com.nucleus.web.useradministration;

import java.util.List;
import java.util.Map;

import com.nucleus.makerchecker.GridVO;
import com.nucleus.master.BaseMasterEntity;


public interface UserGridService {

	Map<String, Object> loadPaginatedData(Class entityName, String userUri,
			Long parentId, Integer iDisplayStart, Integer iDisplayLength,
			String sortColName, String sortDir, boolean filteredStatus);

	Map<String, Object> findEntity(Class entityClass, String userUri,
			Integer iDisplayStart, Integer iDisplayLength,
			Map<String, Object> queryMap);

	void updateUserActions(BaseMasterEntity singleEntity,
			List<Object> loggedInUserInfoList);

	<T extends BaseMasterEntity> List<Object> getAllLoggedInUsers();

	<T extends BaseMasterEntity> boolean loggedInUserisAnAuthorizedMakerForEntity(Class<T> entityClass);

	<T extends BaseMasterEntity> boolean loggedInUserisAnAuthorizedCheckerForEntity(Class<T> entityClass);

	void setNonWorkflowEntityAction(BaseMasterEntity bma,
			Boolean isAuthorozedMakerForEntity);

	<T extends BaseMasterEntity> List<String> getApplicableAssigneeListUri(
			boolean isAuthorizedMakerForEntity,
			boolean isAuthorizedCheckerForEntity, Class<T> entityClass,
			String userUri);

	/**
	 * 
	 * Method used for searching, sorting and load data
	 * @param entityName
	 * @param userUri
	 * @param parentId
	 * @param iDisplayStart
	 * @param iDisplayLength
	 * @param sortColName
	 * @param sortDir
	 * @param filteredStatus
	 * @param searchMap
	 * @return
	 */
	
	Map<String, Object> loadPaginatedData(GridVO gridVO, Class entityName, String userUri,
			Long parentId, boolean filteredStatus);

}
