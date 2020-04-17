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
package com.nucleus.core.searchframework.service;

import java.util.List;

import javax.inject.Named;

import com.nucleus.core.searchframework.entity.SearchRequest;
import com.nucleus.service.BaseServiceImpl;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Named(value = "searchFrameworkService")
public class SearchFrameworkServiceImpl extends BaseServiceImpl implements SearchFrameworkService {

    private static final String JPA_CRITERIA_SEARCH = "JPA_CRITERIA_SEARCH";

    /**
     * Method to create query,accept SearchRequest Object and 
     * create query with criteria object builder with predicate
     */
    @SuppressWarnings("unchecked")
    @Override
    public List executeSearchRequest(SearchRequest request) {
        if (request.getSearchType().equals(JPA_CRITERIA_SEARCH)) {
            return entityDao.executeQuery(new SearchRequestExecutor(request));
        } else {
            throw new SearchException("Search type not supported");
        }
    }

	@Override
	public Object customizeSearchResult(Object result) {
		
		return null;
	}
}
