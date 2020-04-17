package com.nucleus.persistence;

import java.util.List;

import com.nucleus.makerchecker.GridVO;
import com.nucleus.master.BaseMasterEntity;

public interface ChildMasterDao extends EntityDao {

	/**
	 * This method is used to get child data based on parent entity.
	 * This method itself decide to load all data or paginated data based on GridVO.
	 * 
	 * @param childAttributeName
	 * @param entityClass
	 * @param gridVO
	 * @param parentEntity
	 * @return
	 */
	<T extends BaseMasterEntity> List<T> loadPaginatedData(String childAttributeName, Class<?> entityClass, GridVO gridVO,
			T parentEntity);

	/**
	 * Method for returning total merged record size for the grid.
	 * 
	 * @param childAttributeName
	 * @param entityClass
	 * @param parentEntity
	 * @return
	 */
	Integer getTotalRecordSize(String childAttributeName, Class<?> entityClass, BaseMasterEntity parentEntity);
	
	/**
	 * Method for returning total searched record size for the grid.
	 * The searched record will be filtered based on <code>GridVo</code> data.
	 * 
	 * @param childAttributeName
	 * @param entityClass
	 * @param parentEntity
	 * @return
	 */
	Integer getSearchRecordsCount(String childAttributeName, Class<?> entityClass, GridVO gridVO, BaseMasterEntity parentEntity);

}
