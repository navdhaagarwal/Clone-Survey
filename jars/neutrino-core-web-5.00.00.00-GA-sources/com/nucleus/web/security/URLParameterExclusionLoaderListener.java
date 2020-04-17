package com.nucleus.web.security;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.HtmlPolicyBuilder.AttributeBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.nucleus.core.security.BlackListPatternHolder;
import com.nucleus.core.security.PatternConfig;
import com.nucleus.core.security.entities.AdditionalBlackListPattern;
import com.nucleus.core.security.entities.HtmlAttributes;
import com.nucleus.core.security.entities.HtmlElements;
import com.nucleus.core.security.entities.HtmlSanitizerPolicy;
import com.nucleus.core.security.entities.UnfilteredParameter;
import com.nucleus.core.security.entities.UnfilteredRequestUri;
import com.nucleus.core.security.service.URLParameterExclusionService;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
@Named("urlParameterExclusionLoaderListener")
public class URLParameterExclusionLoaderListener implements ServletContextListener {

	private static ApplicationContext appCtx;
	private static final String MESSAGE_EXCEPTION = "Error in method contextInitialized in URLParameterExclusionLoaderListener ";
	private static final String HTTPS = "https";
	private static final String HTTP = "http";
	
	public void setApplicationContext(ServletContext servletConext){
		appCtx =WebApplicationContextUtils.getRequiredWebApplicationContext(servletConext);
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			setApplicationContext(sce.getServletContext());
			URLParameterExclusionService urlParameterExclusionService = appCtx.getBean("urlParameterExclusionService", URLParameterExclusionService.class);
			List<UnfilteredRequestUri> unfilteredRequestUris = urlParameterExclusionService.findAllUnfilteredRequestUris();
			Map<String, Map<String, Map<String, Object>>> unfilteredURLParameterMap = new HashMap<String, Map<String, Map<String, Object>>>();
			if (unfilteredRequestUris != null && !unfilteredRequestUris.isEmpty()) {
				for (UnfilteredRequestUri unfilteredRequestUri : unfilteredRequestUris) {
					Set<UnfilteredParameter> parameters = unfilteredRequestUri.getParameters();
					Map<String, Map<String, Object>> parameterMap = getParameterMap(parameters);
					//add an object of policy builder to this map 
					unfilteredURLParameterMap.put(unfilteredRequestUri.getApplicationURI(), parameterMap);
				}
			}

			XssPatternsAndExcludedURIHolder.setUnfilteredURLParameterMap(unfilteredURLParameterMap);
			//Reading additionalHeaderParameters
			List<AdditionalBlackListPattern> additionalHeaderInputBlackListParameters =urlParameterExclusionService.findAllAdditionalBlackListPatterns();		
			List<PatternConfig> paramPatternList=new ArrayList<>();
			List<PatternConfig> paramAndHeaderPatternList=new ArrayList<>();
			Map<String, Pattern> codeWiseBlackListPatterns=new HashMap<String,Pattern>();
			if(CollectionUtils.isNotEmpty(additionalHeaderInputBlackListParameters))
			{
				
				Map<String, List<Pattern>> paramPatternsWithCondition=new HashMap<>();
				Map<String, List<Pattern>> headerAndparamPatternsWithCondition=new HashMap<>();
				for(AdditionalBlackListPattern blackListPattern:additionalHeaderInputBlackListParameters)
				{
					Pattern pattern=getPattern(blackListPattern.getPattern(),blackListPattern.getFlags());
					boolean isPatternWithCondition=false;
					if(StringUtils.isNotBlank(blackListPattern.getIndexOfPart())){
						isPatternWithCondition=true;
					}
					BaseLoggers.flowLogger.info("adding pattern "+pattern.pattern());
					if(AdditionalBlackListPattern.PARAM.equalsIgnoreCase(blackListPattern.getType()))
					{
						addPatternToList(paramPatternList, blackListPattern.getIndexOfPart(), pattern,
								isPatternWithCondition, paramPatternsWithCondition);
					}
					else
					{
						addPatternToList(paramAndHeaderPatternList, blackListPattern.getIndexOfPart(), pattern,
								isPatternWithCondition, headerAndparamPatternsWithCondition);

					}
					if(notNull(blackListPattern.getCode()))
					{
						codeWiseBlackListPatterns.put(blackListPattern.getCode(),pattern);
					}
				}
				addConditionBasedPatterns(paramAndHeaderPatternList,headerAndparamPatternsWithCondition);
				addConditionBasedPatterns(paramPatternList,paramPatternsWithCondition);
			}
			BlackListPatternHolder.setBlackListPatternMap(codeWiseBlackListPatterns);
			BaseLoggers.flowLogger.debug("Succesfully read patterns from DB");
			XssPatternsAndExcludedURIHolder.addParamPatterns(paramPatternList);
			XssPatternsAndExcludedURIHolder.addHeaderAndParamPatterns(paramAndHeaderPatternList);
		} catch (Exception exception) {
			BaseLoggers.exceptionLogger.error(MESSAGE_EXCEPTION,exception);
			  throw ExceptionBuilder.getInstance(SystemException.class, MESSAGE_EXCEPTION, MESSAGE_EXCEPTION)
			  .setOriginalException(exception)
	          .setMessage(CoreUtility.prepareMessage(MESSAGE_EXCEPTION)).setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).build();
		}
	}

	private void addConditionBasedPatterns(List<PatternConfig> paramAndHeaderPatternList,
			Map<String, List<Pattern>> patternsWithCondition) {
		if(ValidatorUtils.hasNoEntry(patternsWithCondition)){
			return;
		}
		
		patternsWithCondition.forEach((indexOfPart,listOfPatterns)->{
			BaseLoggers.flowLogger.error("Constant part '{}' is configured with patterns {}",indexOfPart,listOfPatterns);
			paramAndHeaderPatternList.add(
					new PatternConfig((input -> input.indexOf(indexOfPart) != -1),listOfPatterns)
					);
		});
		
	}

	private void addPatternToList(List<PatternConfig> paramPatternList,String indexOfPart, Pattern blackListPattern,
			boolean isPatternWithCondition, Map<String, List<Pattern>> paramPatternsWithCondition) {
		if(!isPatternWithCondition){
			paramPatternList.add(new PatternConfig(blackListPattern));
			return;
		}
		List<Pattern> patterns=paramPatternsWithCondition.get(indexOfPart);
		if(patterns==null){
			patterns=new ArrayList<>();
		}
		patterns.add(blackListPattern);
		paramPatternsWithCondition.put(indexOfPart, patterns);
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		BaseLoggers.flowLogger.debug("URLParameterExclusionLoaderListener contextDestroyed()");
	}
	
	Map<String, Map<String, Object>> getParameterMap(Set<UnfilteredParameter> parameters) {
		Map<String, Map<String, Object>> parameterMap = new HashMap<String, Map<String, Object>>();
		if (parameters != null && !parameters.isEmpty()) {
			for (UnfilteredParameter unfilteredParameter : parameters) {
				//Policy Factory builds policy using elements attributes and values allowed for a particular parameter
				PolicyFactory policy = getHtmlPolicy(unfilteredParameter);
				Map<String, Object> policyMap = new HashMap<String, Object>();
				if(policy == null) {
					policyMap.put("isPolicyApplicable", "false");
				}
				
				else{
					policyMap.put("isPolicyApplicable", "true");
					policyMap.put("policy" , policy);
				}
				
				parameterMap.put(unfilteredParameter.getName(), policyMap);
			}
		}
		return parameterMap;
	}
	
	private PolicyFactory getHtmlPolicy(UnfilteredParameter unfilteredParameter) {
		//Parameter->Policy->list of elements->list of attributes-> values
		HtmlSanitizerPolicy htmlSanitizerPolicy = unfilteredParameter.getHtmlSanitizerPolicy();
		if(htmlSanitizerPolicy == null){
			return null;
		}
		
		List<HtmlElements> htmlElementsList = htmlSanitizerPolicy.getHtmlelements();
		List<String[]> htmlElementsListOfString = new ArrayList<String[]>();
		HtmlPolicyBuilder policyBuilder=new HtmlPolicyBuilder();
		
		policyBuilder.allowUrlProtocols(HTTP,HTTPS);
		
		for(HtmlElements htmlelements : htmlElementsList) {
			String[] elementsArray = htmlelements.getHtmlTags().split(",");
			htmlElementsListOfString.add(elementsArray);
			List<HtmlAttributes> htmlAttributeslist = htmlelements.getHtmlAttributes();
			
			for(HtmlAttributes htmlAttributes : htmlAttributeslist) {
				String attributes = htmlAttributes.getHtmlAttributes();
				if(attributes != null && !attributes.isEmpty()) {
					String[] attributeArray = attributes.split(",");
					AttributeBuilder attributeBuilder=policyBuilder.allowAttributes(attributeArray);
					String value = htmlAttributes.getAttributeValue();
					if(value != null && !value.isEmpty()) {
						Pattern attributeValuePattern = Pattern.compile(value);
						attributeBuilder = attributeBuilder.matching(attributeValuePattern);
					}
					policyBuilder = attributeBuilder.onElements(elementsArray);
				}
			}
		}
		
		for(String[] string :htmlElementsListOfString) {
			policyBuilder = policyBuilder.allowElements(string);	
		}
		
		PolicyFactory policy = policyBuilder.toFactory();
		return policy;
	}
	
	private Pattern getPattern(String pattern,String flags)
	{
		StringTokenizer st=new StringTokenizer(flags,"|");
		int bitMask=0;
		while(st.hasMoreTokens())
		{
			String str=st.nextToken();
			bitMask=bitMask|getFlag(str.trim());
		}
		return Pattern.compile(pattern,bitMask);
		
	}

	private int getFlag(String str) {
	        
	        try {
                 Class patternClass=Class.forName("java.util.regex.Pattern");
                 Field field=patternClass.getDeclaredField(str);
                return field.getInt(null);
            } catch (ClassNotFoundException e) {
            	SystemException se = new SystemException("Exception: Pattern class not found",e);
            	BaseLoggers.exceptionLogger.error(se.getMessage(),se);
                throw se;
            } catch (NoSuchFieldException e) {
            	SystemException se = new SystemException("Exception: Wrong flag specified",e);
            	BaseLoggers.exceptionLogger.error(se.getMessage(),se);
                throw se;
            } catch (SecurityException e) {
            	SystemException se = new SystemException("Exception while getting Pattern flag vlues"+str,e);
            	BaseLoggers.exceptionLogger.error(se.getMessage(),se);
                throw se;
            } catch (IllegalArgumentException e) {
            	SystemException se = new SystemException("Exception: illegal argument",e);
            	BaseLoggers.exceptionLogger.error(se.getMessage(),se);
                throw se;
            } catch (IllegalAccessException e) {
            	SystemException se = new SystemException("Exception: illegal access",e);
            	BaseLoggers.exceptionLogger.error(se.getMessage(),se);
                throw se;
            }
		
	}

}
