package com.nucleus.web.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.util.CustomMatcherGenericFilter;

/**
 * 
 * @author gajendra.jatav
 *
 */

public class NeutrinoHostHeaderFilter extends CustomMatcherGenericFilter{

	private boolean anyHostAllowed;

	private List<String> allowedHosts;

	public static final String REQUEST_HEADER_HOST = "Host";

	public static final String ALLOW_ALL_HOSTS = "*";

	public static final String HOSTS_DELIMITER = ",";

	private String configuredAllowedHosts;

	public NeutrinoHostHeaderFilter() {
		this.allowedHosts = new ArrayList<>();
	}

	@Override
	public void filterInternal(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		if(isValidateHost(request)){
			chain.doFilter(request, response);
		}else{
			this.handleInvalidHost((HttpServletRequest) request, (HttpServletResponse) response);
		}
	}

	
	public boolean isValidateHost(ServletRequest request) {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String host = httpRequest.getHeader(REQUEST_HEADER_HOST);
		if (isHostAllowed(host)) {
			return true;
		}
		return false;
	}	
	
	public String getConfiguredAllowedHosts() {
		return configuredAllowedHosts;
	}

	@Value(value = "#{'${security.allowed.hosts}'}")
	public void setConfiguredAllowedHosts(String configuredAllowedHosts) {
		this.configuredAllowedHosts = configuredAllowedHosts;

		if (StringUtils.isEmpty(configuredAllowedHosts)
				|| "${security.allowed.hosts}".equalsIgnoreCase(configuredAllowedHosts)) {
			this.configuredAllowedHosts ="*";
			return;
		}
		this.configuredAllowedHosts = configuredAllowedHosts;
	}
	
	@PostConstruct
	public void init() {
			parseAndStore(this.configuredAllowedHosts);
	}

	private void handleInvalidHost(HttpServletRequest request, HttpServletResponse response) {
		String host = request.getHeader(REQUEST_HEADER_HOST);
		response.setContentType("text/plain");
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.resetBuffer();
		BaseLoggers.exceptionLogger.debug(
				"Invalid Host found Exception in NeutrinoHostHeaderFilter. Invalid Host request; Host= {}", host);
	}

	private boolean isHostAllowed(String urlHost) {
		if (anyHostAllowed) {
			return true;
		}
		if (StringUtils.isBlank(urlHost)) {
			BaseLoggers.flowLogger.error("Host Header found Null or Empity - {}",urlHost);
			return false;
		}
		return allowedHosts.contains(parseHost(urlHost).toLowerCase());
	}

	private String parseHost(String urlHost) {
		String host = urlHost;

		int endIndex = host.indexOf(':');
		if (endIndex != -1) {
			return host.substring(0, endIndex).toLowerCase();
		} else {
			return host;
		}
	}

	private void parseAndStore(String allowedHosts) {
		if (ALLOW_ALL_HOSTS.equals(allowedHosts.trim())) {
			this.anyHostAllowed = true;
		} else {
			this.anyHostAllowed = false;
			List<String> listAllowedHosts = parseStringToList(allowedHosts);
			this.allowedHosts.clear();
			this.allowedHosts.addAll(listAllowedHosts);
		}
	}

	private List<String> parseStringToList(String data) {
		String[] splits;

		if (!StringUtils.isBlank(data)) {
			splits = data.split(HOSTS_DELIMITER);
		} else {
			splits = new String[] {};
		}

		List<String> list = new ArrayList<>();
		if (splits.length > 0) {
			for (String split : splits) {
				list.add(split.trim().toLowerCase());
			}
		}
		return list;
	}


}
