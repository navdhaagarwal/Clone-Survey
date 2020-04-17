package com.nucleus.web.security.cors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.logging.BaseLoggers;

/**
 * For adding custom config for particular url mapping other then /**
 * @author gajendra.jatav
 *
 */
public class NeutrinoCorsConfigExtension {

	@Inject
	@Named("neutrinoCorsConfigurationSource")
	private UrlBasedCorsConfigurationSource corsConfigurationSource;
	
	private String path;
	
	private CorsConfiguration corsConfiguration;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public CorsConfiguration getCorsConfiguration() {
		return corsConfiguration;
	}

	public void setCorsConfiguration(CorsConfiguration corsConfiguration) {
		this.corsConfiguration = corsConfiguration;
	}
	
	@PostConstruct
	public void registerCorsConfig(){
		if("/**".equals(path)){
			BaseLoggers.flowLogger.error("Overriding conrsConfig for /** is not allowed");
			throw new SystemException("Overriding conrsConfig for /** is not allowed");
		}
		corsConfigurationSource.registerCorsConfiguration(path, corsConfiguration);
	}

}
