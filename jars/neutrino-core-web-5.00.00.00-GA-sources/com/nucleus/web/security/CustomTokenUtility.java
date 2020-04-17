package com.nucleus.web.security;

import com.nucleus.web.security.InvalidLoginTokenException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.util.StringUtils;

public class CustomTokenUtility {

    private CustomTokenUtility(){

    }

    public static Boolean isCustomTokenExpired(String tokenValue) throws InvalidCookieException{
        if(StringUtils.isEmpty(tokenValue)){
            return true;
        }
        return isTokenExpired(Long.parseLong(decodeToken(tokenValue)[1]));
    }

    private static boolean isTokenExpired(long tokenExpiryTime) {
        return tokenExpiryTime < System.currentTimeMillis();
    }

    public static String[] decodeToken(String tokenValue) throws InvalidCookieException {
        if (!Base64.isBase64(tokenValue.getBytes())) {
            throw new InvalidLoginTokenException("Login token was not Base64 encoded; value was \'" + tokenValue + "\'");
        } else {
            String tokenAsPlainText = new String(Base64.decode(tokenValue.getBytes()));
            String[] tokens = StringUtils.delimitedListToStringArray(tokenAsPlainText, ":");
            if ((tokens[0].equalsIgnoreCase("http") || tokens[0].equalsIgnoreCase("https")) && tokens[1].startsWith("//")) {
                String[] newTokens = new String[tokens.length - 1];
                newTokens[0] = tokens[0] + ":" + tokens[1];
                System.arraycopy(tokens, 2, newTokens, 1, newTokens.length - 1);
                tokens = newTokens;
            }
            return tokens;
        }
    }

}
