package com.nucleus.web.security;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.nucleus.web.csrf.CSRFTokenManager;

public class CustomLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

	

    public CustomLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
		super(loginFormUrl);
	}

	@Inject
    @Named(value = "systemSetupUtil")
    private SystemSetupUtil systemSetupUtil;

    @Override
    public void afterPropertiesSet() throws Exception {

        String loginFormUrl = systemSetupUtil.getLoginFormUrl();
        String loginFormUrlForSetup = systemSetupUtil.getLoginFormUrlForSetup();
        // VALIDATE BOTH URLS
        Assert.isTrue(StringUtils.hasText(loginFormUrl) && UrlUtils.isValidRedirectUrl(loginFormUrl),
                "loginFormUrl must be specified and must be a valid redirect URL");
        Assert.isTrue(StringUtils.hasText(loginFormUrlForSetup) && UrlUtils.isValidRedirectUrl(loginFormUrlForSetup),
                "loginFormUrl must be specified and must be a valid redirect URL");
        if (isUseForward() && UrlUtils.isAbsoluteUrl(loginFormUrl)) {
            throw new IllegalArgumentException("useForward must be false if using an absolute loginFormURL");
        }
        if (isUseForward() && UrlUtils.isAbsoluteUrl(loginFormUrlForSetup)) {
            throw new IllegalArgumentException("useForward must be false if using an absolute loginFormURL");
        }
        Assert.notNull(getPortMapper(), "portMapper must be specified");
        Assert.notNull(getPortResolver(), "portResolver must be specified");

    }

    @Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
    	
		NeutrinoResponseWrapper neutrinoResponseWrapper;
		if(NeutrinoResponseWrapper.class.isAssignableFrom(response.getClass()))
    	{
			neutrinoResponseWrapper=(NeutrinoResponseWrapper) response;    	
		}
		else
		{
			 neutrinoResponseWrapper =new NeutrinoResponseWrapper(response);
		}
		String csrfToken=CSRFTokenManager.getTokenForSession(request);
		neutrinoResponseWrapper.setCsrfToken(csrfToken);
		super.commence(request, neutrinoResponseWrapper, authException);
	}

	@Override
    public String getLoginFormUrl() {

        return systemSetupUtil.getAuthenticationEntryPointLoginFormUrl();
    }

}
