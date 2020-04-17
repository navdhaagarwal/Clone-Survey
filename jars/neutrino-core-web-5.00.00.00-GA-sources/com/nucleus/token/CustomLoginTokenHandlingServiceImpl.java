package com.nucleus.token;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import net.bull.javamelody.MonitoredWithSpring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.util.StringUtils;

import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.authenticationToken.TokenDetails;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.user.User;
import com.nucleus.user.UserService;
import com.nucleus.web.security.AbstractCustomTokenLoginServices;
import com.nucleus.web.security.InvalidLoginTokenException;

@Named("customLoginTokenHandlingService")
@MonitoredWithSpring(name = "customLoginTokenHandlingService_IMPL_")
public class CustomLoginTokenHandlingServiceImpl implements CustomLoginTokenHandlingService {

    @Autowired
    @Qualifier("tokenLoginServices")
    private AbstractCustomTokenLoginServices abstractCustomTokenLoginServices;

    @Inject
    @Named("userService")
    protected UserService                    userService;

    @Inject
    @Named("authenticationTokenService")
    protected AuthenticationTokenService     authenticationTokenService;

    @Override
    public String generateCustomLoginToken(String username) {
        NeutrinoValidator.notNull(username, "User name cann't be null");
        return abstractCustomTokenLoginServices.generateToken(username);
    }

    @Override
    public String getRequestParamForToken() {
        return abstractCustomTokenLoginServices.getRequestParamName();
    }

    @Override
    public User getUserAssociatedWithLoginToken(HttpServletRequest request) {
        NeutrinoValidator.notNull(request, "Request Object cannot be null");
        String loginToken = request.getParameter(getRequestParamForToken());
        if (loginToken != null) {
            String username = getUsernameFromLoginToken(loginToken);
            return userService.findUserByUsername(username);
        }
        return null;
    }

    private String getUsernameFromLoginToken(String loginToken) throws InvalidCookieException {
        if (!Base64.isBase64(loginToken.getBytes())) {
            throw new InvalidLoginTokenException("Login token was not Base64 encoded; value was '" + loginToken + "'");
        }

        String tokenAsPlainText = new String(Base64.decode(loginToken.getBytes()));
        String[] tokens = StringUtils.delimitedListToStringArray(tokenAsPlainText, ":");

        if (tokens.length == 3) {
            return tokens[0];
        }
        return null;
    }

    @Override
    public TokenDetails getOTPTokenDetails(String otp) {
        NeutrinoValidator.notNull(otp, "OTP cannot be null");
        return authenticationTokenService.getOTPTokenDetailsByTokenId(otp);
    }
}
