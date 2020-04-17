package com.nucleus.web.security;

import static com.nucleus.web.security.AesUtil.PASS_PHRASE;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.ELRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.UriUtils;

import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserInfo;
import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;

import net.bull.javamelody.MonitoredWithSpring;

public class RestCustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private ImageCaptchaService captchaService;

    @Inject
    @Named(value = "systemSetupUtil")
    private SystemSetupUtil     systemSetupUtil;

    private boolean             captchaEnabled;

    @Inject
    @Named("organizationService")
    OrganizationService         organizationService;

    // possible values : "Y" or "N"
    @Value(value = "#{'${core.web.config.webClientToEncryptpwd}'}")
    private String              webClientToEncryptpwd;

    private RequestMatcher      requestMatcher                   = new ELRequestMatcher(
                                                                         "hasHeader('X-Requested-With','XMLHttpRequest')");

    // ~ Static fields/initializers
    // =====================================================================================
    private static final String SPRING_SECURITY_FORM_CAPTCHA_KEY = "j_captcha";

    // ~ Constructors
    // ===================================================================================================

    public RestCustomUsernamePasswordAuthenticationFilter() {
        super();
    }

    // ~ Methods
    // ========================================================================================================
    @Override
    @MonitoredWithSpring(name = "CUPAF_VERIFY_AUTHENTICATION")
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!systemSetupUtil.isSystemSetup()) {
            setCaptchaEnabled(false);
        }

        if (!validateCaptcha(obtainChallengeId(request), obtainCaptcha(request))) {
            throw new AuthenticationServiceException("CaptchaValidationFailed");
        }

        /** Change for validating that the login time is within the branch operating Time STARTS **/
        Authentication authentication = super.attemptAuthentication(request, response);

        if (authentication.getPrincipal() != null) {
            UserInfo userInfo = null;
            if (UserInfo.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
                userInfo = (UserInfo) authentication.getPrincipal();
                if (userInfo != null && userInfo.getLoggedInBranch() != null) {
                    Boolean result = organizationService.getUserLoginTimeValid(userInfo);

                    if (!result) {
                        throw new AuthenticationServiceException("branchTimeValidationFailed");
                    }

                }
            }
        }
        /** Change for validating that the login time is within the branch operating Time ENDS **/
        return authentication;
    }

    protected String obtainCaptcha(HttpServletRequest request) {
        return request.getParameter(SPRING_SECURITY_FORM_CAPTCHA_KEY);
    }

    protected String obtainChallengeId(HttpServletRequest request) {
        return request.getSession().getId();
    }

    protected boolean validateCaptcha(String challengeId, String captcha) {
        if (!captchaEnabled) {
            return true;
        }
        try {
            if (StringUtils.isNotEmpty(challengeId) && StringUtils.isNotEmpty(captcha)) {
                return captchaService.validateResponseForID(challengeId, captcha);
            } else {
                return false;
            }
        } catch (CaptchaServiceException e) {
            throw new AuthenticationServiceException("CaptchaServiceException occurred : " + e.getMessage(), e);
        }
    }

    /**
     * @param captchaEnabled the captchaEnabled to set
     */
    public void setCaptchaEnabled(boolean captchaEnabled) {
        this.captchaEnabled = captchaEnabled;
    }

    @Override
    protected String obtainPassword(HttpServletRequest request) {
        String providedpwd = super.obtainPassword(request);
        String decryptedPwd = providedpwd;
        String docodedpswd = "";
        if ("Y".equalsIgnoreCase(webClientToEncryptpwd)) {
            try {
                docodedpswd = UriUtils.decode(decryptedPwd, "UTF-8");
            } catch (Exception e) {
                BaseLoggers.securityLogger.error("exception during decoding the supplied password from UI", e);
            }
            String passPhraseVal = (String) request.getHeader(PASS_PHRASE);
            if (passPhraseVal != null) {
                decryptedPwd = AesUtil.Decrypt(docodedpswd, passPhraseVal);
            }

        }
        return decryptedPwd;
    }

    public RequestMatcher getRequestMatcher() {
        return requestMatcher;
    }

    public void setRequestMatcher(RequestMatcher requestMatcher) {
        this.requestMatcher = requestMatcher;
    }

    protected boolean isRpcRequest(HttpServletRequest request, HttpServletResponse response) {
        return requestMatcher.matches(request);

    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        // return isRpcRequest(request, response);
        return true;
    }

}
