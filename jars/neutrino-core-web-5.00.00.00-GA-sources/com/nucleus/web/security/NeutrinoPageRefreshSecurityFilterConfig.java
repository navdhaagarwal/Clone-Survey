package com.nucleus.web.security;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

@Named
public class NeutrinoPageRefreshSecurityFilterConfig {
	
	@Value(value = "#{'${security.pageRefreshFilterEnabled}'}")
	private String pageRefreshFilterEnabled;

	@Value(value = "#{'${security.accessed.url.queue.size}'}")
	private String urlToTimeStampMapSize;
	
	/**default value of urlToTimeStampMapSize*/
	public static final Integer DEFAULT_MAP_SIZE=500;
	
	
	public Boolean getPageRefreshFilterEnabled() {
		if (pageRefreshFilterEnabled instanceof String && "${security.pageRefreshFilterEnabled}".equalsIgnoreCase(pageRefreshFilterEnabled)) {
			return false;
		} else {
			return Boolean.valueOf(pageRefreshFilterEnabled);
		}
	}

	public Integer getUrlToTimeStampMapSize() {
		if(urlToTimeStampMapSize instanceof String && "${security.accessed.url.queue.size}".equalsIgnoreCase(urlToTimeStampMapSize)){
			// if the map size property is not defined, default value is set
			return DEFAULT_MAP_SIZE;
		} else {
			return Integer.valueOf(urlToTimeStampMapSize);
		}
	}

}
