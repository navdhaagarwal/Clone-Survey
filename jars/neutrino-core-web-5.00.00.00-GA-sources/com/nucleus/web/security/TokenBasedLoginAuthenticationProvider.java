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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

/**
 * Authentication provider for token based auto-login
 * @author Nucleus Software Exports Limited
 * 
 */
public class TokenBasedLoginAuthenticationProvider implements AuthenticationProvider, InitializingBean {

    private String key;

    public TokenBasedLoginAuthenticationProvider() {

    }

    public TokenBasedLoginAuthenticationProvider(String key) {
        this.key = key;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.hasLength(key);

    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }

        if (this.key.hashCode() != ((AutoLoginAuthenticationToken) authentication).getKeyHash()) {
            throw new InvalidLoginTokenException("The presented auto-login token does not contain the expected key");
        }

        return authentication;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean supports(Class<?> authentication) {
        return (AutoLoginAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
