package com.nucleus.web.oauth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import com.nucleus.logging.BaseLoggers;

/**
 * @PDDEV-16807 
 * For each service request via oauth channel it uses the ThreadLocalSecurityContextHolderStrategy 
 * which interacts with SecurityContextPersistenceFilter.
 * Refer detailed input @ 
 * https://stackoverflow.com/questions/6408007/spring-securitys-securitycontextholder-session-or-request-bound
 * The user principal (element of SecurityContext) is stored in the HTTP Session. 
 * And for each request it is put in a thread local.
 * So for each request it is putting the OAuth2Authentication object is 
 * session which was resulting in OOM and generating the heap dump,
 * when neo is accessed from any mobility servers like mServe.
 * Purpose of this filter to invalidate the created session once the 
 * request is processed.
 * (see
 * {@link org.springframework.security.web.contex.SecurityContextPersistenceFilter}
 * {@link org.springframework.security.web.context.HttpSessionSecurityContextRepository}).
 *
 * @author Rohit Singh
 * @since 04.05.2018
 */
public class OAuthSessionDestroyFilter implements Filter{
	
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Do nothing 
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		
		chain.doFilter(req, res);
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpSession session = request.getSession();

		if (session!=null) {
			String sessionId = session.getId();
			session.invalidate();
			BaseLoggers.flowLogger.debug("OAuthSessionDestroyFilter :: Session destroyed with session id "+sessionId  );
		}	
	}

	@Override
	public void destroy() {
		// Do nothing 
	}

}
