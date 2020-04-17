package com.nucleus.token;

import javax.servlet.http.HttpServletRequest;

import com.nucleus.authenticationToken.TokenDetails;
import com.nucleus.user.User;

public interface CustomLoginTokenHandlingService {

    public String generateCustomLoginToken(String username);

    public String getRequestParamForToken();

    public User getUserAssociatedWithLoginToken(HttpServletRequest request);

    public TokenDetails getOTPTokenDetails(String otp);
}
