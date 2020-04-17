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
package com.nucleus.grid;

import java.util.Map;

import com.nucleus.makerchecker.GridVO;

public interface IGridService {

	Map<String, Object> loadPaginatedData(Class entityName, String userUri,
			Long parentId, Integer iDisplayStart, Integer iDisplayLength,
			String sortColName, String sortDir);

	Map<String, Object> findEntity(Class entityClass, String userUri,
			Integer iDisplayStart, Integer iDisplayLength,
			Map<String, Object> queryMap);
	
	/**
	 * Method used for searching, sorting and load data
	 * @param gridVO
	 * @param entityName
	 * @param userUri
	 * @return
	 */
	Map<String, Object> loadPaginatedData(GridVO gridVO, Class entityName, String userUri, Long parentId);

}
