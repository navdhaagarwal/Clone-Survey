package com.nucleus.web.security;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.common.CommonConfigUtility;
import com.nucleus.web.csrf.CSRFTokenManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
/**
 * Added to secure against parameter manipulation
 * @author gajendra.jatav
 * 
 */
public class NeutrinoUrlValidatorFilter extends NeutrinoUrlExcludableFilter {

	public static final String SECURITY_TOKEN="_hkstd";
	public static final String SECURITY_TOKEN_REDIRECT="SECURE_REDIRECT_HKSTD";
	
	public static final String SECURITY_REDIRECT_QUERY_PARAM="&"+SECURITY_TOKEN_REDIRECT+"=redirect";
	
	private Boolean urlValidatorFilterEnabled;

	
	private String ssoTicketValidatorUrl;
	
	private boolean isParamEncryptionEnabled;
	
	private boolean isSSOActive;
	
	@Override
	public void initFilter(FilterConfig filterConfig) throws ServletException {
		NeutrinoUrlValidatorFilterConfig neutrinoUrlValidatorFilterConfig = NeutrinoSpringAppContextUtil
				.getBeanByType(NeutrinoUrlValidatorFilterConfig.class);
		Boolean filterEnabled=neutrinoUrlValidatorFilterConfig.getUrlValidatorFilterEnabled();
		isParamEncryptionEnabled=neutrinoUrlValidatorFilterConfig.getParamEncryptionEnabled();
		if(filterEnabled==null)
		{
			this.urlValidatorFilterEnabled=false;
				return;
		}
		this.urlValidatorFilterEnabled=filterEnabled;
		
		
		
		CommonConfigUtility commonConfigUtility = NeutrinoSpringAppContextUtil.getBeanByName("commonConfigUtility",CommonConfigUtility.class);
		if (commonConfigUtility.getSsoActive()) {
			this.isSSOActive=true;
			this.ssoTicketValidatorUrl =commonConfigUtility.getSsoTicketValidatorUrl();
		} else {
			this.isSSOActive=false;
			this.ssoTicketValidatorUrl ="";
		}
		
		
	}

	@Override
	public void filter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest httpServletRequest=(HttpServletRequest) request;
		boolean urlValidationRequired = isUrlValidationRequired(this.urlValidatorFilterEnabled,this.isSSOActive, 
																httpServletRequest,this.ssoTicketValidatorUrl);
		
		if(urlValidationRequired){
			doValidateSecurityToken(httpServletRequest,(HttpServletResponse)response);
		}
	
		chain.doFilter(httpServletRequest, response);

	}



	private boolean decryptionRequired(HttpServletRequest httpServletRequest) {
		return "POST".equalsIgnoreCase(httpServletRequest.getMethod());
	}

	@Override
	public void doDestroy() {
		// called when filter will be destroyed
	}


	private   void doValidateSecurityToken(HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException
	{
		String parameterHash=getParameterHash(httpServletRequest);
		String uriHash =  getUriHash(httpServletRequest);
		if(parameterHash!=null && uriHash!=null && MessageDigest.isEqual(parameterHash.getBytes(), uriHash.getBytes()) )
		{
			return;
		}
		else {
			response.setStatus(HttpStatus.FORBIDDEN.value());
			BaseLoggers.flowLogger.debug("Url: {}, clientSecurityToken: {}, serverSideToken: {}",
					getUrlWithQueryString(httpServletRequest), parameterHash, uriHash);
			response.sendRedirect(httpServletRequest.getContextPath() + "/app/webExceptionHandler/accessDenied");
			throw new XssException("Url security token did not match, request parameter might be changed in between ");
		}
	}

	private static String getParameterHash(HttpServletRequest request) {

		String parameterHash = request.getHeader(SECURITY_TOKEN);
		if (parameterHash == null) {
			parameterHash = request.getParameter(SECURITY_TOKEN);
		}
		if (parameterHash != null) {
			parameterHash = parameterHash.substring(0, 32);
		}

		return parameterHash;
	}

	private static  String removeParams(String queryString, String param) {
        String keyValue = param + "=[^&]*?";
	    return queryString.replaceAll("(&" + keyValue + "(?=(&|$))|^" + keyValue + "(&|$))", "");
	}

	private static String getUriHash(HttpServletRequest request) {
		String csrfToken=CSRFTokenManager.getTokenForSession(request);
		return getHashValueOfUrl(getUrlWithQueryString(request),csrfToken);
	}
	
	private static String getUrlWithQueryString(HttpServletRequest request)
	{
		StringBuilder url=new StringBuilder(request.getRequestURI());
		
		if(StringUtils.isNoneEmpty(request.getQueryString()))
		{
			String queryString=removeParams(request.getQueryString(),SECURITY_TOKEN);
			if(request.getParameter(SECURITY_TOKEN_REDIRECT)!=null){//This is to identify the redirect case as in case of redirect security token is calculated without endocoding.

                try {

                       //queryString=UriUtils.decode(queryString.replace(SECURITY_REDIRECT_QUERY_PARAM, ""),"UTF-8");

                	queryString=UriUtils.decode(removeParams(queryString,SECURITY_TOKEN_REDIRECT),"UTF-8");
                } catch (Exception e) {     

                       BaseLoggers.flowLogger.error("Exception occurred while calculating {} for query {} {}",SECURITY_TOKEN,queryString,e);

                       throw new SystemException(e);

                }

          }


			if(StringUtils.isNoneEmpty(queryString))
			{
				url.append("?"+queryString);
			}
		}
		return url.toString();
	}
	
	public static String getHashValueOfUrl(String url, String csrfToken)
	{
		return DigestUtils.md5Hex(url+csrfToken);
	}

}
