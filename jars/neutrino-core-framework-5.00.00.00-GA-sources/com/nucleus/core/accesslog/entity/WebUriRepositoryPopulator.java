package com.nucleus.core.accesslog.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.persistence.EntityDao;

@Named("webUriRepositoryPopulator")
public class WebUriRepositoryPopulator {

	private HashMap<String, Object> webUriRepositoryMap = new HashMap<>();

	@Inject
	@Named("dynamicURILogicalNameMapper")
	private DynamicURILogicalNameMapper  dynamicURILogicalNameMapper;
		
	@Inject
	@Named("entityDao")
	private EntityDao entityDao;
	
	public HashMap<String, Object> getWebUriRepositoryMap() {
		return webUriRepositoryMap;
	}

	@PostConstruct
	public void populateWebUriRepository(){
		NamedQueryExecutor<Map<String, Object>> queryExecutor = new NamedQueryExecutor<Map<String, Object>>(
                "WebURIRepository.getWebUriRepositoryMap");
        List<Map<String, Object>> uriRepoMapList = entityDao.executeQuery(queryExecutor);
    	for(Map<String, Object> map : uriRepoMapList) {     
    		String uri  = String.valueOf(map.get("uri"));
    		if(StringUtils.contains(uri, DynamicURILogicalNameMapper.DYNAMIC_URI_TOKEN)) {
    			dynamicURILogicalNameMapper.insertUriNode(String.valueOf(map.get("uri")), String.valueOf(map.get("id")));
    		}else {
        		webUriRepositoryMap.put(String.valueOf(map.get("uri")),Long.valueOf(String.valueOf(map.get("id"))));
    		}
    	}
	}
	
}
