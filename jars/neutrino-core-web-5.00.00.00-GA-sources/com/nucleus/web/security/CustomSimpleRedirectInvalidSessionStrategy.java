package com.nucleus.web.security;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

public class CustomSimpleRedirectInvalidSessionStrategy implements
		InvalidSessionStrategy {
	private final Log logger = LogFactory.getLog(getClass());

	@Inject
	@Named(value = "systemSetupUtil")
	private SystemSetupUtil systemSetupUtil;

	private String destinationUrl;
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	private boolean createNewSession = true;

	@Override
	public void onInvalidSessionDetected(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		this.destinationUrl = systemSetupUtil
				.getInvalidOrExpiredSessionUrl();

		Assert.isTrue(UrlUtils.isValidRedirectUrl(destinationUrl),
				"url must start with '/' or with 'http(s)'");
		logger.debug("Starting new session (if required) and redirecting to '"
				+ destinationUrl + "'");
		if (createNewSession) {
			request.getSession();
		}
		redirectStrategy.sendRedirect(request, response, destinationUrl);
	}

	public void setDestinationUrl(String destinationUrl) {
		Assert.isTrue(UrlUtils.isValidRedirectUrl(destinationUrl),
				"url must start with '/' or with 'http(s)'");
		this.destinationUrl = destinationUrl;
	}

	/**
	 * Determines whether a new session should be created before redirecting (to
	 * avoid possible looping issues where the same session ID is sent with the
	 * redirected request). Alternatively, ensure that the configured URL does
	 * not pass through the {@code SessionManagementFilter}.
	 * 
	 * @param createNewSession
	 *            defaults to {@code true}.
	 */
	public void setCreateNewSession(boolean createNewSession) {
		this.createNewSession = createNewSession;
	}

	public RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}

	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}

	
}
