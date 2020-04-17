package com.nucleus.web.filters;

import static com.nucleus.web.security.AesUtil.PASS_PHRASE;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.web.security.FilterExcludedUrlHolder;
import com.nucleus.web.security.NeutrinoBodyManipulationFilterConfig;
import com.nucleus.web.security.XssException;
import com.nucleus.logging.BaseLoggers;

public class NeutrinoBodyManipulationFilter implements Filter{

	public static final String  UNMODIFIABLE_PROPERTY_PARAMETER_NOTAPPLICABLE= "bhcbhcbk23n";

	private static final String COMMA = ",";
	private static final String COMMA_SPACE = ", ";
	private static final String DELIMETER = ":-:";
	private static final String AES = "AES";
	private static final String SALT = "oiybECjo";// MUST be 8 digits, MUST
	private static final String UN_MODIFIABLE_KEY = "unmodifiableDatakey";
	//	Logging constants
	private static final String LOG_SESSION_ID = "SESSION ID :";
	private static final String LOG_NAME_VALUE_PAIR = " name/value :";
	private static final String LOG_TAMPERING_DONE_FOR = " FIELD TAMPERING DONE for input :";
	private static final String LOG_UN_MODIFIABLE_KEY = " unmodifiableDataKey :";
	private static final String LOG_FOR_URI = " for URI :";
	private static final String SPACE = " ";
	private static final String LOG_GOT_NOT_NULL_DATA = " GOT NOT NULL DATA :";
	private static final String LOG_TOTAL_TIME_FOR_1ST_LOOP = " TOTAL TIME 1st for loop :";
	private static final String LOG_DECRYPTED_DATA = " DECRYPTED DATA :";
	private static final String LOG_TOTAL_TIME_FOR_2ND_LOOP = " TOTAL TIME 2nd for loop : ";
	private static final String LOG_TOTAL_TIME = " TOTAL TIME :";
	private static final String LOG_ERROR = "Some error occurred ERROR CODE :13";
	private static final String LOG_DIDNOT_GOT_VALID_TOKEN = " got GET Request for URI :";
	private static final String LOG_GOT_DECRYPTED_NAME_VALUE = " Got Decrypted name/value pair as :";
	private static final String LOG_NO_PARAM_TO_PROTECT = " NO INPUT PARAM TO PROTECT hence returning";
	private static final String LOG_VALUES_FROM_HEADER = " Values from Header :";
	
	private static final Base64.Decoder decoder = Base64.getDecoder();
	//========================================================== code to get the exclude URL functionality
	private String bodyManipulationFilterEnabled = "true";
	private static final String EXCLUDED_URI="excluded.uris";

	private List<AntPathRequestMatcher> excludedUriList = new ArrayList<>();


	public List<AntPathRequestMatcher> getExcludedUriList() {
		return excludedUriList;
	}

	public void setExcludedUriList(List<AntPathRequestMatcher> excludedUriList) {
		this.excludedUriList = excludedUriList;
	}


	private boolean isExcludedUri(HttpServletRequest request) {
		for(AntPathRequestMatcher antPathRequestMatcher:excludedUriList)
		{
			if(antPathRequestMatcher.matches((HttpServletRequest) request))
			{
				return true;
			}
		}
		return false;
	}


	private void parseAndCreateRegex(String excludedUris) {
		FilterExcludedUrlHolder filterExcludedUrlHolder = NeutrinoSpringAppContextUtil
				.getBeanByName("filterExcludedUrlHolder", FilterExcludedUrlHolder.class);

		addAllUrlToList(filterExcludedUrlHolder.getExcludeForAllFiltersList());
		Map<String, Set<String>> excludedUriMap=filterExcludedUrlHolder.getExcludedUrlMap();
		if(ValidatorUtils.hasAnyEntry(excludedUriMap))
		{
			Set<String> excludedUrisSet=excludedUriMap.get(this.getClass().getSimpleName());
			addAllUrlToList(excludedUrisSet);
		}

		if(StringUtils.isNoneEmpty(excludedUris))
		{
			String[] excludedUrisList=excludedUris.split(COMMA);
			for(String uri:excludedUrisList)
			{
				excludedUriList.add(new AntPathRequestMatcher(uri));
			}
		}
	}

	private void addAllUrlToList(Set<String> excludeForAllFiltersList) {
		if(ValidatorUtils.hasNoElements(excludeForAllFiltersList))
		{
			return;
		}
		for(String uri:excludeForAllFiltersList)
		{
			excludedUriList.add(new AntPathRequestMatcher(uri));
		}
	}
	//=========================================================================================

	private String getDecryptedData(String key, String data){
		BaseLoggers.securityLogger.debug("Going to decrypt :"+data);
		Key aesKey = new SecretKeySpec((key+SALT).getBytes(), AES);
		String decrypted = null;
		try 
		{
			byte[] tmp = decoder.decode(data);
			Cipher cipher = Cipher.getInstance(AES);
			// encrypt the text
			cipher.init(Cipher.DECRYPT_MODE, aesKey);
			decrypted = new String(cipher.doFinal(tmp));
			BaseLoggers.securityLogger.debug("Decrypted data :"+decrypted);
		}
		catch(Exception e) 
		{
			System.err.println("Exception while decryption of parameters "+e.getMessage());
			
			throw new XssException(LOG_ERROR);
		}
		BaseLoggers.securityLogger.debug("Going to return decrypted data as  :"+decrypted.substring(1, decrypted.length()-1));
		return decrypted.substring(1, decrypted.length()-1);
	}

	private void checkParamTampering(HttpServletRequest req, String dec) {
		String sessionID = req.getSession().getId();
		BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_GOT_DECRYPTED_NAME_VALUE,dec));
		String[] nameValuePair;
		String[] tmpVal;
		nameValuePair = dec.trim().split(DELIMETER);
		tmpVal = req.getParameterValues(nameValuePair[0]);

		BaseLoggers.securityLogger.trace(generateLogLine(LOG_SESSION_ID,sessionID,LOG_NAME_VALUE_PAIR,nameValuePair[0],nameValuePair[1]));
		if(tmpVal!=null  && tmpVal.length>1){// when tmpVal is actually array : [1,2,3] for multiselect tag
			String[] expectedValue  = nameValuePair[1].split(COMMA);
			Arrays.sort(expectedValue);
			Arrays.sort(tmpVal);
			if(Arrays.equals(tmpVal, expectedValue)){
				return;
			}
			else{
				BaseLoggers.securityLogger.error(generateLogLine(LOG_SESSION_ID,sessionID,LOG_TAMPERING_DONE_FOR,nameValuePair[0],SPACE,tmpVal[0]));
				throw new XssException(LOG_ERROR);
			}
		}
		else if(tmpVal!=null  && tmpVal[0].contains(COMMA)){// when tmpVal has one value and that is separated by comma: 1,2,3
//              for multiItemSelect tag
			String[] expectedValue  = nameValuePair[1].split(COMMA);
			String[] actualValue = tmpVal[0].split(COMMA);
			Arrays.sort(expectedValue);
			Arrays.sort(actualValue);

			if(Arrays.equals(actualValue, expectedValue)){
				return;
			}
			else{
				BaseLoggers.securityLogger.error(generateLogLine(LOG_SESSION_ID,sessionID,LOG_TAMPERING_DONE_FOR,nameValuePair[0],SPACE,tmpVal[0]));
				throw new XssException(LOG_ERROR);
			}
		}
		else if(tmpVal!=null  && !tmpVal[0].equals(nameValuePair[1])){// when tmpVal has one value the single value : 1 for input tag
			BaseLoggers.securityLogger.error(generateLogLine(LOG_SESSION_ID,sessionID,LOG_TAMPERING_DONE_FOR,nameValuePair[0],SPACE,tmpVal[0]));
			throw new XssException(LOG_ERROR);
		}
	}


	public void initFilter(FilterConfig filterConfig) throws ServletException {
		NeutrinoBodyManipulationFilterConfig neutrinoBMFilterConfig = NeutrinoSpringAppContextUtil
				.getBeanByType(NeutrinoBodyManipulationFilterConfig.class);

				String filterEnabled = neutrinoBMFilterConfig.getBodyManipulationFilterEnabled()+"";

		BaseLoggers.securityLogger.debug("body Manipulation filterEnabled :"+filterEnabled);		
		this.bodyManipulationFilterEnabled=filterEnabled;
	}



	private void doValidateBodyManipulation(HttpServletRequest req){
		String sessionID = req.getSession().getId();
		long t1 = System.nanoTime();
		String unmodifiableDataKey = (String)req.getSession().getAttribute(UN_MODIFIABLE_KEY);

		if(unmodifiableDataKey==null){
			unmodifiableDataKey = RandomStringUtils.randomAlphanumeric(12);
			BaseLoggers.securityLogger.info(generateLogLine(LOG_SESSION_ID,sessionID,LOG_UN_MODIFIABLE_KEY,unmodifiableDataKey,LOG_FOR_URI,req.getRequestURI()));
			req.getSession().setAttribute(UN_MODIFIABLE_KEY, unmodifiableDataKey);
			addEmptyToken(req, unmodifiableDataKey);
			return;
		}
		String unmodifiableDataValuesFromHeader = req.getHeader(unmodifiableDataKey);
		BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_VALUES_FROM_HEADER,unmodifiableDataValuesFromHeader));
		String[] unmodifiableDataValues = unmodifiableDataValuesFromHeader==null?req.getParameterValues(unmodifiableDataKey):unmodifiableDataValuesFromHeader.split(",");
//		String[] unmodifiableDataValues = req.getParameterValues(unmodifiableDataKey);
		if(!req.getMethod().equals(HttpMethod.GET)){// for urls which we can ignore JUST FOR POC

			if(unmodifiableDataValues==null || unmodifiableDataValues.length==0){
				String er = unmodifiableDataValues==null?" DATA IS NULL":" DATA LENGTH IS ZERO";
				BaseLoggers.securityLogger.error(generateLogLine(LOG_SESSION_ID,sessionID,er,LOG_FOR_URI,req.getRequestURI()));
				throw new XssException(LOG_ERROR);
			}
			BaseLoggers.securityLogger.trace(generateLogLine(LOG_SESSION_ID,sessionID,LOG_GOT_NOT_NULL_DATA,req.getRequestURI()));
			String key = (String)req.getSession().getAttribute(PASS_PHRASE);
			StringBuilder sb = new StringBuilder();

			try {
				
				Stream.of(unmodifiableDataValues)
				.filter(data -> data!=null && !data.trim().isEmpty())
				.map(data -> getDecryptedData(key, data))
				.filter(decryptedData -> !decryptedData.trim().equals(UNMODIFIABLE_PROPERTY_PARAMETER_NOTAPPLICABLE))
				.forEach(decryptedData ->sb.append(decryptedData).append(COMMA_SPACE));
				
				BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_TOTAL_TIME_FOR_1ST_LOOP,(System.nanoTime()-t1)+SPACE));
				
				if(sb.length()==0){
					BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_NO_PARAM_TO_PROTECT));
					addEmptyToken(req, unmodifiableDataKey);
					BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_TOTAL_TIME,(System.nanoTime()-t1)+SPACE));
					return;
				}
					
				BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_DECRYPTED_DATA,sb.toString()));
				
				Stream.of(sb.toString().substring(0, sb.length()-2).split(COMMA_SPACE))
				.parallel()
				.filter(decryptedData -> !decryptedData.trim().equals(UNMODIFIABLE_PROPERTY_PARAMETER_NOTAPPLICABLE))
				.forEach(dec -> checkParamTampering(req, dec.trim()));
				
				
				BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_TOTAL_TIME_FOR_2ND_LOOP,(System.nanoTime()-t1)+SPACE));
			} catch (Exception e) {
				BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_TOTAL_TIME,(System.nanoTime()-t1)+SPACE));
				
				throw new XssException(LOG_ERROR);
			}

		}else{
			BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_DIDNOT_GOT_VALID_TOKEN,req.getRequestURI()));
		}
		addEmptyToken(req, unmodifiableDataKey);
		BaseLoggers.securityLogger.debug(generateLogLine(LOG_SESSION_ID,sessionID,LOG_TOTAL_TIME,(System.nanoTime()-t1)+SPACE));

	}

	private void addEmptyToken(HttpServletRequest req, String unmodifiableDataKey) {
		List<String> UNMODIFIABLE_PROPERTY_PARAMETER_INIT_LIST = new ArrayList<>();
		UNMODIFIABLE_PROPERTY_PARAMETER_INIT_LIST.add(UNMODIFIABLE_PROPERTY_PARAMETER_NOTAPPLICABLE);
		req.setAttribute(unmodifiableDataKey, UNMODIFIABLE_PROPERTY_PARAMETER_INIT_LIST);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String excludedUris=filterConfig.getInitParameter(EXCLUDED_URI);
		parseAndCreateRegex(excludedUris);
		initFilter(filterConfig);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (!isExcludedUri((HttpServletRequest)request)) {
			BaseLoggers.securityLogger.debug("SESSION ID :"+((HttpServletRequest)request).getSession().getId()+" Got Valid URI to execute BM FILTER :"+((HttpServletRequest)request).getRequestURI());
			if (this.bodyManipulationFilterEnabled.trim().equalsIgnoreCase("true")) {
				doValidateBodyManipulation((HttpServletRequest) request);
			}

		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
	
	private String generateLogLine(String... vars){
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < vars.length; i++) {
			sb.append(vars[i]);
		}
		return sb.toString();
	}
}
