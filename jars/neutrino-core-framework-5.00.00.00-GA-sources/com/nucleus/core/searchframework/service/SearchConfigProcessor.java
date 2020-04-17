package com.nucleus.core.searchframework.service;

import java.util.List;

import com.nucleus.core.searchframework.entity.SearchAttributeBean;
import com.nucleus.core.searchframework.entity.SearchRequest;

public interface SearchConfigProcessor {

    /**
     * Method To read config file and
     * paint UI 
     * @param inputStream
     * @param searchCOnfigId
     * @return
     */
    public List<SearchAttributeBean> prepareSearchAttributesForSearchConfiguration(String searchConfigId);

    /**
     * Method To Get Request Data from UI and Make query
     * @param inputStream
     * @param list
     * @param selectFieldList
     * @return
     */
    public SearchRequest getSearchRequestData(List<SearchAttributeBean> list, String searchRequestEntityID);

}
