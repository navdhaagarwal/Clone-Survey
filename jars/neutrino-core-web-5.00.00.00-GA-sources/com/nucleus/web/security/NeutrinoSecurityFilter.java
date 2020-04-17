/**
     * This file and a proportion of its content is copyrigsht of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.UriUtils;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.logging.BaseLoggers;

/**
 * Filter to decrypt the url parameters. This filter wraps the incoming request and decrypts the url query params so that they can be understood down the 
 * filter chain. The application would need to present the encoded query params to the browser.
 * @author Nucleus Software Exports Limited
 * 
 */
public class NeutrinoSecurityFilter extends NeutrinoUrlExcludableFilter {

    private static StandardPBEStringEncryptor encryptor;
    private String xContentTypeHeaderMode="nosniff";
    private FilterConfig    filterConfig;
    private static final String STRIPPING_STRATEGY="strip";
    private static final String PARAM_BASE_URIS = "base.uris";
    private List<String> baseURIs;
    private static final String URIS_DELIMITER = ",";
    private static final String UTF8="UTF-8";
    private static final String SECURE_TKN="secure_";
    private static final String EXCEPTION_MESSAGE="Exception occured in NeutrinoSecurityFilter:";
    public NeutrinoSecurityFilter() {
        this.baseURIs = new ArrayList<>();
    }
    
    @SuppressWarnings("unchecked")
    @Override
	public void filter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest hrequest = (HttpServletRequest) request;
        HttpServletResponse hresponse = (HttpServletResponse) response;
        setSecurityHeadersInResponse(hrequest, hresponse);      
        HttpServletRequestWrapper secureRequest = initializeHttpServletRequestWrapper(hrequest);
		chain.doFilter(secureRequest, hresponse);
    }
    

	private void setSecurityHeadersInResponse(HttpServletRequest hrequest, HttpServletResponse hresponse) {
  	  hresponse.addHeader("X-Content-Type-Options", xContentTypeHeaderMode);
  	  if ( !hresponse.containsHeader("Expires") && isEligibleForExpiresHeader(hrequest.getRequestURI()) ) {
  		hresponse.addHeader("Expires", "Mon, 01 Jan 1900 16:00:00 GMT");
  	  }
	}
    
    protected HttpServletRequestWrapper initializeHttpServletRequestWrapper(HttpServletRequest hrequest){
        if(STRIPPING_STRATEGY.equalsIgnoreCase(filterConfig.getInitParameter("strategy"))){
            return new ParamValueStrippingSecurityWrapperRequest(hrequest);
        }
        
        return new NeutrinoSecurityWrapperRequest(hrequest, XssPatternsAndExcludedURIHolder.getExludedParameters(hrequest), XssPatternsAndExcludedURIHolder.getParamPatterns(),  
        		XssPatternsAndExcludedURIHolder.getHeaderAndParamPatterns(), 
        		NeutrinoSpringAppContextUtil.getBeanByName("stringEncryptor", StandardPBEStringEncryptor.class) );
    }

    @Override
    public void initFilter(FilterConfig filterConf) throws ServletException {
    	filterConfig = filterConf;
        encryptor = (StandardPBEStringEncryptor)WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig.getServletContext()).getBean("stringEncryptor");
        
        String configBaseURIs = filterConfig.getInitParameter(PARAM_BASE_URIS);
        parseAndStore(configBaseURIs);
      }
    

	private void parseAndStore(String configBaseURIs)
	{
		if (configBaseURIs != null) {
		    List<String> listBaseURIs = parseStringToList(configBaseURIs);
		    this.baseURIs.clear();
		    this.baseURIs.addAll(listBaseURIs);
		}
	}
	
	private List<String> parseStringToList(String data) {
		String[] splits;
	
		if (data != null && data.length() > 0) {
			splits = data.split(URIS_DELIMITER);
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

    private boolean isEligibleForExpiresHeader(String requestUri) {
    	boolean result = false; 

    	if (baseURIs == null || baseURIs.isEmpty()) {
    		return result;	
    	}
    	
    	for (String uri : baseURIs) {
    		result = requestUri.contains(uri);
    		if (result) {
    			break;
    		}
    	}
    	
        return result;
    }
    
    @Override
    public void doDestroy() {
    	
    }

    
    private static final class ParamValueStrippingSecurityWrapperRequest extends HttpServletRequestWrapper implements HttpServletRequest {
        private static List<String> unfilteredParameters = Arrays.asList("ruleExp","ruleExpression","conditionExp","operator","parameterExp","parameterValue","currentTargetOgnl","targetOgnl","expression"
                ,"ruleQueryExp","leftValue","assignmentExp");
        private static List<String> unfilteredRequestUris = Arrays.asList("/Employer","/VapComputationPolicy","/Rule","/RuleInvocationMapping","/Condition","/Parameter","/EventDefinition","/RuleAction","/TaskAssignmentMaster","/AssignmentMaster","/EligibilitySet","/RuleSet","/RuleSimulation");
        private static Pattern[] patterns = new Pattern[]{
            // Script fragments
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            // src='...'
            Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // lonely script tags
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // eval(...)
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // expression(...)
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // javascript:...
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            // vbscript:...
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            // onload(...)=...
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            
        
            Pattern.compile("<", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile(">", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("/>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),   
//            Pattern.compile("\\^", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),        
            Pattern.compile(";", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            //Pattern.compile("]", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            //Pattern.compile("}", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),        
            Pattern.compile("\\(", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
//            Pattern.compile("\\-", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
           // Pattern.compile("\\+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
         //   Pattern.compile("'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            //Pattern.compile("\\[", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            //Pattern.compile("\\{", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        //    Pattern.compile("!", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("alert", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("confirm", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("prompt", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
         
            
            
//            ";alert(1)//123
//            [^&%$#!(){};+'\"\\\\]*
         // onmouseover(...)=...
//            Pattern.compile("onmouseover(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
         // onmouseover(...)=...
//            Pattern.compile("onmouseover
        };
        public ParamValueStrippingSecurityWrapperRequest(HttpServletRequest request) {
            super(request);
        }
        private HttpServletRequest getHttpServletRequest() {
            return (HttpServletRequest) super.getRequest();
        }
        public Map getParameterMap() {
            Map<String, String[]> map = getHttpServletRequest().getParameterMap();
            Map<String, String[]> cleanMap = new HashMap<>();
            for (Object o : map.entrySet()) {
                Map.Entry e = (Map.Entry) o;
                String name = (String) e.getKey();
                String[] value = (String[]) e.getValue();
                String[] cleanValues = new String[value.length];
                for (int j = 0 ; j < value.length ; j++) {
                     StringBuilder cleanValue =new StringBuilder();
                    if(value[j]!=null && value[j].startsWith("enc_"))
                    {   try {
                        value[j]=UriUtils.decode(value[j], UTF8);
                        value[j]=value[j].substring(value[j].indexOf('_')+1, value[j].length());
                        cleanValue.append(encryptor.decrypt(value[j]));
                    } catch (Exception e1) {
                        BaseLoggers.exceptionLogger.debug(EXCEPTION_MESSAGE+e);
                    }   
                    }
                    else if(value[j]!=null && value[j].startsWith(SECURE_TKN)){
                        try{                        
                        value[j]=value[j].substring(value[j].indexOf('_')+1, value[j].length());
                        byte[] decoded = Base64.decodeBase64(value[j]);
                        cleanValue.append(new String(decoded, UTF8));
                        }
                        catch(Exception ex){
                            BaseLoggers.exceptionLogger.debug(EXCEPTION_MESSAGE+ex);
                        }
                    }
                    else
                        cleanValue.append(value[j]);
                    cleanValues[j] = cleanValue.toString();
                }
                cleanMap.put(name, cleanValues);

            }
            return cleanMap;
        }

        public String getQueryString() {
            String query = getHttpServletRequest().getQueryString();
            StringBuilder mainBuffer=new StringBuilder();
            if(query!=null && query.length()>0){
            StringTokenizer strToken=new StringTokenizer(query, "&"); 
            Integer counter=0;
              while(strToken.hasMoreTokens()){
            	StringBuilder str2bffr=new StringBuilder();
                if(counter>0){str2bffr.append("&");}
                counter++;
                String a=strToken.nextToken();
                str2bffr.append(a.substring(0, a.indexOf('=')));
                 try {
                    a=a.substring(a.indexOf('=')+1,a.length());
                    String str2b2 = UriUtils.decode(a,UTF8);
                
                if(str2b2.startsWith("enc_")){
                    str2b2=str2b2.substring(str2b2.indexOf('_')+1, str2b2.length());
                    if(str2b2.contains("%"))
                    str2b2=UriUtils.decode(str2b2,UTF8);
                    str2b2="="+encryptor.decrypt(str2b2);
                    str2bffr.append(str2b2);
                }
                else if(str2b2!=null && str2b2.startsWith(SECURE_TKN)){
                    try{
                        str2b2=str2b2.substring(str2b2.indexOf('_')+1, str2b2.length());
                        byte[] decoded = Base64.decodeBase64(str2b2);
                        str2b2=new String(decoded, UTF8);
                        str2b2="="+str2b2;
                        str2bffr.append(str2b2);                        
                        }
                        catch(Exception ex){
                            BaseLoggers.exceptionLogger.debug(EXCEPTION_MESSAGE+ex);
                        }
                    }
                else 
                    str2bffr.append("="+str2b2);
                    mainBuffer.append(str2bffr);
                } catch (Exception e) {
                BaseLoggers.exceptionLogger.debug(EXCEPTION_MESSAGE+e);
                }
              }
            } else return getHttpServletRequest().getQueryString();
           return mainBuffer.toString();
        }

        public String getParameter(String name) {
            String orig = getHttpServletRequest().getParameter(name);
            StringBuilder clean = new StringBuilder();
            if(orig!=null){
            if(orig.startsWith("enc_"))
            {    try {
                    orig=UriUtils.decode(orig,UTF8);
                    orig=orig.substring(orig.indexOf('_')+1, orig.length());
                    clean.append(encryptor.decrypt(orig));
                    } 
                catch (Exception e) {
                        BaseLoggers.exceptionLogger.debug("Exception occured in NeutrinoSecurityFilter : "+e);
                    }
            }

            else if(orig.startsWith(SECURE_TKN)){
                try{                
                orig=orig.substring(orig.indexOf('_')+1, orig.length());
                byte[] decoded = Base64.decodeBase64(orig);
                clean.append(new String(decoded, UTF8));
                }
                catch(Exception ex){
                    BaseLoggers.exceptionLogger.debug(EXCEPTION_MESSAGE+ex);
                }
            }
            
            else
                    clean.append(orig); 
                }
            else
                return getHttpServletRequest().getParameter(name);
            
            return clean.toString();
        }

        public String[] getParameterValues(String name) {
            String[] values = getHttpServletRequest().getParameterValues(name);
            String requestUri = getHttpServletRequest().getRequestURI();
            List<String> newValues = new ArrayList<>();
            if(values!=null && values.length>0)
            for (String value : values) {
            	StringBuilder cleanValue =new StringBuilder();
                if(value!=null && value.startsWith("enc_"))
                {   try {
                    value=UriUtils.decode(value,UTF8);
                    value=value.substring(value.indexOf('_')+1, value.length());
                    cleanValue.append(encryptor.decrypt(value));
                    } 
                    catch (Exception e) {
                    BaseLoggers.exceptionLogger.debug(EXCEPTION_MESSAGE+e);
                    }
                }
                else if(value!=null && value.startsWith(SECURE_TKN)){
                    try{
                    value=value.substring(value.indexOf('_')+1, value.length());
                    byte[] decoded = Base64.decodeBase64(value);
                    cleanValue.append(new String(decoded, UTF8));
                    }
                    catch(Exception ex){
                        BaseLoggers.exceptionLogger.debug(EXCEPTION_MESSAGE+ex);
                    }
                }
                else{
                    boolean filter = true;              
                    for(String unfilteredUri:unfilteredRequestUris){
                        if(requestUri.contains(unfilteredUri)){
                            filter = false;
                            break;
                        }
                    }
                    if(filter){
                        for(String unfilteredParam:unfilteredParameters){
                            if(name.contains(unfilteredParam)){
                                filter = false;
                                break;
                            }
                        }                       
                    }
                    String tmpValue=value;
                    if(value!=null && value.trim().length()>0 && filter){                       
                        for (Pattern scriptPattern : patterns){
                            value = scriptPattern.matcher(value).replaceAll("");        
                        }
                    }
                    if(tmpValue!=null && value!=null && !tmpValue.equals(value)){
                            BaseLoggers.exceptionLogger.error("Unwanted parameter values found for parameter name :- "+name+" and parameter value is "+tmpValue);
                    }
                    cleanValue.append(value);
                }
                    newValues.add(cleanValue.toString());
           }
            else return getHttpServletRequest().getParameterValues(name);
          return newValues.toArray(new String[newValues.size()]);
        }

    }

}
