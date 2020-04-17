package com.nucleus.web.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.session.SessionFixationProtectionEvent;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriUtils;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.security.BlacklistCondition;
import com.nucleus.core.security.PatternConfig;
import com.nucleus.event.EventService;
import com.nucleus.event.EventTypes;
import com.nucleus.event.UserSecurityTrailEvent;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.html.util.HtmlUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.common.CommonConfigUtility;

@Named("neutrinoSecurityUtility")
public class NeutrinoSecurityUtility {

	@Inject
	@Named("userService")
	private UserService userService;
	
	
	@Autowired
	private EventService eventService;
	
	@Inject
    @Named("commonConfigUtility")
    private CommonConfigUtility commonConfigUtility;
	  
	private NeutrinoSecurityUtility(){}
	private static boolean sanitizationEnabled=true;
	
	private static final String EQUALS="=";
	
	private static final String AND="&";
	
	private static final String UTF8="UTF-8";
	
	private static final String SECURE="secure_";
	
	private static final String ENC="enc_";
	
	private static final String MODULE_SSO="SSO";
	
	private static final String MODULE_MOBILITY="MOBILITY";

	public static void checkSanity(String parameterValue, String parameterName, List<PatternConfig> patterns, 
			Map<String, Map<String, Object>> exludedParameters) {
		boolean filter = Boolean.TRUE;
		if (exludedParameters != null && !exludedParameters.isEmpty()
				&& exludedParameters.get(parameterName) != null) {
			filter = Boolean.FALSE;
			Map<String, Object> policyMap = exludedParameters.get(parameterName);
			if(policyMap.get("isPolicyApplicable").equals("true")) {
				//Change Listener if any elements or attributes are discarded, has to be passed to policy.sanitize method
				NeutrinoHtmlChangeListener neutrinoHtmlChangeListener=NeutrinoSpringAppContextUtil.getBeanByName("neutrinoHtmlChangeListener", NeutrinoHtmlChangeListener.class);

				PolicyFactory policy = (PolicyFactory)policyMap.get("policy");
				String []ctx=new String[2];
				ctx[0]=parameterName;
				ctx[1]=parameterValue;
				String safeHTML = policy.sanitize(parameterValue,neutrinoHtmlChangeListener,ctx);
			}
		}
		if (filter) {
			for (PatternConfig scriptPattern : patterns) {
				applyPattern(scriptPattern,parameterName,parameterValue);
			}
		}
	}

	private static void applyPattern(PatternConfig scriptPattern, String parameterName, String parameterValue) {
		if (patternExecutionRequired(scriptPattern.getBalckListCondition(), parameterValue)) {
			if (scriptPattern.isApplyPattern()) {
				//scan parameter based on condition and pattern or only with pattern
				testPattern(scriptPattern.getPatterns(), parameterName, parameterValue);
			} else {
				//scan parameter only based on condition 
				throwXssException(parameterName, parameterValue);
			}
		}
	}

	private static boolean patternExecutionRequired(BlacklistCondition beforePattern, String parameterValue) {
		return (beforePattern==null) || beforePattern.check(parameterValue);
	}

	private static void testPattern(List<Pattern> patterns, String parameterName, String parameterValue) {
		for (Pattern pattern : patterns) {
			if (pattern.matcher(parameterValue).lookingAt()) {
				throwXssException(parameterName, parameterValue);
			}
		}
	}

	public static void throwXssException(String parameterName,String parameterValue)
	{
		XssException xsse = (XssException)ExceptionBuilder.getInstance(XssException.class)
				.setMessage("fmsg.0001", new String[]{parameterValue})
				.setLogMessage("Unwanted parameter/header values found for \n parameter/header name : "
						+ HtmlUtils.htmlEscape(parameterName)
						+ " \n parameter/header value is "
						+ HtmlUtils.htmlEscape(parameterValue)).build();
		BaseLoggers.exceptionLogger.error(xsse.getLogMessage(),xsse);
		throw xsse;

	}

	public static Map getParameterMap(Map<String, String[]> map, Map<String, Map<String, Object>> exludedParameters,
			List<PatternConfig> paramPatterns, List<PatternConfig> headerAndParamPatterns, StandardPBEStringEncryptor encryptor) {
		Map<String, String[]> cleanMap = new HashMap<String, String[]>();
		for (Object o : map.entrySet()) {
			Map.Entry e = (Map.Entry) o;
			String name = (String) e.getKey();
			String[] value = (String[]) e.getValue();
			String[] cleanValues = new String[value.length];
			for (int j = 0 ; j < value.length ; j++) {
				StringBuilder cleanValue =new StringBuilder();
				if(value[j]!=null && value[j].startsWith(ENC))
				{	try {
					value[j]=UriUtils.decode(value[j], UTF8);
					value[j]=value[j].substring(value[j].indexOf("_")+1, value[j].length());
					cleanValue.append(encryptor.decrypt(value[j]));
					if(sanitizationEnabled)
					{
						NeutrinoSecurityUtility.checkSanity( cleanValue.toString(), name, headerAndParamPatterns, exludedParameters);
						NeutrinoSecurityUtility.checkSanity( cleanValue.toString(), name, paramPatterns, exludedParameters);
					}
				} catch (Exception e1) {
					BaseLoggers.exceptionLogger.debug("Exception occured in NeutrinoMultipartHttpServletRequest:"+e);
				}	
				}
				else if(value[j]!=null && value[j].startsWith(SECURE)){
					try{                		
						value[j]=value[j].substring(value[j].indexOf("_")+1, value[j].length());
						byte[] decoded = Base64.decodeBase64(value[j]);
						cleanValue.append(new String(decoded, UTF8));
						if(sanitizationEnabled){
							NeutrinoSecurityUtility.checkSanity( cleanValue.toString(), name, headerAndParamPatterns, exludedParameters);
							NeutrinoSecurityUtility.checkSanity( cleanValue.toString(), name, paramPatterns, exludedParameters);
						}
					}
					catch(Exception ex){
						BaseLoggers.exceptionLogger.debug("Exception occured in NeutrinoMultipartHttpServletRequest:"+ex);
					}
				}
				else if (value[j] != null) {
					cleanValue.append(value[j]);
					if(sanitizationEnabled){
						NeutrinoSecurityUtility.checkSanity(cleanValue.toString(), name, headerAndParamPatterns, exludedParameters);
						NeutrinoSecurityUtility.checkSanity(cleanValue.toString(), name, paramPatterns, exludedParameters);
					}
				}
				cleanValues[j] = cleanValue.toString();
			}
			cleanMap.put(name, cleanValues);

		}
		return cleanMap;		
	}

	public static String getQueryString(String query, StandardPBEStringEncryptor encryptor) {
			if(StringUtils.isEmpty(query) || query.indexOf('=')==-1){
				return query;
			}
			String decodedQuery=query;
			try {
				decodedQuery=UriUtils.decode(query,UTF8);
			} catch (Exception ex) {
				BaseLoggers.exceptionLogger.debug("Exception occured in NeutrinoMultipartHttpServletRequest:",ex);
			}
			if(decodedQuery.indexOf(ENC)==-1 && decodedQuery.indexOf(SECURE)==-1){
				return query;
			}

			StringBuilder queryStr=new StringBuilder();
			StringTokenizer strToken=new StringTokenizer(decodedQuery, AND); 
			Integer counter=0;
			while(strToken.hasMoreTokens()){
				if(counter>0){queryStr.append(AND);}
				counter++;
				String queryParamPair=strToken.nextToken();
				if(queryParamPair.indexOf(EQUALS)==-1){
					queryStr.append(queryParamPair);
					continue;
				}
				queryStr.append(queryParamPair.substring(0, queryParamPair.indexOf(EQUALS)));
				try {
					String paremValue = queryParamPair.substring(queryParamPair.indexOf(EQUALS)+1,queryParamPair.length());
					if(paremValue.startsWith(ENC)){
						String encryptedParemValue=paremValue.substring(paremValue.indexOf("_")+1, paremValue.length());
						if(encryptedParemValue.contains("%")){
							encryptedParemValue=UriUtils.decode(encryptedParemValue,UTF8);
						}
						queryStr.append(EQUALS+encryptor.decrypt(encryptedParemValue));
					}
					else if(paremValue!=null && paremValue.startsWith(SECURE)){
						try{
							paremValue=paremValue.substring(paremValue.indexOf("_")+1, paremValue.length());
							byte[] decoded = Base64.decodeBase64(paremValue);
							paremValue=new String(decoded, UTF8);
							paremValue=EQUALS+paremValue;
							queryStr.append(paremValue);                  		
						}
						catch(Exception ex){
							BaseLoggers.exceptionLogger.debug("Exception occured in NeutrinoMultipartHttpServletRequest:",ex);
						}
					}
					else{
						queryStr.append(EQUALS+paremValue);
					}
				} catch (Exception e) {
					BaseLoggers.exceptionLogger.debug("Exception occured in NeutrinoMultipartHttpServletRequest:",e);
				}
			}
		return queryStr.toString();
	}
	
	public static String getParameter(String orig, String name, Map<String, Map<String, Object>> exludedParameters,
			List<PatternConfig> paramPatterns, List<PatternConfig> headerAndParamPatterns, StandardPBEStringEncryptor encryptor) {
		StringBuilder clean = new StringBuilder();
		if(orig!=null){
			if(orig!=null && orig.startsWith(ENC))
			{    try {
				orig=UriUtils.decode(orig,UTF8);
				orig=orig.substring(orig.indexOf("_")+1, orig.length());
				clean.append(encryptor.decrypt(orig));
				if(sanitizationEnabled){
					NeutrinoSecurityUtility.checkSanity( clean.toString(), name, headerAndParamPatterns, exludedParameters);
					NeutrinoSecurityUtility.checkSanity( clean.toString(), name, paramPatterns, exludedParameters);
				} }
			catch (Exception e) {
				BaseLoggers.exceptionLogger.debug("Exception occured in NeutrinoMultipartHttpServletRequest : "+e);
			}
			}

			else if(orig!=null && orig.startsWith(SECURE)){
				try{        		
					orig=orig.substring(orig.indexOf("_")+1, orig.length());
					byte[] decoded = Base64.decodeBase64(orig);
					clean.append(new String(decoded, UTF8));
					if(sanitizationEnabled){
						NeutrinoSecurityUtility.checkSanity( clean.toString(), name, headerAndParamPatterns, exludedParameters);
						NeutrinoSecurityUtility.checkSanity( clean.toString(), name, paramPatterns, exludedParameters);
					}
				}
				catch(Exception ex){
					BaseLoggers.exceptionLogger.debug("Exception occured in NeutrinoMultipartHttpServletRequest:"+ex);
				}
			}

			else {
				clean.append(orig);
				if(sanitizationEnabled){
					NeutrinoSecurityUtility.checkSanity(clean.toString(), name, headerAndParamPatterns, exludedParameters);
					NeutrinoSecurityUtility.checkSanity(clean.toString(), name, paramPatterns, exludedParameters);
				}
			}
		}
		else {
			return orig;
		}
		return clean.toString();		
	}

	public static String[] getParameterValues(String[] values, String name,  Map<String, Map<String, Object>> exludedParameters,
			List<PatternConfig> paramPatterns, List<PatternConfig> headerAndParamPatterns, StandardPBEStringEncryptor encryptor) {

		List<String> newValues = new ArrayList<String>();
		if (values != null && values.length > 0) {
			for (String value : values) {
				StringBuilder cleanValue =new StringBuilder();
				if(value!=null && value.startsWith(ENC))
				{	
					try {
						value=UriUtils.decode(value,UTF8);
						value=value.substring(value.indexOf("_")+1, value.length());
						cleanValue.append(encryptor.decrypt(value));
						if(sanitizationEnabled){
							NeutrinoSecurityUtility.checkSanity( cleanValue.toString(), name, headerAndParamPatterns, exludedParameters);
							NeutrinoSecurityUtility.checkSanity( cleanValue.toString(), name, paramPatterns, exludedParameters);
						} 
					}
					catch (Exception e) {
						BaseLoggers.exceptionLogger.debug("Exception occured in NeutrinoMultipartHttpServletRequest:"+e);
					}
				}
				else if(value!=null && value.startsWith(SECURE)){
					try{
						value=value.substring(value.indexOf("_")+1, value.length());
						byte[] decoded = Base64.decodeBase64(value);
						cleanValue.append(new String(decoded, UTF8));
						if(sanitizationEnabled){
							NeutrinoSecurityUtility.checkSanity( cleanValue.toString(), name, headerAndParamPatterns, exludedParameters);
							NeutrinoSecurityUtility.checkSanity( cleanValue.toString(), name, paramPatterns, exludedParameters);
						}
						
					}
					catch(Exception ex){
						BaseLoggers.exceptionLogger.debug("Exception occured in NeutrinoMultipartHttpServletRequest:"+ex);
					}
				}
				else {
					cleanValue.append(value);
					if(sanitizationEnabled){
						NeutrinoSecurityUtility.checkSanity(cleanValue.toString(), name, headerAndParamPatterns, exludedParameters);
						NeutrinoSecurityUtility.checkSanity(cleanValue.toString(), name, paramPatterns, exludedParameters);
					}
				}
				newValues.add(cleanValue.toString());
			}
		}
		else {
			return values;
		}

		return newValues.toArray(new String[newValues.size()]);
	}
	public static void updateSantizingEnabled(boolean sanitizationEnabled ){
		NeutrinoSecurityUtility.sanitizationEnabled=sanitizationEnabled;
	}
	
	public void createAuthenticationSuccessEventEntry(AuthenticationSuccessEvent authenticationSuccessEvent, SessionFixationProtectionEvent sessionFixationProtectionEvent)
	{

		if (authenticationSuccessEvent != null
				&& commonConfigUtility.getSsoActive()
				&& (authenticationSuccessEvent instanceof SSOAuthenticationSuccessEvent)){
			// This check comes into picture only in case of SSO.
			// Preventing event entry into DB until we have
			// established that user has successfully logged in.
			// When user has successfully logged in authenticationSuccessEvent
			// will be an instance of SSOAuthenticationSuccessEvent
			createAuthenticationSuccessEntry(authenticationSuccessEvent,null,MODULE_SSO);
			return;
		}
		
		//create a login entry in GenericEvent when accessing the module if SSO is enabled
		//or when login to the application if SSO is disabled
		if(!commonConfigUtility.getSsoActive() 
				|| (authenticationSuccessEvent != null && (authenticationSuccessEvent.getSource() instanceof CasAuthenticationToken))){
			createAuthenticationSuccessEntry(authenticationSuccessEvent, sessionFixationProtectionEvent,ProductInformationLoader.getProductName());
			return;
		}
		
		if(authenticationSuccessEvent != null && (authenticationSuccessEvent.getSource() instanceof UsernamePasswordAuthenticationToken) &&
			authenticationSuccessEvent.getAuthentication() != null 
			&& (authenticationSuccessEvent.getAuthentication().getPrincipal() instanceof UserInfo)){
            createAuthenticationSuccessEntry(authenticationSuccessEvent,null,MODULE_MOBILITY);
		}
	}
	
	
	private void createAuthenticationSuccessEntry(AuthenticationSuccessEvent authenticationSuccessEvent,SessionFixationProtectionEvent sessionFixationProtectionEvent,String moduleName) {
		UserInfo userInfo = null;
		AbstractAuthenticationToken abstractAuthenticationToken = null;
	
		UserSecurityTrailEvent userSecurityTrailEvent = new UserSecurityTrailEvent(EventTypes.USER_SECURITY_TRAIL_LOGIN_SUCCESS);
		userSecurityTrailEvent.addPersistentProperty("APP_SERVER_IP_ADDRESS", commonConfigUtility.getNodeIPAddress());
		userSecurityTrailEvent.setModuleNameForEvent(moduleName);
		
		if (authenticationSuccessEvent != null){	    	
			if (authenticationSuccessEvent.getSource() != null) {
				abstractAuthenticationToken = (AbstractAuthenticationToken)authenticationSuccessEvent.getSource();
			}
			if ((authenticationSuccessEvent.getAuthentication() != null) && 
					(authenticationSuccessEvent.getAuthentication().getPrincipal() instanceof UserInfo)) {
				userInfo = (UserInfo)authenticationSuccessEvent.getAuthentication().getPrincipal();
			}
			if ((abstractAuthenticationToken != null) && (abstractAuthenticationToken.getDetails() != null) && 
					(abstractAuthenticationToken.getDetails() instanceof WebAuthenticationDetails)){
				HttpServletRequest request =null;
				RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
				if (requestAttributes instanceof ServletRequestAttributes) {
					request = ((ServletRequestAttributes)requestAttributes).getRequest();
				}
				String remoteIP = null;
				if(request!=null){
					remoteIP = request.getHeader("x-forwarded-for");
					if (remoteIP == null || "".equals(remoteIP)) {
						remoteIP = request.getHeader("x-forwarded-by");
					}
					if (remoteIP == null || "".equals(remoteIP)) {
						remoteIP = request.getRemoteHost();
					}
				}else{
					remoteIP=
							((WebAuthenticationDetails)abstractAuthenticationToken.getDetails()).getRemoteAddress();
				}
				userSecurityTrailEvent.setRemoteIpAddress(remoteIP);
				if (sessionFixationProtectionEvent == null){
					userSecurityTrailEvent.setSessionId(((WebAuthenticationDetails)abstractAuthenticationToken.getDetails()).getSessionId());					
				}else{
					userSecurityTrailEvent.setSessionId(sessionFixationProtectionEvent.getNewSessionId());
				}
			}
		}
		if (userInfo != null && userInfo.getUsername() != null){
			userInfo.getUserEntityId();
			userSecurityTrailEvent.setUsername(userInfo.getUsername());
			userSecurityTrailEvent.setAssociatedUserUri(userInfo.getUserEntityId().getUri());
			this.userService.resetFailedLoginCountToZero(userInfo.getId());
		}
		this.eventService.createEventEntry(userSecurityTrailEvent);
		
	}

	protected void createSessionDestroyedEventEntry(Map<String,String> ssoSessionMap) {
        if(ssoSessionMap == null) {
        	return;
        }
        
        String sessionId = ssoSessionMap.get("sessionId");
    	String username = ssoSessionMap.get("username");
    	String remoteIpAddress = ssoSessionMap.get("remoteIpAddress");
    	String associatedUserUri = ssoSessionMap.get("associatedUserUri");
    	
    	if(sessionId != null && username != null) {
    		UserSecurityTrailEvent userSecurityTrailEvent = new UserSecurityTrailEvent(EventTypes.USER_SECURITY_TRAIL_LOGOUT);
    		
    		userSecurityTrailEvent.setSessionId(sessionId);
    		userSecurityTrailEvent.setUsername(username);
            userSecurityTrailEvent.setAssociatedUserUri(associatedUserUri);
			userSecurityTrailEvent.setModuleNameForEvent("SSO");
			userSecurityTrailEvent.setRemoteIpAddress(remoteIpAddress);
			userSecurityTrailEvent.setLogOutType(NeutrinoSessionInformation.LOGOUT_TYPE_BY_SSO_LOGOUT);
			eventService.createEventEntry(userSecurityTrailEvent);
    		
    	}
    }
	
	
	
	/*private void setModuleNameForEventForSSOprofile(AuthenticationSuccessEvent authenticationSuccessEvent, UserSecurityTrailEvent userSecurityTrailEvent)
	{
		if ((authenticationSuccessEvent.getSource() instanceof UsernamePasswordAuthenticationToken)) {
			userSecurityTrailEvent.setModuleNameForEvent("SSO");
		} else {
		      userSecurityTrailEvent.setModuleNameForEvent(ProductInformationLoader.getProductName());
	    }
	}*/
	
}
