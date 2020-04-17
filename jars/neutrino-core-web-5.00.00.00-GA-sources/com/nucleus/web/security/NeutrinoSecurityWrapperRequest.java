package com.nucleus.web.security;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.security.PatternConfig;
import com.nucleus.web.common.CommonConfigUtility;

public class NeutrinoSecurityWrapperRequest extends HttpServletRequestWrapper implements HttpServletRequest {
	 
   private Map<String, Map<String, Object>> exludedParameters;
   private List<PatternConfig> paramPatterns;
   private List<PatternConfig> headerAndParamPatterns;
   private StandardPBEStringEncryptor encryptor; 
   
   private  Set<String> sanitizedHeaderValueCacheSet = new HashSet<String>();
	
   public NeutrinoSecurityWrapperRequest(HttpServletRequest request) {
       super(request);
   }
   
   public NeutrinoSecurityWrapperRequest(HttpServletRequest request, Map<String, Map<String, Object>> exludedParameters, List<PatternConfig> paramPatterns,
		   								List<PatternConfig> headerAndParamPatterns, StandardPBEStringEncryptor encryptor) {
       super(request);
	   this.exludedParameters = exludedParameters;
	   this.paramPatterns=paramPatterns;
	   this.headerAndParamPatterns = headerAndParamPatterns;
	   this.encryptor = encryptor;
   }
   
   private HttpServletRequest getHttpServletRequest() {
       return (HttpServletRequest) super.getRequest();
   }
   
   public Map getParameterMap() {
	   Map<String, String[]> map = getHttpServletRequest().getParameterMap();
   	   return NeutrinoSecurityUtility.getParameterMap(map, exludedParameters, paramPatterns, headerAndParamPatterns, encryptor);
   }

   public String getQueryString() {
       String query = getHttpServletRequest().getQueryString();
       return NeutrinoSecurityUtility.getQueryString(query, encryptor);
   }

   public String getParameter(String name) {
	   String orig = getHttpServletRequest().getParameter(name);
	   return NeutrinoSecurityUtility.getParameter(orig, name, exludedParameters, paramPatterns, headerAndParamPatterns, encryptor);
   }

   public String[] getParameterValues(String name) {
       String[] values = getHttpServletRequest().getParameterValues(name);
       return NeutrinoSecurityUtility.getParameterValues(values, name, exludedParameters, paramPatterns, headerAndParamPatterns, encryptor);
   }

   public String getHeader(String name) {

	 
		String value = super.getHeader(name);

		if(sanitizedHeaderValueCacheSet.contains(value)){
			   return value;
		}
		if (value != null&& NeutrinoSpringAppContextUtil.getBeanByName("commonConfigUtility",CommonConfigUtility.class).isSanitizingEnabled()) {
			
			NeutrinoSecurityUtility.checkSanity(value, name, headerAndParamPatterns, exludedParameters);
			
				sanitizedHeaderValueCacheSet.add(value);
			
		}
	
		return value;
	}
}