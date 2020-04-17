package com.nucleus.device;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserMobilityInfo;
import com.nucleus.user.UserService;
import com.nucleus.user.UserSessionManagerService;

/**
 * 
 * This interceptor is used to detect the device used to access the website for
 * all the secure urls. Please note: that if any unsecure link is added in
 * security context, then it has to be listed in excluded urls for this filter
 * in mvc context.
 * 
 */
@Component
public class DeviceDetectInterceptor extends HandlerInterceptorAdapter {

    @Inject
    @Named("userService")
    private UserService               userService;
    @Inject
    @Named("userSessionManagerService")
    private UserSessionManagerService userSessionManagerService;

    private final String              MOBILE_DEVICE    = "mobile";
    private final String              CHALLENGE_PASSED = "challengePassed";
    // private final String PASSED = "Passed";
    private final String              msg              = "Application accessed through mobile device";
    private final String              notAuthmsg       = "Not authorize to login through mobile";

    /**
     * Please note: that if any unsecure link is added in security context, then
     * it has to be listed in excluded urls for this filter in mvc context.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // user has not logged in
        if (userSessionManagerService.getLoggedinUserInfo() == null) {
            return true;
        }

        Device device = DeviceUtils.getCurrentDevice(request);
//        String deviceType = "unknown";
        // by pass the interceptor if device is normal
        if (device.isNormal() || device.isTablet()) {
//            deviceType = "";
//            request.getSession().setAttribute("deviceName", "");
            return true;
        } else { // user entered through a hand held device
            UserMobilityInfo userMobilityInfo = null;
            userMobilityInfo = userService.getUserMobilityInfo(userSessionManagerService.getLoggedinUserInfo().getId());
            if (userMobilityInfo != null && userMobilityInfo.getIsMobileEnabled()) { // Mobility is
                                                                                     // enabled
                if (!userMobilityInfo.getIsChallengeEnabled()
                        || (request.getSession().getAttribute(CHALLENGE_PASSED) != null && request.getSession()
                                .getAttribute(CHALLENGE_PASSED).equals(userMobilityInfo.getChallenge()))) {
//                    deviceType = MOBILE_DEVICE;
//                    request.getSession().setAttribute("deviceName", deviceType);
                    BaseLoggers.accessLogger.info(msg);
                    return true;
                } else // yet to authorize through a session
                {
                    if (!(request.getRequestURI().contains("/device/getChallenge") || request.getRequestURI().contains(
                            "/challengeSubmit"))) {

                        // ModelAndView mv = new
                        // ModelAndView(request.getContextPath() +
                        // "/app/device/getChallenge");
                        // ModelAndViewDefiningException mvde = new
                        // ModelAndViewDefiningException(mv);
                        // throw mvde;
                        response.sendRedirect(request.getContextPath() + "/app/device/getChallenge");
                        return false;
                    }
                    return true;
                }

            } else // user is not authorized for mobile view.
            {
                BaseLoggers.accessLogger.error(notAuthmsg);
                //request.setAttribute("error", notAuthmsg);
                response.sendRedirect(request.getContextPath() + "/app/device/logout?error="+notAuthmsg);
                return false;

            }
        }

    }

}
