package com.nucleus.web.license.agreement;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nucleus.user.User;
import com.nucleus.user.UserService;
import com.nucleus.web.security.SystemSetupUtil;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Controller
@RequestMapping(value = "/licenseAgreement")
public class LicenseAgreementController {
    @Inject
    @Named("userService")
    private UserService                      userService;

    @Inject
    @Named(value = "systemSetupUtil")
    private SystemSetupUtil                  systemSetupUtil;

    private static final String LICENSE_AGREEMENT_PAGE  = "licenseAgreement";

    private static final String AUTHENTICATION_SUCCESS  = "onAuthenticationSuccess";

    private static final String REDIRECT_KEY            = "redirect:";

    private static final String FORWARD_KEY             = "forward:";

    @RequestMapping(value = "/licenseAcceptanceOnLogin/{username}")
    public String licenseAcceptanceOnFirstLogin(@PathVariable("username") String username, ModelMap map,
            HttpServletRequest request) {
        map.put("username", username);
        User user = userService.findUserByUsername(username);
        boolean isUserAuthenticated = Boolean.FALSE;
        String redirectToLoginFormUrl = REDIRECT_KEY + systemSetupUtil.getLoginFormUrl();
        if (request.getSession().getAttribute(AUTHENTICATION_SUCCESS) != null) {
            isUserAuthenticated = Boolean.valueOf(request.getSession().getAttribute(AUTHENTICATION_SUCCESS).toString());
        }
        if (isUserAuthenticated && user != null && user.isForcePasswordResetOnLogin() && systemSetupUtil.isSystemDeployedOnCloud()) {
            return LICENSE_AGREEMENT_PAGE;
        } else {
            return redirectToLoginFormUrl;
        }
    }

    @RequestMapping(value = "/submitLicenseAcceptanceToUser/{username}")
    public String saveLicenseAcceptanceToUser(@PathVariable("username") String username,
            @RequestParam(value = "licenseAccepted", required = false) boolean licenseAccepted,
            HttpServletRequest request) {
        User user = userService.findUserByUsername(username);
        boolean isUserAuthenticated = Boolean.FALSE;
        String redirectToLoginFormUrl = REDIRECT_KEY + systemSetupUtil.getLoginFormUrl();
        String forwardToResetPasswordUrl = FORWARD_KEY + systemSetupUtil.getResetPasswordUrl();
        if (request.getSession().getAttribute(AUTHENTICATION_SUCCESS) != null) {
            isUserAuthenticated = Boolean.valueOf(request.getSession().getAttribute(AUTHENTICATION_SUCCESS).toString());
        }
        if (isUserAuthenticated && user != null && user.isForcePasswordResetOnLogin()) {
            if (licenseAccepted && systemSetupUtil.isSystemDeployedOnCloud()) {
                request.setAttribute("licenseAccepted", licenseAccepted);
                return forwardToResetPasswordUrl + "/" + username;
            } else {
                return redirectToLoginFormUrl;
            }
        } else {
            return redirectToLoginFormUrl;
        }
    }

}
