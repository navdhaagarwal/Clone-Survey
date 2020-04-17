/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.util.StringUtils;

import com.nucleus.authenticationToken.AuthenticationTokenConstants;
import com.nucleus.authenticationToken.TokenDetails;
import com.nucleus.user.UserInfo;

/**
 * 
 * @author Nucleus Software Exports Limited
 *
 */
public class NonPersitentCustomTokenLoginServices extends AbstractCustomTokenLoginServices {

    public NonPersitentCustomTokenLoginServices(String key, UserDetailsService userDetailsService, int tokenValiditySeconds,
            String requestParamName) {
        super(key, userDetailsService);
        setRequestParamName(requestParamName);
        setTokenValiditySeconds(tokenValiditySeconds);
    }

    @Override
    protected UserDetails processAutoLoginToken(String[] loginTokenParts, HttpServletRequest request,
            HttpServletResponse response) {

        if (loginTokenParts.length != 3) {
            throw new InvalidLoginTokenException("Auto login token did not contain 3" + " tokens, but contained '"
                    + Arrays.asList(loginTokenParts) + "'");
        }

        long tokenExpiryTime;

        try {
            tokenExpiryTime =Long.parseLong(loginTokenParts[1]);
        } catch (NumberFormatException nfe) {
            throw new InvalidLoginTokenException("Auto Login token[1] did not contain a valid number (contained '"
                    + loginTokenParts[1] + "')");
        }

        if (isTokenExpired(tokenExpiryTime)) {
            throw new InvalidLoginTokenException("Auto Login token[1] has expired (expired on '" + new Date(tokenExpiryTime)
                    + "'; current time is '" + new Date() + "')");
        }

        UserDetails userDetails = getUserDetailsService().loadUserByUsername(loginTokenParts[0]);

        if (userDetails == null) {
            throw new InvalidLoginTokenException("Invalid token as no user found for this token: Corrupted Token");
        }

        String expectedTokenSignature = makeTokenSignature(tokenExpiryTime, userDetails.getUsername(),
                userDetails.getPassword());

        if (!checkEquals(expectedTokenSignature, loginTokenParts[2])) {
            throw new InvalidLoginTokenException("Login token[2] contained signature '" + loginTokenParts[2]
                    + "' but expected '" + expectedTokenSignature + "'");
        }

        return userDetails;
    }

    @Override
    public void processCustomTokenAndOTP(UserDetails user, TokenDetails otpDetails) {
        if (otpDetails.getStatus().equals(AuthenticationTokenConstants.INVALID_TOKEN)) {
            throw new InvalidLoginTokenException("Invalid One Time Password");
        } else if (otpDetails.getStatus().equals(AuthenticationTokenConstants.PAGE_EXPIRED)) {
            throw new InvalidLoginTokenException("Expired One Time Password");
        } else {
            UserInfo userInfo = userService.getUserById(otpDetails.getUserId());
            if (userInfo != null && userInfo.getUsername() != null && !(userInfo.getUsername().equals(user.getUsername()))) {
                throw new InvalidLoginTokenException("Invalid Login Token Exception");
            }
        }
    }

    @Override
    public String generateToken(String username) {
        String encodedToken = null;

        // if no username then abort and return null
        if (!StringUtils.hasLength(username)) {
            logger.debug("Unable to retrieve username");
            return encodedToken;
        }

        UserDetails user = getUserDetailsService().loadUserByUsername(username);
        String password = user.getPassword();

        if (!StringUtils.hasLength(password)) {
            logger.debug("Unable to obtain password for user: " + username);
            return encodedToken;
        }

        int tokenLifetime = calculateLoginLifetime();
        long expiryTime = System.currentTimeMillis();

        expiryTime += 1000L * (tokenLifetime < 0 ? TWO_WEEKS_S : tokenLifetime);

        String signatureValue = makeTokenSignature(expiryTime, username, password);

        encodedToken = encodeToken(new String[] { username, Long.toString(expiryTime), signatureValue });

        if (logger.isDebugEnabled()) {
            logger.debug("Added login token for user '" + username + "', expiry: '" + new Date(expiryTime) + "'");
        }
        return encodedToken;
    }

    /**
     * Calculates the digital signature to be sent to be used as token. Default value is
     * MD5 ("username:tokenExpiryTime:password:key")
     */
    protected String makeTokenSignature(long tokenExpiryTime, String username, String password) {
        String data = username + ":" + tokenExpiryTime + ":" + password + ":" + getKey();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No SHA-256 algorithm available!");
        }

        return new String(Hex.encode(digest.digest(data.getBytes())));
    }

    protected boolean isTokenExpired(long tokenExpiryTime) {
        return tokenExpiryTime < System.currentTimeMillis();
    }

    protected int calculateLoginLifetime() {
        return getTokenValiditySeconds();
    }

    protected String retrieveUserName(Authentication authentication) {
        if (isInstanceOfUserDetails(authentication)) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            return authentication.getPrincipal().toString();
        }
    }

    protected String retrievePassword(Authentication authentication) {
        if (isInstanceOfUserDetails(authentication)) {
            return ((UserDetails) authentication.getPrincipal()).getPassword();
        } else {
            if (authentication.getCredentials() == null) {
                return null;
            }
            return authentication.getCredentials().toString();
        }
    }

    private boolean isInstanceOfUserDetails(Authentication authentication) {
        return authentication.getPrincipal() instanceof UserDetails;
    }

    private static boolean checkEquals(String expected, String actual) {
        byte[] expectedBytes = bytesUtf8(expected);
        byte[] actualBytes = bytesUtf8(actual);
        if (expectedBytes.length != actualBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0 ; i < expectedBytes.length ; i++) {
            result |= expectedBytes[i] ^ actualBytes[i];
        }
        return result == 0;
    }

    private static byte[] bytesUtf8(String s) {
        if (s == null) {
            return null;
        }
        return Utf8.encode(s);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    }

}
