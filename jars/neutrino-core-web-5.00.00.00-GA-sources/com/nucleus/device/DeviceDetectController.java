package com.nucleus.device;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserMobilityInfo;
import com.nucleus.user.UserService;
import com.nucleus.user.UserSessionManagerService;

@Controller
@RequestMapping("/device")
public class DeviceDetectController {
    @Inject
    @Named("userSessionManagerService")
    private UserSessionManagerService userSessionManagerService;

    @Inject
    @Named("userService")
    private UserService               userService;
    

    private static final String CHALLENGE_PASSED = "challengePassed";

    private static final String msg = "Application accessed through mobile device";
    
    @RequestMapping("/getChallenge")
    public String getChallenge()
    {
        return "getChallenge";
    }
    
    @RequestMapping("/challengeSubmit")
    public String challengeSubmit(@RequestParam("challenge") String challenge, HttpServletRequest request, HttpServletResponse response,ModelMap map) {
        
        UserMobilityInfo userMobilityInfo = userService.getUserMobilityInfo(userSessionManagerService
                .getLoggedinUserInfo().getId());
        if(userMobilityInfo!=null && userMobilityInfo.getChallenge().intValue()==Integer.parseInt(challenge))
            {
                BaseLoggers.accessLogger.info(msg);
                request.getSession().setAttribute(CHALLENGE_PASSED, Integer.parseInt(challenge));
                request.getSession().setAttribute("deviceName", "mobile");
//                response.sendRedirect(request.getContextPath()+"/app/dashboard");
                return "redirect:/app/dashboard";
            }
        else
            {
                map.put("error","Incorrect Challenge");
                return "getChallenge";
            }
    }
    
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String getLogoutPage(@RequestParam("error") String errmsg,ModelMap model) {
        BaseLoggers.flowLogger.debug("Received request to show Logout page");
        userSessionManagerService.invalidateCurrentLoggedinUserSession();
        model.put("logout", "logout");
        model.put("loginPage", "loginPage");
        model.put("error",errmsg);
        return "logout";
    }
        
}
