package com.nucleus.web.security.cors;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;

import com.nucleus.logging.BaseLoggers;

/**
 * 
 * A placeholder to hold Global CORS configuration
 * 
 * @author gajendra.jatav
 *
 */
public class NeutrinoCORSConfig extends CorsConfiguration {

	private String corsAllowedOrigins;

	private String corsAllowedMethods;

	private String corsAllowedHeaders;

	private String corsExposedHeaders;

	private boolean corsAllowCredentials;

	private Long corsMaxAge;

	public static final String ALLOWED_METHODS = "GET,POST";

	public static final Long CORS_MAX_AGE = 3600L;

	@PostConstruct
	public void initCorsConfig() {
		BaseLoggers.flowLogger.info(
				"Configuring below CORS settings\n allowed-origins : {}, allowed-methods: {}, allowed-headers: {}, exposed-headers: {}, allow-credentials: {}, max-age: {}",
				corsAllowedOrigins, corsAllowedMethods, corsAllowedHeaders, corsExposedHeaders, corsAllowCredentials,
				corsMaxAge);
		if(StringUtils.isNotEmpty(corsAllowedOrigins))
		{
			setAllowedOrigins(Arrays.asList(corsAllowedOrigins.split(",")));
		}
		setAllowedMethods(Arrays.asList(corsAllowedMethods.split(",")));
		setAllowedHeaders(Arrays.asList(corsAllowedHeaders.split(",")));
		setExposedHeaders(Arrays.asList(corsExposedHeaders.split(",")));
		setAllowCredentials(corsAllowCredentials);
		setMaxAge(corsMaxAge);
	}

	public String getCorsAllowedOrigins() {
		return corsAllowedOrigins;
	}

	/**
	 * Set the Origins to allow, e.g. http://abc.com,https://xyz.com, etc.
	 * 
	 * @param corsAllowedOrigins
	 */
	@Value("${core.web.config.cors.allowedOrigins}")
	public void setCorsAllowedOrigins(String corsAllowedOrigins) {
		if (StringUtils.isEmpty(corsAllowedOrigins)
				|| "${core.web.config.cors.allowedOrigins}".equalsIgnoreCase(corsAllowedOrigins)) {
			this.corsAllowedOrigins = null;
			return;
		}
		this.corsAllowedOrigins = corsAllowedOrigins;
	}

	public String getCorsAllowedMethods() {
		return corsAllowedMethods;
	}

	/**
	 * Set the HTTP Methods to allow, e.g. GET,POST etc.
	 * 
	 * @param corsAllowedMethods
	 */
	@Value("${core.web.config.cors.allowedMethods}")
	public void setCorsAllowedMethods(String corsAllowedMethods) {
		if (StringUtils.isEmpty(corsAllowedMethods)
				|| "${core.web.config.cors.allowedMethods}".equalsIgnoreCase(corsAllowedMethods)) {
			this.corsAllowedMethods = ALLOWED_METHODS;
			return;
		}

		this.corsAllowedMethods = corsAllowedMethods;
	}

	public String getCorsAllowedHeaders() {
		return corsAllowedHeaders;
	}

	/**
	 * Set the Custom headers to allow for configured origins, e.g.
	 * X-CUSTOM-HEADER,Y-CUSTOM-HEADER etc.
	 * 
	 * @param corsAllowedHeaders
	 */
	@Value("${core.web.config.cors.allowedHeaders}")
	public void setCorsAllowedHeaders(String corsAllowedHeaders) {
		if (StringUtils.isEmpty(corsAllowedHeaders)
				|| "${core.web.config.cors.allowedHeaders}".equalsIgnoreCase(corsAllowedHeaders)) {
			this.corsAllowedHeaders = "";
			return;
		}

		this.corsAllowedHeaders = corsAllowedHeaders;
	}

	public String getCorsExposedHeaders() {
		return corsExposedHeaders;
	}

	/**
	 * Set the Custom headers to allow in response from server for configured
	 * origins, e.g. X-RESPONSE-CUSTOM-HEADER,Y-RESPONSE-CUSTOM-HEADER etc.
	 * 
	 * @param corsExposedHeaders
	 */
	@Value("${core.web.config.cors.exposedHeaders}")
	public void setCorsExposedHeaders(String corsExposedHeaders) {

		if (StringUtils.isEmpty(corsExposedHeaders)
				|| "${core.web.config.cors.exposedHeaders}".equalsIgnoreCase(corsExposedHeaders)) {
			this.corsExposedHeaders = "";
			return;
		}

		this.corsExposedHeaders = corsExposedHeaders;
	}

	public boolean isCorsAllowCredentials() {
		return corsAllowCredentials;
	}

	/**
	 * Configuration to allow if the request can be made with/will include
	 * credentials such as Cookies
	 * 
	 * @param corsAllowCredentials
	 */
	@Value("${core.web.config.cors.allowCredentials}")
	public void setCorsAllowCredentials(String corsAllowCredentials) {
		if (StringUtils.isEmpty(corsAllowCredentials)
				|| "${core.web.config.cors.allowCredentials}".equalsIgnoreCase(corsAllowCredentials)) {
			this.corsAllowCredentials = false;
			return;
		}

		this.corsAllowCredentials = Boolean.parseBoolean(corsAllowCredentials);
	}

	public Long getCorsMaxAge() {
		return corsMaxAge;
	}

	/**
	 * Value in seconds to cache preflight request results
	 * 
	 * @param corsMaxAge
	 */
	@Value("${core.web.config.cors.maxAge}")
	public void setCorsMaxAge(String corsMaxAge) {

		if (corsMaxAge instanceof String && "${core.web.config.cors.maxAge}".equalsIgnoreCase(corsMaxAge)) {
			this.corsMaxAge = CORS_MAX_AGE;
			return;
		}

		this.corsMaxAge = Long.parseLong(corsMaxAge);
	}

}
