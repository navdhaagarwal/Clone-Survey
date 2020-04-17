package com.nucleus.master;

import java.util.List;
import java.util.Map;

import com.nucleus.makerchecker.ColumnConfiguration;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.makerchecker.MasterConfigurationRegistry;

public interface ChildMasterService {

	boolean isMatchesSearchCriteria(String searchTerm, Object columnValue);
	
	Map<String, Object> createDataTableFromEntityMapper(MasterConfigurationRegistry masterConfigurationRegistry, String childKey);
	
	Class<?> getEntityClass(String key);
	
	/**
	 * To load merged child grid data. It will return updated list of child grid where modified
	 * record will be preferred over existing record.
	 * 
	 * @param parentKey
	 * @param childKey
	 * @param userUri
	 * @param id
	 * @param gridVO
	 * @param columnConfigurationList
	 * @return
	 * @throws Exception
	 */
	<T extends BaseMasterEntity> Map<String, Object> getChildGridData(String parentKey, String childKey, String userUri,
			Long id, GridVO gridVO, List<ColumnConfiguration> columnConfigurationList) throws Exception;
	
	/**
	 * A generic method to return count of all records.
	 * This count will include the child records from original record also
	 * the modified/added/deleted records.
	 * 
	 * Method returns null in case there is no parent entity for particular id. 
	 * 
	 * @param childAttributeName
	 * @param parentEntityClass
	 * @param id
	 * @return
	 */
	<T extends BaseMasterEntity> Integer getTotalRecordSizeByParentId(String childAttributeName, Class<T> parentEntityClass, Long id);
	
	/**
	 * A generic method to return count of all records.
	 * This count will include the child records from original record also
	 * the modified/added/deleted records.
	 * 
	 * Method returns null in case there is no parent entity for particular id.
	 * 
	 * @param childAttributeName
	 * @param parentEntityClass
	 * @param id
	 * @return
	 */
	Integer getTotalRecordSizeByParentEntity(String childAttributeName, Class<?> parentEntityClass, BaseMasterEntity parentEntity);
}
