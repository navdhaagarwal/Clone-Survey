package com.nucleus.web.security;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.logging.BaseLoggers;

public final class NeutrinoCORSFilter implements Filter {

    private boolean anyOriginAllowed;
    
    private List<String> allowedOrigins;
    
    private String allowedOriginsString;

    public static final String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    public static final String REQUEST_HEADER_HOST = "Host";
    
    public static final String REQUEST_HEADER_REFERER = "Referer";

    public static final String DEFAULT_ALLOWED_ORIGINS = "*";
    
    public static final String ORIGINS_DELIMITER = ",";

    public static final String PARAM_CORS_ALLOWED_ORIGINS = "cors.allowed.origins";

    public NeutrinoCORSFilter() {
        this.allowedOrigins = new ArrayList<>();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    	HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String host = httpRequest.getHeader(REQUEST_HEADER_HOST);
        String referer = httpRequest.getHeader(REQUEST_HEADER_REFERER);
        		
        if (!isOriginAllowed(host, referer)) {
        	this.handleInvalidCORS(httpRequest, httpResponse);
        } else {
        	httpResponse.addHeader(RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, allowedOriginsString);
        	chain.doFilter(request, response);
        }
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        if (filterConfig != null) {
            String configAllowedOrigins = filterConfig.getInitParameter(PARAM_CORS_ALLOWED_ORIGINS);
            parseAndStore(configAllowedOrigins);
        }
    }

    private void handleInvalidCORS(HttpServletRequest request,
             HttpServletResponse response) {
        String host = request.getHeader(NeutrinoCORSFilter.REQUEST_HEADER_HOST);
        String referer = request.getHeader(REQUEST_HEADER_REFERER);

        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.resetBuffer();

        if (BaseLoggers.exceptionLogger.isDebugEnabled()) {
            BaseLoggers.exceptionLogger.debug("CORS Exception in NeutrinoCORSFilter. Invalid CORS request; Host="+host+" Referer="+referer);
        }
    }

    @Override
    public void destroy() {
        // NOOP
    }

    private boolean isOriginAllowed(String host, String referer) {
    	boolean hostAllowed=false; 
    	boolean refererAllowed = false;
        if (anyOriginAllowed) {
            return true;
        }
        if ( StringUtils.isBlank(host) ) {
        	return false;
        }
        hostAllowed = allowedOrigins.contains(parseHost(host));
        if ( StringUtils.isNotBlank(referer) ) {
        	String parsedHostFromReferer = parseHostFromReferer(referer);
        	refererAllowed = allowedOrigins.contains(parsedHostFromReferer);
        }
        else
        {
        	refererAllowed=true;
        }
        return hostAllowed && refererAllowed;
    }

    private String parseHostFromReferer(String url) {
    	URL refererUrl;
    	String host;
    	try {
    		refererUrl = new URL(url);
    		host = refererUrl.getHost().toLowerCase();
    	} catch (MalformedURLException ex) {
    		host = parseHostFromString(url);
    		BaseLoggers.exceptionLogger.debug("CORS Exception in NeutrinoCORSFilter. Invalid Referer URL; Referer="+url);
    	}
    	return host;
    }
    
    private String parseHostFromString(String urlString) {
    	String host = urlString;
   		int startIndex = host.indexOf('/');
   		host = host.substring(startIndex + 2);
   		int endIndex = host.indexOf(':');

   		return host.substring(0 , endIndex).toLowerCase();
    }

    private String parseHost(String urlHost) {
    	String host = urlHost;
    	
		int endIndex = host.indexOf(':');
    	if(endIndex != -1){       		
       		return host.substring(0 , endIndex).toLowerCase();
       	}else{
       		return  host;
       	}
    }
    
    private void parseAndStore(String allowedOrigins)
                     {
        if (allowedOrigins != null) {
            if (DEFAULT_ALLOWED_ORIGINS.equals(allowedOrigins.trim())) {
                this.anyOriginAllowed = true;
                this.allowedOriginsString = DEFAULT_ALLOWED_ORIGINS;
            } else {
                this.anyOriginAllowed = false;
                List<String> listAllowedOrigins = parseStringToList(allowedOrigins);
                this.allowedOriginsString = allowedOrigins.toLowerCase();
                this.allowedOrigins.clear();
                this.allowedOrigins.addAll(listAllowedOrigins);
            }
        }
    }

    private List<String> parseStringToList(String data) {
        String[] splits;

        if (data != null && data.length() > 0) {
            splits = data.split(ORIGINS_DELIMITER);
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
