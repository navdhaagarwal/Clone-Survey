package com.nucleus.web.security;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.security.PatternConfig;
import com.nucleus.web.common.CommonConfigUtility;

public class NeutrinoMultipartHttpServletRequest extends DefaultMultipartHttpServletRequest {
	 
   private Map<String, Map<String , Object>> exludedParameters;
   private List<PatternConfig> paramPatterns;
   private List<PatternConfig> headerAndParamPatterns;
   private StandardPBEStringEncryptor encryptor;
   
   public NeutrinoMultipartHttpServletRequest(HttpServletRequest request) {
       super(request);
   }

   public NeutrinoMultipartHttpServletRequest(HttpServletRequest request, MultiValueMap<String, MultipartFile> mpFiles,
			Map<String, String[]> mpParams, Map<String, String> mpParamContentTypes, Map<String, Map<String, Object>> exludedParameters,
			List<PatternConfig> paramPatterns, List<PatternConfig> headerAndParamPatterns, StandardPBEStringEncryptor encryptor) {
	   super(request);
	   setMultipartFiles(mpFiles);
	   setMultipartParameters(mpParams);
	   setMultipartParameterContentTypes(mpParamContentTypes);
	   this.exludedParameters = exludedParameters;
	   this.paramPatterns = paramPatterns;
	   this.headerAndParamPatterns = headerAndParamPatterns;
	   this.encryptor = encryptor;
   }
	
   private HttpServletRequest getHttpServletRequest() {
       return super.getRequest();
   }

   @Override
   public Map getParameterMap() {
	   Map<String, String[]> map = super.getParameterMap();
	   return NeutrinoSecurityUtility.getParameterMap(map, exludedParameters, paramPatterns, headerAndParamPatterns, encryptor);
   }
   
   @Override
   public String getQueryString() {
       String query = getHttpServletRequest().getQueryString(); 
       return NeutrinoSecurityUtility.getQueryString(query, encryptor);
   }
   
   @Override
   public String getParameter(String name) {
	   String orig = super.getParameter(name);
	   return NeutrinoSecurityUtility.getParameter(orig, name, exludedParameters, paramPatterns, headerAndParamPatterns, encryptor);
   }

   @Override
   public String[] getParameterValues(String name) {
       String[] values = super.getParameterValues(name); 
       return NeutrinoSecurityUtility.getParameterValues(values, name, exludedParameters, paramPatterns, headerAndParamPatterns, encryptor);
   }

   @Override
   public String getHeader(String name) {
		String value = super.getHeader(name);
		
		if (value != null &&NeutrinoSpringAppContextUtil.getBeanByName("commonConfigUtility",CommonConfigUtility.class).isSanitizingEnabled()) {
			NeutrinoSecurityUtility.checkSanity(value, name, headerAndParamPatterns, exludedParameters);
		}
		return value;
	}
 
	@Override
	protected MultiValueMap<String, MultipartFile> getMultipartFiles() {
		
		if(!NeutrinoSpringAppContextUtil.getBeanByName("commonConfigUtility",CommonConfigUtility.class).isSanitizingEnabled()){
			return super.getMultipartFiles();
		}
		MultiValueMap<String, MultipartFile> map = super.getMultipartFiles();
		MultiValueMap<String, MultipartFile> updatedMap=new LinkedMultiValueMap<>();
        if (map != null && !map.isEmpty()) {
    		for(Map.Entry<String, List<MultipartFile>> entry : map.entrySet()) {
    			for(MultipartFile mfile : entry.getValue()) {
                    CommonsMultipartFile multipartFile = (CommonsMultipartFile)mfile;
    			    if(multipartFile!=null && multipartFile.getFileItem()!=null && multipartFile.getFileItem().getName()!=null){
                        NeutrinoSecurityUtility.checkSanity(multipartFile.getFileItem().getName(), multipartFile.getName(), headerAndParamPatterns, exludedParameters);
                        NeutrinoSecurityUtility.checkSanity(multipartFile.getFileItem().getName(), multipartFile.getName(), paramPatterns, exludedParameters);
                    }
        			NeutrinoSecurityUtility.checkSanity(multipartFile.getOriginalFilename(), multipartFile.getName(), headerAndParamPatterns, exludedParameters);
        			NeutrinoSecurityUtility.checkSanity(multipartFile.getOriginalFilename(), multipartFile.getName(), paramPatterns, exludedParameters);
        			updatedMap.add(entry.getKey(),new NeutrinoCommonsMultipartFile(multipartFile));
    			}
    		}
        }

		return updatedMap;
	}
	
   public  void setExludedParameters(Map<String, Map<String, Object>> exludedParameters) {
	   this.exludedParameters = exludedParameters;
   }
   
   public void setEncryptor(StandardPBEStringEncryptor encryptor) {
	   this.encryptor = encryptor;
   }
   
   public void setParamPatterns(List<PatternConfig> paramPatterns) {
	   this.paramPatterns = paramPatterns;
   }
   
   public void setHeaderAndParamPatterns(List<PatternConfig> headerAndParamPatterns) {
	   this.headerAndParamPatterns = headerAndParamPatterns;
   }
}