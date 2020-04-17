package com.nucleus.web.security;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nucleus.logging.BaseLoggers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.web.csrf.CSRFTokenManager;
import com.nucleus.web.security.frameoptions.NeutrinoXFrameOptionsHeaderWriter;
/**
 * 
 * Enables filter to exclude urls from being filtered.
 * Excluded URL can be configured in two ways.
 * <br>1. In Web.xml - configure init-param excluded.uris 
 * <br>2. create Bean having excludedUrlFilterPluginBeanPostProcessor as parent 
 * <br><code>&lt;bean parent="excludedUrlFilterPluginBeanPostProcessor">
 * <br>		&nbsp;&nbsp;&nbsp;&lt;property name="pluginBeanName" value="coreFilterUrlExclusionMap" />
 * <br>&lt;/bean></code>
 * <br>Bean coreFilterUrlExclusionMap represents a Map of Map<String, Set<String>>
 * <br>where key is FilterClass simple name and value is List of url to exclude from that filter.
 * 
 * @author gajendra.jatav
 *
 */
public abstract class NeutrinoUrlExcludableFilter implements Filter{

	private static final String EXCLUDED_URI="excluded.uris";
	
	private List<AntPathRequestMatcher> excludedUriList = new ArrayList<>();

	public static final String XFRAME_OPTIONS_HEADER = "X-Frame-Options";
	
	public static final String SAMEORIGIN = "SAMEORIGIN-XS";
	
	
	public List<AntPathRequestMatcher> getExcludedUriList() {
		return excludedUriList;
	}

	public void setExcludedUriList(List<AntPathRequestMatcher> excludedUriList) {
		this.excludedUriList = excludedUriList;
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
		
		((HttpServletResponse) response).setHeader(NeutrinoXFrameOptionsHeaderWriter.XFRAME_OPTIONS_HEADER,
				NeutrinoXFrameOptionsHeaderWriter.SAMEORIGIN);

		NeutrinoResponseWrapper neutrinoResponseWrapper;
		if(NeutrinoResponseWrapper.class.isAssignableFrom(response.getClass()))
    	{
			neutrinoResponseWrapper=(NeutrinoResponseWrapper) response;    	
		}
		else
		{
			 neutrinoResponseWrapper =new NeutrinoResponseWrapper((HttpServletResponse) response);
			 String csrfToken=CSRFTokenManager.getTokenForSession((HttpServletRequest) request);
			 neutrinoResponseWrapper.setCsrfToken(csrfToken);
		}
		if(isExcludedUri((HttpServletRequest) request))
		{
			chain.doFilter(request, neutrinoResponseWrapper);
		}
		else
		{
			filter(request, neutrinoResponseWrapper, chain);			
		}


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

	@Override
	public void destroy() {
		doDestroy();
	}

    /**
     * Called by the web container to indicate to a filter that it is being placed into
     * service. The servlet container calls the init method exactly once after instantiating the
     * filter. The init method must complete successfully before the filter is asked to do any
     * filtering work. <br><br>
     * The web container cannot place the filter into service if the init method either<br>
     * 1.Throws a ServletException <br>
     * 2.Does not return within a time period defined by the web container
     */

	public abstract void initFilter(FilterConfig filterConfig) throws ServletException ;

	 /**
     * The <code>doFilter</code> method of the Filter is called by the container
     * each time a request/response pair is passed through the chain due
     * to a client request for a resource at the end of the chain. The FilterChain passed in to this
     * method allows the Filter to pass on the request and response to the next entity in the
     * chain.<p>
     * A typical implementation of this method would follow the following pattern:- <br>
     * 1. Examine the request<br>
     * 2. Optionally wrap the request object with a custom implementation to
     * filter content or headers for input filtering <br>
     * 3. Optionally wrap the response object with a custom implementation to
     * filter content or headers for output filtering <br>
     * 4. a) <strong>Either</strong> invoke the next entity in the chain using the FilterChain object (<code>chain.doFilter()</code>), <br>
     * 4. b) <strong>or</strong> not pass on the request/response pair to the next entity in the filter chain to block the request processing<br>
     * 5. Directly set headers on the response after invocation of the next entity in ther filter chain.
     */
	public abstract void filter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException;


	 /**
     * Called by the web container to indicate to a filter that it is being taken out of service. This
     * method is only called once all threads within the filter's doFilter method have exited or after
     * a timeout period has passed. After the web container calls this method, it will not call the
     * doFilter method again on this instance of the filter. <br><br>
     *
     * This method gives the filter an opportunity to clean up any resources that are being held (for
     * example, memory, file handles, threads) and make sure that any persistent state is synchronized
     * with the filter's current state in memory.
     */
	public abstract void doDestroy();
	
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
			String[] excludedUrisList=excludedUris.split(",");
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
	
	
	
	 /**
	 * Called by the filter method of the implemented class 
     * It will validate the URL as per the enabledFilterValue 
     * If the enabledFilterValue is true ,SSO is active and the referer URL contains the SSO ticket Validator URL,
     * it will not validate ; otherwise it do validation 
     *  
     */
	public boolean isUrlValidationRequired(boolean enabledFilterValue,boolean isSSOActive,
										HttpServletRequest httpServletRequest,String ssoTicketValidatorUrl){
		boolean urlValidationRequired =enabledFilterValue;
		if(isSSOActive){
			String refererUrl=httpServletRequest.getHeader("referer");
			try {

				boolean referredBySSO=refererUrl!=null?isValidRefererHeader(refererUrl, ssoTicketValidatorUrl):false;
				urlValidationRequired = urlValidationRequired&&(!referredBySSO);
			} catch (URISyntaxException e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
			}
			
			
			
		}
		return urlValidationRequired;
	}
	
	
	private boolean isValidRefererHeader(String refererUrl, String ssoTicketValidatorUrl) throws URISyntaxException{
		URI ssoUri = new URI(ssoTicketValidatorUrl);
		URI refererUri = new URI(refererUrl);
		boolean isSameHostAndPort = StringUtils.equals(refererUri.getAuthority(), ssoUri.getAuthority());
		boolean isSameProtocol = StringUtils.equals(refererUri.getScheme(), ssoUri.getScheme());
		
		return isSameHostAndPort && isSameProtocol;
	}
}
