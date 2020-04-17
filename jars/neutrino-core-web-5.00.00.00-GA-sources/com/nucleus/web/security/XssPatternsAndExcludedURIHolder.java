package com.nucleus.web.security;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import com.nucleus.core.security.BlackListPatternHolder;
import com.nucleus.core.security.PatternConfig;

public class XssPatternsAndExcludedURIHolder {
	
	private XssPatternsAndExcludedURIHolder(){}
	
    private static Map<String, Map<String, Map<String, Object>>> unfilteredURLParameterMap;
    private static StandardPBEStringEncryptor encryptor;

  
	
    public static Map<String, Map<String, Map<String, Object>>> getUnfilteredURLParameterMap() {
		return unfilteredURLParameterMap;
	}

	public static void setUnfilteredURLParameterMap(
			Map<String, Map<String, Map<String, Object>>> unfilteredURLParameterMap) {
		XssPatternsAndExcludedURIHolder.unfilteredURLParameterMap = unfilteredURLParameterMap;
	}

	public static StandardPBEStringEncryptor getEncryptor() {
		return encryptor;
	}

	public static void setEncryptor(StandardPBEStringEncryptor encryptor) {
		XssPatternsAndExcludedURIHolder.encryptor = encryptor;
	}

    public static List<PatternConfig> getParamPatterns() {
    	return BlackListPatternHolder.paramPatterns;
    }
    
    public static void addParamPatterns(List<PatternConfig> paramPatternsList) {
    	BlackListPatternHolder.paramPatterns.addAll(paramPatternsList);
    }
    
    public static List<PatternConfig> getHeaderAndParamPatterns() {
    	return BlackListPatternHolder.headerAndParamPatterns;
    }
    
    public static void addHeaderAndParamPatterns(List<PatternConfig> headerAndParamPatternsList) {
    	BlackListPatternHolder.headerAndParamPatterns.addAll(headerAndParamPatternsList);
    }

    public static Map<String, Map<String, Object>> getExludedParameters(HttpServletRequest hrequest) {

    	Set<String> exludedUris=null;
        if (unfilteredURLParameterMap != null && !unfilteredURLParameterMap.isEmpty()) {
            exludedUris = unfilteredURLParameterMap.keySet();
        }

        Map<String, Map<String, Object>> exludedParameters = null;
        String requestUri = hrequest.getRequestURI();
        if (exludedUris != null && !exludedUris.isEmpty()) {
            for (String unfilteredUri : exludedUris) {
                if (requestUri.contains(unfilteredUri)) {
                    exludedParameters = unfilteredURLParameterMap.get(unfilteredUri);
                    break;
                }
            }
        }
        return exludedParameters;
    }
}
