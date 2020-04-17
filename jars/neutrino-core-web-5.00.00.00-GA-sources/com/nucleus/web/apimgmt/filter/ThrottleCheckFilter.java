package com.nucleus.web.apimgmt.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.nucleus.security.oauth.apim.ThrottleQuotaManagementService;
import com.nucleus.security.oauth.apim.ThrottlingPolicy;
import com.nucleus.security.oauth.dao.TrustedSourceDao;

public class ThrottleCheckFilter implements Filter {

	
	@Inject
	@Named("throttleQuotaManagementService")
	private ThrottleQuotaManagementService throttleQuotaManagementService;
	
	@Inject
	@Named("trustedSourceDao")
	private TrustedSourceDao trustedSourceDao;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//Required implementation : Hence empty.
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		/*
		 * 
		 * Load throttling data
		 */
	/*	String clientId = (String)request.getAttribute("clientId");
		if(clientId == null || "".equals(clientId)){
			throw new AccessDeniedException("Client ID Not Found!");
			
		}
		OauthClientDetails details = trustedSourceDao.loadUnproxiedClientByClientId(clientId);
		if(details == null){
			throw new AccessDeniedException("Client not found");
		}
		request.setAttribute("clientDetails", details);
		List<APIDetails> apiDetails = details.getMappedAPIs();
		for (APIDetails api : apiDetails) {
			if (((HttpServletRequest) request).getRequestURI().contains(api.getApiUri())) {
				
				Set<ThrottlingPolicy> policies = api.getPolicies();
				for (ThrottlingPolicy policy : policies) {
					if (policy.getMappedTrustedSource() == null) {
						if (policy.getIsGlobal()) {

							if (checkThrottle(policy, api.getApiUri(), request, null)) {
								continue;
							}
						} else
							throw new ThrottleCheckException("Invalid Throttling Policy Mapped. Please Check");
					}

					if (clientId.equals(policy.getMappedTrustedSource().getClientId())) {
						if (checkThrottle(policy, api.getApiUri(), request, clientId)) {
							continue;
						}

					}

				}

			}
		}

	 	*/
		chain.doFilter(request, response);

	}

	private boolean checkThrottle(ThrottlingPolicy policy, String apiURI, ServletRequest request, String clientID) {

		return true;
	}

	@Override
	public void destroy() {
		//Required implementation : Hence empty.
	}

}
