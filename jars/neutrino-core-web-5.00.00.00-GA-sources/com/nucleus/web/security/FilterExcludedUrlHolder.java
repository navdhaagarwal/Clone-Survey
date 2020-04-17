package com.nucleus.web.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.HashedMap;

import com.nucleus.finnone.pro.general.util.ValidatorUtils;

/**
 * Enables to configure urls to be excluded from filters which extends NeutrinoUrlExcludableFilter
 * 
 * @author gajendra.jatav
 *
 */
public class FilterExcludedUrlHolder {
	
	private Map<String, Set<String>> excludedUrlMap=new HashedMap();
	
	private List<Map<String, Set<String>>> excludedUrlList=new ArrayList<>();

	private Set<String> excludeForAllFiltersList=new HashSet<>();
	
	public static final String EXCLUDEFORALLFILTERS="excludeForAllFilters";
	
	
	public Set<String> getExcludeForAllFiltersList() {
		return excludeForAllFiltersList;
	}

	public List<Map<String, Set<String>>> getExcludedUrlList() {
		return excludedUrlList;
	}

	public void setExcludedUrlList(List<Map<String, Set<String>>> excludedUrlList) {
		
		if(ValidatorUtils.hasElements(excludedUrlList))
		{
			for(Map<String, Set<String>> map:excludedUrlList)
			{
				this.setExcludedUrlMap(map);
			}
		}
		this.excludedUrlList = excludedUrlList;
	}

	public Map<String, Set<String>> getExcludedUrlMap() {
		return excludedUrlMap;
	}

	public void setExcludedUrlMap(Map<String, Set<String>> excludedUrlMap) {
		if(ValidatorUtils.hasAnyEntry(excludedUrlMap))
		{
			for(Map.Entry<String, Set<String>> entry:excludedUrlMap.entrySet())
			{
				if(ValidatorUtils.hasElements(entry.getValue()))
				{
					addToExcludedUrlMap(entry.getKey(),entry.getValue());
				}
			}
		}
	}

	private void addToExcludedUrlMap(String key, Set<String> value) {

		if(key!=null && EXCLUDEFORALLFILTERS.equals(key))
		{
			excludeForAllFiltersList.addAll(value);
		}
		Set<String> excludedUrls=this.excludedUrlMap.get(key);
		if(excludedUrls==null)
		{
			excludedUrls=new HashSet<>();
			this.excludedUrlMap.put(key, excludedUrls);
		}
		excludedUrls.addAll(value);
	}

	

}
