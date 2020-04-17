package com.nucleus.core.searchframework.service;

import java.util.List;

import com.nucleus.core.searchframework.entity.SearchRequest;
import com.nucleus.service.BaseService;

public interface SearchFrameworkService extends BaseService {

    /**
     * @param em
     * @param searchQuery
     * @return
     * @throws ClassNotFoundException
     */
    public List executeSearchRequest(SearchRequest searchQuery);
    
    public Object customizeSearchResult(Object result);

}
